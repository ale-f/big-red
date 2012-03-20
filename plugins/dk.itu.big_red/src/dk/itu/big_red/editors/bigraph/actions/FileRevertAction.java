package dk.itu.big_red.editors.bigraph.actions;

import org.eclipse.gef.ui.actions.EditorPartAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;

import dk.itu.big_red.editors.AbstractGEFEditor;

public class FileRevertAction extends EditorPartAction {
	public FileRevertAction(IEditorPart part) {
		super(part);
		setLazyEnablementCalculation(true);
	}

	@Override
	public void init() {
		super.init();
		setText("Rever&t");
		setId(ActionFactory.REVERT.getId());
	}
	
	@Override
	protected boolean calculateEnabled() {
		return getCommandStack().isDirty();
	}

	@Override
	public void run() {
		((AbstractGEFEditor)getEditorPart()).revert();
	}
}
