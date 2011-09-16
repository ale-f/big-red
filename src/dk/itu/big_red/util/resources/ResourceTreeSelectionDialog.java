package dk.itu.big_red.util.resources;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * ResourceTreeSelectionDialogs are reasonably customisable dialogs designed
 * for selecting a single resource from a tree.
 * @author alec
 *
 */
public class ResourceTreeSelectionDialog extends ElementTreeSelectionDialog {
	public static final int MODE_GENERIC = 0,
	                        MODE_CONTAINER = 1,
	                        MODE_FILE = 2;
	
	public ResourceTreeSelectionDialog(Shell parent, IContainer input, int mode, Object... extra) {
		super(parent, new WorkbenchLabelProvider(), new WorkbenchContentProvider());
		setAllowMultiple(false);
		setInput(input);
		if (mode == MODE_FILE) {
			setValidator(new FileSelectionStatusValidator());
			FileContentTypeViewerFilter fv = new FileContentTypeViewerFilter();
			addFilter(fv);
			for (Object i : extra)
				fv.addContentType((String)i);
		} else if (mode == MODE_CONTAINER) {
			setValidator(new ContainerSelectionStatusValidator());
			addFilter(new ContainerViewerFilter());
		}
	}
	
	@Override
	public IResource getFirstResult() {
		return (IResource)super.getFirstResult();
	}
	
	/**
	 * {@inheritDoc}
	 * <p>(This overridden version will do nothing if passed <code>null</code>.) 
	 */
	@Override
	public void setInitialSelection(Object selection) {
		if (selection != null)
			super.setInitialSelection(selection);
	}
}
