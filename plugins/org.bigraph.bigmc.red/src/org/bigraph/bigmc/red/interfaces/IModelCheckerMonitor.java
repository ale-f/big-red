package org.bigraph.bigmc.red.interfaces;

/**
 * An <strong>IModelCheckerMonitor</strong> allows progress information and
 * cancellation requests to be carried between an {@link IModelChecker} and its
 * host.
 * @author alec
 */
public interface IModelCheckerMonitor {
	public void start(String name, int totalWork);
	
	public void worked(int units);
	public void subtask(String name);
	
	public void end();
	
	boolean isCanceled();
	void setCanceled(boolean canceled);
}
