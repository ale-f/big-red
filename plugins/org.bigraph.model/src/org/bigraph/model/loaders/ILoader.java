package org.bigraph.model.loaders;

import org.bigraph.model.process.IParticipantHost;
import org.bigraph.model.resources.IFileWrapper;

public interface ILoader extends IParticipantHost {
	ILoader getParent();
	
	void addNotice(LoaderNotice notice);
	void addNotice(LoaderNotice.Type type, String message);
	
	/**
	 * Returns the {@link IFileWrapper} registered with this {@link ILoader}.
	 * @return an {@link IFileWrapper} (can be <code>null</code>)
	 */
	IFileWrapper getFile();
}
