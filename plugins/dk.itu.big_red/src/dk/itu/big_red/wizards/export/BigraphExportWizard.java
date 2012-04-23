package dk.itu.big_red.wizards.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import dk.itu.big_red.editors.assistants.IFactory;
import dk.itu.big_red.editors.simulation_spec.ExportResults;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.SimulationSpec;
import dk.itu.big_red.model.load_save.LoadFailedException;
import dk.itu.big_red.model.load_save.Loader;
import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.model.load_save.Saver;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.io.TotalReadStrategy;
import dk.itu.big_red.utilities.ui.SaverOptionsGroup;
import dk.itu.big_red.utilities.ui.jface.ListContentProvider;
import dk.itu.big_red.utilities.ui.jface.WorkspaceProvider;

public class BigraphExportWizard extends Wizard implements IExportWizard {
	@Override
	public boolean canFinish() {
		return (super.canFinish() &&
				selectedFile != null && selectedExporter != null);
	}
	
	@Override
	public boolean performFinish() {
		try {
			IOAdapter io = new IOAdapter();
			selectedExporter.setModel(Loader.fromFile(selectedFile));
			selectedExporter.setOutputStream(io.getOutputStream());
			selectedExporter.exportObject();
			new ExportResults(
					TotalReadStrategy.readString(io.getInputStream())).
				new ExportResultsDialog(getShell()).open();
			return true;
		} catch (LoadFailedException e) {
			e.printStackTrace();
			return false;
		} catch (SaveFailedException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private IFile selectedFile;
	private Saver selectedExporter;
	
	private final class ExporterWizardPage extends WizardPage {
		protected ExporterWizardPage() {
			super("Select an exporter");
			
			setTitle("Select an exporter");
			setDescription("Select and configure the exporter.");
			setPageComplete(true);
		}
		
		private ComboViewer cv;
		private SaverOptionsGroup sog;
		
		private final void cvSelection() {
			selectedExporter = (Saver)
					((IFactory<?>)
						((IStructuredSelection)cv.getSelection()).
							getFirstElement()).newInstance();
			sog.setSaver(selectedExporter);
			getContainer().updateButtons();
		}
		
		@Override
		public void createControl(Composite parent) {
			Composite self = new Composite(parent, SWT.NONE);
			self.setLayout(new GridLayout(1, true));
			cv = new ComboViewer(self);
			cv.setContentProvider(new ListContentProvider());
			cv.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					return ((IFactory<?>)element).getName();
				}
			});
			cv.addSelectionChangedListener(new ISelectionChangedListener() {
				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					cvSelection();
				}
			});
			cv.getCombo().setLayoutData(
					new GridData(SWT.FILL, SWT.FILL, true, false));
			new Label(self, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(
					new GridData(SWT.FILL, SWT.FILL, true, false));
			sog = new SaverOptionsGroup(self);
			
			setControl(self);
			prepare();
		}
		
		void prepare() {
			cv.setInput(null);
			sog.setSaver(null);
			if (selectedFile == null)
				return;
			
			String id;
			try {
				id = getClassForType(
					selectedFile.getContentDescription().
						getContentType().getId());
			} catch (CoreException e) {
				return;
			}
			
			ArrayList<IFactory<Saver>> f = new ArrayList<IFactory<Saver>>();
			for (final IConfigurationElement ice :
				RegistryFactory.getRegistry().
					getConfigurationElementsFor(Saver.EXTENSION_POINT)) {
				if (id.equals(ice.getAttribute("exports"))) {
					f.add(new IFactory<Saver>() {
						@Override
						public String getName() {
							return ice.getAttribute("name");
						}

						@Override
						public Saver newInstance() {
							try {
								return (Saver)
									ice.createExecutableExtension("class");
							} catch (CoreException e) {
								return null;
							}
						}
					});
				}
			}
			cv.setInput(f);
			cv.setSelection(new StructuredSelection(f.get(0)), true);
			cvSelection();
		}
	}
	
	private static Map<String, String> typesMap =
			new HashMap<String, String>();
	static {
		typesMap.put(Bigraph.CONTENT_TYPE,
				Bigraph.class.getCanonicalName());
		typesMap.put(Signature.CONTENT_TYPE,
				Signature.class.getCanonicalName());
		typesMap.put(ReactionRule.CONTENT_TYPE,
				ReactionRule.class.getCanonicalName());
		typesMap.put(SimulationSpec.CONTENT_TYPE,
				SimulationSpec.class.getCanonicalName());
	}
	
	private String getClassForType(String contentType) {
		return typesMap.get(contentType);
	}
	
	@Override
	public void init(IWorkbench workbench, final IStructuredSelection selection) {
		final ExporterWizardPage exporterPage = new ExporterWizardPage();
		
		addPage(new WizardPage("Select a file") {
			@Override
			public void createControl(Composite parent) {
				final TreeViewer t = new TreeViewer(parent,
						SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
				WorkspaceProvider p = new WorkspaceProvider();
				t.addFilter(new ViewerFilter() {
					@Override
					public boolean select(Viewer viewer, Object parentElement,
							Object element) {
						if (element instanceof IResource) {
							return ((IResource)element).getName().
									charAt(0) != '.';
						} else return false;
					}
				});
				t.setComparator(new ViewerComparator());
				t.setLabelProvider(
						WorkbenchLabelProvider.
							getDecoratingWorkbenchLabelProvider());
				t.setContentProvider(p);
				t.setInput(ResourcesPlugin.getWorkspace().getRoot());
				
				t.addDoubleClickListener(new IDoubleClickListener() {
					@Override
					public void doubleClick(DoubleClickEvent event) {
						if (canFlipToNextPage())
							getContainer().showPage(getNextPage());
					}
				});
				
				t.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						boolean complete = isPageComplete();
						selectedFile = null;
						selectedExporter = null;
						try {
							ITreeSelection s = (ITreeSelection)t.getSelection();
							complete = (s.getFirstElement() instanceof IFile);
							if (!complete)
								return;
							selectedFile = (IFile)s.getFirstElement();
							try {
								String id =
									selectedFile.getContentDescription().
										getContentType().getId();
								complete =
									(Bigraph.CONTENT_TYPE.equals(id) ||
									 Signature.CONTENT_TYPE.equals(id) ||
									 ReactionRule.CONTENT_TYPE.equals(id) ||
									 SimulationSpec.CONTENT_TYPE.equals(id));
							} catch (CoreException e) {
								complete = false;
								return;
							}
						} finally {
							if (complete != isPageComplete())
								setPageComplete(complete);
							exporterPage.prepare();
						}
					}
				});
				
				setControl(t.getControl());
			}
			
			{
				setTitle("Select a file");
				setDescription("Select a bigraph, signature, " +
						"reaction rule, or simulation spec to export.");
			}
		});
		
		addPage(exporterPage);
	}
}
