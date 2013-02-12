package dk.itu.big_red.editors;

import java.util.ArrayDeque;

import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.eclipse.jface.action.IStatusLineManager;

public abstract class AbstractNonGEFEditor extends AbstractEditor {
	private IChange savePoint = null;
	private ArrayDeque<IChange>
			undoBuffer = new ArrayDeque<IChange>(),
			redoBuffer = new ArrayDeque<IChange>();
	
	protected IChange getSavePoint() {
		return savePoint;
	}
	
	protected void setSavePoint() {
		savePoint = undoBuffer.peek();
		firePropertyChange(PROP_DIRTY);
	}
	
	@Override
	public boolean canUndo() {
		return (undoBuffer.size() != 0);
	}
	
	@Override
	public boolean canRedo() {
		return (redoBuffer.size() != 0);
	}
	
	protected abstract void tryApplyChange(IChange c)
			throws ChangeCreationException;
	
	protected boolean doChange(IChange c) {
		IStatusLineManager slm =
				getEditorSite().getActionBars().getStatusLineManager();
		try {
			tryApplyChange(c);
			
			redoBuffer.clear();
			undoBuffer.push(c);
			stateChanged();
			
			slm.setErrorMessage(null);
			return true;
		} catch (ChangeCreationException cre) {
			slm.setErrorMessage(cre.getRationale());
			return false;
		}
	}
	
	@Override
	public void undo() {
		try {
			if (!canUndo())
				return;
			IChange c;
			redoBuffer.push(c = undoBuffer.pop());
			tryApplyChange(c.inverse());
			stateChanged();
		} catch (ChangeCreationException cre) {
			throw new Error("Unhandled Change undo failure", cre);
		}
	}
	
	@Override
	public void redo() {
		try {
			if (!canRedo())
				return;
			IChange c;
			tryApplyChange(c = redoBuffer.pop());
			undoBuffer.push(c);
			stateChanged();
		} catch (ChangeCreationException cre) {
			throw new Error("Unhandled Change redo failure", cre);
		}
	}
	
	@Override
	public boolean canRevert() {
		return isDirty();
	}
	
	@Override
	public void revert() {
		while (canUndo())
			undo();
		clearUndo();
	}
	
	/**
	 * Clears the undo and redo buffers and the save point and posts a
	 * notification.
	 */
	protected void clearUndo() {
		undoBuffer.clear();
		redoBuffer.clear();
		savePoint = null;
		stateChanged();
	}
	
	@Override
	public boolean isDirty() {
		return (undoBuffer.peek() != getSavePoint());
	}
}
