package dk.itu.big_red.interaction_managers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

import dk.itu.big_red.utilities.ui.UI;

public class TestInteractionManager extends InteractionManager {

	@Override
	public void run() {
		final Shell s = UI.chain(
				new Shell(UI.getShell(), SWT.SHELL_TRIM | SWT.PRIMARY_MODAL)).
			size(640, 480).text("RedBrowser").layout(new FillLayout()).done();
		final Browser b = new Browser(s, SWT.WEBKIT);
		b.setUrl("http://bigraph.org/");
		s.open();
		
		while (!s.isDisposed())
			UI.tick();
		
		UI.showMessageBox(UI.getShell(), 0, "Gotcha", getSimulationSpec().toString());
	}

}
