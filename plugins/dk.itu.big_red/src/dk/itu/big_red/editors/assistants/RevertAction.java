package dk.itu.big_red.editors.assistants;

import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.actions.ActionFactory;

import dk.itu.big_red.editors.AbstractEditor;

public class RevertAction extends Action implements UpdateAction {
	private AbstractEditor editor;
	
	public RevertAction(AbstractEditor editor) {
		this.editor = editor;
		
		setText("Rever&t");
		setId(ActionFactory.REVERT.getId());
	}
	
	protected boolean calculateEnabled() {
		return editor.canRevert();
	}

	@Override
	public void run() {
		if (editor.canRevert())
			editor.revert();
	}

	@Override
	public void update() {
		setEnabled(calculateEnabled());
	}
}
