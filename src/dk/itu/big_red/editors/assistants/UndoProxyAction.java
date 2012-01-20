package dk.itu.big_red.editors.assistants;

import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory;

public class UndoProxyAction extends Action implements UpdateAction {
	public static interface IUndoImplementor {
		public boolean canUndo();
		public void undo();
	}
	
	private IUndoImplementor undoImplementor;
	
	public UndoProxyAction(IUndoImplementor undoImplementor) {
		this.undoImplementor = undoImplementor;
		setId(ActionFactory.UNDO.getId());
		setText("Undo");
	}
	
	@Override
	public boolean isEnabled() {
		setEnabled(calculateEnabled());
		return super.isEnabled();
	}
	
	protected boolean calculateEnabled() {
		return undoImplementor.canUndo();
	}
	
	@Override
	public void run() {
		if (undoImplementor.canUndo())
			undoImplementor.undo();
	}
	
	@Override
	public void update() {
		setEnabled(calculateEnabled());
	}
}
