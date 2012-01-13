package dk.itu.big_red.utilities.resources;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
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
	public static enum Mode {
		GENERIC,
		CONTAINER,
		FILE
	};
	
	public static int CLEAR = 0x19890522;
	
	public ResourceTreeSelectionDialog(Shell parent, IContainer input, Mode mode, String... contentTypes) {
		super(parent, new WorkbenchLabelProvider(), new WorkbenchContentProvider());
		setAllowMultiple(false);
		setInput(input);
		if (mode == Mode.FILE) {
			setValidator(new FileSelectionStatusValidator());
			FileTypeViewerFilter fv = new FileTypeViewerFilter();
			addFilter(fv);
			for (String i : contentTypes)
				fv.addContentType(i);
		} else if (mode == Mode.CONTAINER) {
			setValidator(new ContainerSelectionStatusValidator());
			addFilter(new ContainerViewerFilter());
		}
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		createButton(parent, IDialogConstants.DESELECT_ALL_ID, "Clear", false);
	}
	
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.DESELECT_ALL_ID) {
			setResult(null);
			setReturnCode(CLEAR);
			close();
		} else super.buttonPressed(buttonId);
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
		if (selection != null) {
			super.setInitialSelection(selection);
		} else super.setInitialSelections(new Object[0]);
	}
}
