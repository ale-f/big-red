package dk.itu.big_red.editors;

import java.util.ArrayDeque;

import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.eclipse.jface.action.IStatusLineManager;

public abstract class AbstractNonGEFEditor extends AbstractEditor {
	private IChangeDescriptor savePoint = null;
	private ArrayDeque<IChangeDescriptor>
			undoBuffer = new ArrayDeque<IChangeDescriptor>(),
			redoBuffer = new ArrayDeque<IChangeDescriptor>();
	
	protected IChangeDescriptor getSavePoint() {
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
	
	protected abstract Resolver getResolver();
	
	protected void tryApplyChange(IChangeDescriptor c)
			throws ChangeCreationException {
		DescriptorExecutorManager.getInstance().tryApplyChange(
				getResolver(), c);
	}
	
	protected boolean doChange(IChangeDescriptor c) {
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
			IChangeDescriptor c;
			redoBuffer.push(c = undoBuffer.pop());
			tryApplyChange(c.inverse());
			stateChanged();
		} catch (ChangeCreationException cre) {
			throw new RuntimeException("Unhandled Change undo failure", cre);
		}
	}
	
	@Override
	public void redo() {
		try {
			if (!canRedo())
				return;
			IChangeDescriptor c;
			tryApplyChange(c = redoBuffer.pop());
			undoBuffer.push(c);
			stateChanged();
		} catch (ChangeCreationException cre) {
			throw new RuntimeException("Unhandled Change redo failure", cre);
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
