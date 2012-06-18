package dk.itu.big_red.editors;

import java.util.ArrayDeque;

import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.ChangeRejectedException;
import org.eclipse.jface.action.IStatusLineManager;


public abstract class AbstractNonGEFEditor extends AbstractEditor {
	private Change savePoint = null;
	private ArrayDeque<Change>
			undoBuffer = new ArrayDeque<Change>(),
			redoBuffer = new ArrayDeque<Change>();
	
	protected Change getSavePoint() {
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
	
	protected abstract void tryApplyChange(Change c)
			throws ChangeRejectedException;
	
	protected boolean doChange(Change c) {
		IStatusLineManager slm =
				getEditorSite().getActionBars().getStatusLineManager();
		try {
			tryApplyChange(c);
			
			redoBuffer.clear();
			undoBuffer.push(c);
			stateChanged();
			
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
			stateChanged();
		} catch (ChangeRejectedException cre) {
			throw new Error("Unhandled Change undo failure", cre);
		}
	}
	
	@Override
	public void redo() {
		try {
			if (!canRedo())
				return;
			Change c;
			tryApplyChange(c = redoBuffer.pop());
			undoBuffer.push(c);
			stateChanged();
		} catch (ChangeRejectedException cre) {
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
		undoBuffer.clear();
		redoBuffer.clear();
		stateChanged();
	}
	
	@Override
	public boolean isDirty() {
		return (undoBuffer.peek() != getSavePoint());
	}
}
