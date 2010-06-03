package dk.itu.big_red.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchWindow;

public class WindowShowViewAction extends Action {
	private IWorkbenchWindow window;
	
	public WindowShowViewAction(IWorkbenchWindow window) {
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
			
		}
	}
}
