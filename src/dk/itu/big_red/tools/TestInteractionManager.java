package dk.itu.big_red.tools;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import dk.itu.big_red.utilities.ui.UI;

public class TestInteractionManager extends InteractionManager {

	@Override
	public void run() {
		final Shell s =
				new Shell(UI.getShell(), SWT.SHELL_TRIM | SWT.PRIMARY_MODAL);
		s.setSize(640, 480);
		s.setText("RedBrowser");
		s.setLayout(new FillLayout());
		final Browser b = new Browser(s, SWT.WEBKIT);
		b.setUrl("http://bigraph.org/");
		b.addProgressListener(new ProgressListener() {
			@Override
			public void completed(ProgressEvent event) {
				s.setText(b.getUrl() + " - RedBrowser");
			}
			
			@Override
			public void changed(ProgressEvent event) {
			}
		});
		s.open();
		
		Display d = UI.getWorkbench().getDisplay();
		while (!s.isDisposed())
			if (!d.readAndDispatch())
				d.sleep();
		
		UI.showMessageBox(0, "Gotcha", getSimulationSpec().toString());
	}

}
