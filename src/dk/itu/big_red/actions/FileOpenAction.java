package dk.itu.big_red.actions;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import dk.itu.big_red.BigRedConstants;
import dk.itu.big_red.util.UI;

public class FileOpenAction extends Action {
	private IWorkbenchWindow window;
	
	public FileOpenAction(IWorkbenchWindow window) {
		this.window = window;
		
		setId("net.ybother.big_red.open");
		setText("&Open...");
		setAccelerator(SWT.CTRL | 'O');
	}

	public boolean isEnabled() {
		return true;
	}
	
	public void run() {
		if (window != null) {
			FileDialog f = UI.getFileDialog(window.getShell(), SWT.OPEN);
			f.setText("Open");
			f.setFilterExtensions(BigRedConstants.FILE_FILTER_EXTENSIONS);
			f.setFilterNames(BigRedConstants.FILE_FILTER_NAMES);

			String filename = f.open();
			if (filename == null)
				return;
			
			try {
				window.getActivePage().openEditor(
					new dk.itu.big_red.EditorInput(filename),
					dk.itu.big_red.GraphicalEditor.ID);
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
