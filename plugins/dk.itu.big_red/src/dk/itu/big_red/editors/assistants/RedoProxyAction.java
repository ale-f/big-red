package dk.itu.big_red.editors.assistants;

import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.actions.ActionFactory;

import dk.itu.big_red.utilities.ui.UI;

public class RedoProxyAction extends Action implements UpdateAction {
	public static interface IRedoImplementor {
		public boolean canRedo();
		public void redo();
	}
	
	private IRedoImplementor redoImplementor;
	
	public RedoProxyAction(IRedoImplementor redoImplementor) {
		this.redoImplementor = redoImplementor;
		setId(ActionFactory.REDO.getId());
		setText("Redo");
		setImageDescriptor(UI.getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
		setDisabledImageDescriptor(
				UI.getImageDescriptor(ISharedImages.IMG_TOOL_REDO_DISABLED));
	}
	
	@Override
	public boolean isEnabled() {
		setEnabled(calculateEnabled());
		return super.isEnabled();
	}
	
	protected boolean calculateEnabled() {
		return redoImplementor.canRedo();
	}
	
	@Override
	public void run() {
		if (redoImplementor.canRedo())
			redoImplementor.redo();
	}
	
	@Override
	public void update() {
		setEnabled(calculateEnabled());
	}
}
