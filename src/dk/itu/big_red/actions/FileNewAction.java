package dk.itu.big_red.actions;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

import dk.itu.big_red.util.Utility;

public class FileNewAction extends Action {
	private IWorkbenchWindow window;
	
	public FileNewAction(IWorkbenchWindow window) {
		this.window = window;
		
		setId("net.ybother.big_red.new");
		setText("&New");
		setAccelerator(SWT.CTRL | 'N');
		
		setImageDescriptor(Utility.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));
		setDisabledImageDescriptor(Utility.getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD_DISABLED));
	}

	public void run() {
		if (window != null) {
			try {				
				window.getActivePage().openEditor(
					new dk.itu.big_red.EditorInput("#empty"),
					dk.itu.big_red.GraphicalEditor.ID);
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
