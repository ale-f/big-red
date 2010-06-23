package dk.itu.big_red.actions;

import org.eclipse.ui.IEditorPart;

import dk.itu.big_red.editors.BigraphEditor;

public class FileSaveAction extends org.eclipse.gef.ui.actions.SaveAction {

	public FileSaveAction(IEditorPart editor) {
		super(editor);
		setLazyEnablementCalculation(true);
	}
	
	protected BigraphEditor getEditor() {
		return (BigraphEditor)getEditorPart();
	}
	
	@Override
	public boolean calculateEnabled() {
		/*
		 * FIXME: this should work. Why won't it work? I hate you, isEnabled,
		 * in all your various guises
		 */
		return false;
	}
	
	public void run() {
	}
}
