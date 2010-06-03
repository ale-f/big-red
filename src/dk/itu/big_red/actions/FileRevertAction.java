package dk.itu.big_red.actions;


import org.eclipse.gef.ui.actions.EditorPartAction;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;

import dk.itu.big_red.GraphicalEditor;


public class FileRevertAction extends EditorPartAction {

	public FileRevertAction(IEditorPart part) {
		super(part);
		setLazyEnablementCalculation(true);
		
	}

	public void init() {
		super.init();
		setText("Rever&t");
		setId(ActionFactory.REVERT.getId());
	}
	
	@Override
	protected boolean calculateEnabled() {
		return getCommandStack().isDirty();
	}

	public void run() {
		((GraphicalEditor)getEditorPart()).revert();
	}
}
