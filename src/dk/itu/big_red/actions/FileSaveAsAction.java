package dk.itu.big_red.actions;


import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.actions.ActionFactory;

import dk.itu.big_red.BigRedConstants;
import dk.itu.big_red.util.UI;


public class FileSaveAsAction extends org.eclipse.gef.ui.actions.SaveAction {
	
	public FileSaveAsAction(IEditorPart editor) {
		super(editor);
		setLazyEnablementCalculation(true);
	}
	
	protected void init() {
		setId(ActionFactory.SAVE_AS.getId());
		setText("Save &As...");
		setToolTipText("Save As");
	}
	
	@Override
	public void run() {
		FileDialog f = UI.getFileDialog(SWT.SAVE);
		f.setText("Save As...");
		f.setOverwrite(true);
		f.setFilterExtensions(BigRedConstants.FILE_FILTER_EXTENSIONS);
		f.setFilterNames(BigRedConstants.FILE_FILTER_NAMES);
		String file = f.open();
		if (file != null) {
			if (!file.endsWith(".bigred"))
				file = file + ".bigred";
			getEditorPart().doSave(new NullProgressMonitor());
		}
	}
}
