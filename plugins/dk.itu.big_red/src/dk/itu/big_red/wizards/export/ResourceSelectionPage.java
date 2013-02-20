package dk.itu.big_red.wizards.export;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

import static org.eclipse.ui.model.WorkbenchLabelProvider.
		getDecoratingWorkbenchLabelProvider;

public class ResourceSelectionPage extends WizardPage {
	public ResourceSelectionPage(String pageName) {
		super(pageName);
	}

	private TreeViewer treeViewer;
	
	protected TreeViewer createTreeViewer(Composite parent, int flags) {
		return new TreeViewer(parent, flags);
	}
	
	protected TreeViewer getTreeViewer() {
		return treeViewer;
	}
	
	@Override
	public void createControl(Composite parent) {
		treeViewer = createTreeViewer(
				parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		setControl(treeViewer.getControl());
		configureTreeViewer();
		initialize();
	}
	
	protected void configureTreeViewer() {
		getTreeViewer().addFilter(new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement,
					Object element) {
				if (element instanceof IResource) {
					IResource r = (IResource)element;
					return (!r.isHidden() &&
							r.getName().charAt(0) != '.');
				} else return false;
			}
		});
		getTreeViewer().setComparator(
				new ResourceComparator(ResourceComparator.TYPE));
		getTreeViewer().setLabelProvider(
				getDecoratingWorkbenchLabelProvider());
		getTreeViewer().setContentProvider(new WorkbenchContentProvider());
		getTreeViewer().setInput(getInput());
		
		getTreeViewer().addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				if (canFlipToNextPage())
					getContainer().showPage(getNextPage());
			}
		});
	}
	
	/**
	 * Subclasses may override.
	 * @return an {@link IResource} to be used as the tree viewer's input
	 */
	protected IResource getInput() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}
	
	/**
	 * Called once the {@link #createControl(Composite)} method has finished.
	 * <p>By default, this method does nothing. (Subclasses may override.)
	 */
	protected void initialize() {
	}
	
	{
		setTitle("Select a file");
		setDescription("Select a bigraph, signature, " +
				"reaction rule, or simulation spec to export.");
	}
}
