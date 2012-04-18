package dk.itu.big_red.wizards.export;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

import dk.itu.big_red.editors.simulation_spec.ExportResults;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.SimulationSpec;
import dk.itu.big_red.model.load_save.LoadFailedException;
import dk.itu.big_red.model.load_save.Loader;
import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.model.load_save.Saver;
import dk.itu.big_red.model.load_save.savers.BigraphXMLSaver;
import dk.itu.big_red.utilities.io.IOAdapter;
import dk.itu.big_red.utilities.ui.jface.ConfigurationElementLabelProvider;
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
			new ExportResults(IOAdapter.readString(io.getInputStream())).
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
	private Saver selectedExporter = new BigraphXMLSaver();
	
	@Override
	public void init(IWorkbench workbench, final IStructuredSelection selection) {
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
				t.setLabelProvider(p);
				t.setContentProvider(p);
				t.setInput(ResourcesPlugin.getWorkspace().getRoot());
				
				t.addSelectionChangedListener(new ISelectionChangedListener() {
					@Override
					public void selectionChanged(SelectionChangedEvent event) {
						boolean complete = isPageComplete();
						selectedFile = null;
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
		
		addPage(new WizardPage("Select an exporter") {
			@Override
			public void createControl(Composite parent) {
				ComboViewer cv = new ComboViewer(parent);
				cv.setLabelProvider(new ConfigurationElementLabelProvider());
				setControl(cv.getControl());
			}
			
			{
				setTitle("Select an exporter");
				setDescription("Select and configure the exporter.");
				setPageComplete(true);
			}
		});
	}
}
