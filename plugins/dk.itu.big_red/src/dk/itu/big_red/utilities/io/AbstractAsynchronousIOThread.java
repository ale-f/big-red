package dk.itu.big_red.utilities.io;

import dk.itu.big_red.utilities.ui.UI;

abstract class AbstractAsynchronousIOThread extends Thread {
	protected Boolean running = new Boolean(true);
	
	protected boolean conditionalDispatch(Runnable r) {
		synchronized (running) {
			if (running)
				UI.asyncExec(r);
			return running;
		}
	}
	
	public void kill() {
		synchronized (running) {
			running = false;
		}
	}
}
