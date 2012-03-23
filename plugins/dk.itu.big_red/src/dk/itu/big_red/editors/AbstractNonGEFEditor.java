package dk.itu.big_red.editors;

import java.util.ArrayDeque;

import org.eclipse.jface.action.IStatusLineManager;

import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeRejectedException;

public abstract class AbstractNonGEFEditor extends AbstractEditor {
	private boolean dirty = false;
	private Change savePoint = null;
	private ArrayDeque<Change>
			undoBuffer = new ArrayDeque<Change>(),
			redoBuffer = new ArrayDeque<Change>();
	
	protected Change getSavePoint() {
		return savePoint;
	}
	
	protected void setSavePoint() {
		savePoint = undoBuffer.peek();
		checkDirt();
	}
	
	@Override
	public boolean canUndo() {
		return (undoBuffer.size() != 0);
	}
	
	@Override
	public boolean canRedo() {
		return (redoBuffer.size() != 0);
	}
	
	protected abstract void tryApplyChange(Change c)
			throws ChangeRejectedException;
	
	protected boolean doChange(Change c) {
		IStatusLineManager slm =
				getEditorSite().getActionBars().getStatusLineManager();
		try {
			tryApplyChange(c);
			
			redoBuffer.clear();
			undoBuffer.push(c);
			checkDirt();
			updateActions(getStateActions());
			
			slm.setErrorMessage(null);
			return true;
		} catch (ChangeRejectedException cre) {
			slm.setErrorMessage(cre.getRationale());
			return false;
		}
	}
	
	@Override
	public void undo() {
		try {
			if (!canUndo())
				return;
			Change c;
			redoBuffer.push(c = undoBuffer.pop());
			tryApplyChange(c.inverse());
		} catch (ChangeRejectedException cre) {
			throw new Error("Unhandled Change undo failure", cre);
		}
		checkDirt();
		updateActions(getStateActions());
	}
	
	@Override
	public void redo() {
		try {
			if (!canRedo())
				return;
			Change c;
			tryApplyChange(c = redoBuffer.pop());
			undoBuffer.push(c);
		} catch (ChangeRejectedException cre) {
			throw new Error("Unhandled Change redo failure", cre);
		}
		checkDirt();
		updateActions(getStateActions());
	}
	
	@Override
	public boolean isDirty() {
		return dirty;
	}
	
	private void checkDirt() {
		boolean newDirty = (undoBuffer.peek() != getSavePoint());
		if (newDirty != dirty) {
			dirty = newDirty;
			firePropertyChange(PROP_DIRTY);
		}
	}
}
