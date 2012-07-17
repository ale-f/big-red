package dk.itu.big_red.editors.actions;

import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.actions.ActionFactory;

import dk.itu.big_red.utilities.ui.UI;

public class RedoProxyAction extends ProxyAction {
	public RedoProxyAction(IActionImplementor redoImplementor) {
		super(ActionFactory.REDO.getId());
		setImplementor(redoImplementor);
		
		setText("Redo");
		setImageDescriptor(UI.getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
		setDisabledImageDescriptor(
				UI.getImageDescriptor(ISharedImages.IMG_TOOL_REDO_DISABLED));
	}
}
