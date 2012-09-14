package org.bigraph.model.loaders;

import java.util.List;

import org.bigraph.model.resources.IFileWrapper;

public interface ILoader {
	interface Participant {
		void setLoader(ILoader loader);
		Participant newInstance();
	}
	
	void addParticipant(Participant p);
	List<? extends Participant> getParticipants();
	
	void addNotice(LoaderNotice notice);
	void addNotice(LoaderNotice.Type type, String message);
	
	/**
	 * Returns the {@link IFileWrapper} registered with this {@link ILoader}.
	 * @return an {@link IFileWrapper} (can be <code>null</code>)
	 */
	IFileWrapper getFile();
}
