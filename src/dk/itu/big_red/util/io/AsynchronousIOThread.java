package dk.itu.big_red.util.io;

import dk.itu.big_red.util.ui.UI;

abstract class AsynchronousIOThread extends Thread {
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
