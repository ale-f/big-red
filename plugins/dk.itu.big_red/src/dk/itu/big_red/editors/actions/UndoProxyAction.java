package dk.itu.big_red.editors.actions;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.actions.ActionFactory;

import dk.itu.big_red.utilities.ui.UI;

public class UndoProxyAction extends ProxyAction {
	public UndoProxyAction(IActionImplementor undoImplementor) {
		super(ActionFactory.UNDO.getId());
		setImplementor(undoImplementor);
		
		setText("Undo");
		setImageDescriptor(UI.getImageDescriptor(ISharedImages.IMG_TOOL_UNDO));
		setDisabledImageDescriptor(
				UI.getImageDescriptor(ISharedImages.IMG_TOOL_UNDO_DISABLED));
	}
}
