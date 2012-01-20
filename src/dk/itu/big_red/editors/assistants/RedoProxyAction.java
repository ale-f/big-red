package dk.itu.big_red.editors.assistants;

import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory;

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
