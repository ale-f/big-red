package dk.itu.big_red.wizards.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bigraph.model.Bigraph;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Signature;
import org.bigraph.model.SimulationSpec;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.savers.SaveFailedException;
import org.bigraph.model.savers.Saver;
import org.bigraph.model.wrapper.SaverUtilities;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.part.FileEditorInput;
import dk.itu.big_red.editors.assistants.IFactory;
import dk.itu.big_red.editors.simulation_spec.ExportResults;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.io.strategies.TotalReadStrategy;
import dk.itu.big_red.utilities.resources.EclipseFileWrapper;
import dk.itu.big_red.utilities.ui.SaverOptionsGroup;
import dk.itu.big_red.utilities.ui.UI;

public class TextExportWizard extends Wizard implements IExportWizard {
	@Override
	public boolean canFinish() {
		return (super.canFinish() &&
				selectedFile != null && selectedExporter != null);
	}
	
	@Override
	public boolean performFinish() {
		try {
			IOAdapter io = new IOAdapter();
			selectedExporter.setModel(
					new EclipseFileWrapper(selectedFile).load());
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
			cv.setContentProvider(new ArrayContentProvider());
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
					getConfigurationElementsFor(SaverUtilities.EXTENSION_POINT)) {
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
	
	private static String getClassForType(String contentType) {
		return typesMap.get(contentType);
	}
	
	private IResource initialResource = null;
	
	@Override
	public void init(
			IWorkbench workbench, final IStructuredSelection selection) {
		Iterator<?> it = selection.iterator();
		while (it.hasNext()) {
			Object i = it.next();
			if (i instanceof IResource) {
				initialResource = (IResource)i;
				break;
			}
		}
		if (initialResource == null) {
			IEditorPart ep = UI.getWorkbenchPage().getActiveEditor();
			if (ep != null) {
				IEditorInput ei = ep.getEditorInput();
				if (ei instanceof FileEditorInput)
					initialResource = ((FileEditorInput)ei).getFile();
			}
		}
		
		final ExporterWizardPage exporterPage = new ExporterWizardPage();
		
		addPage(new ResourceSelectionPage("Select a file") {
			@Override
			protected void initialize() {
				final TreeViewer t = getTreeViewer();
				
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
							if (exporterPage.getControl() != null)
								exporterPage.prepare();
						}
					}
				});
				
				if (initialResource != null)
					t.setSelection(
							new StructuredSelection(initialResource), true);
			}
		});
		
		addPage(exporterPage);
	}
}
