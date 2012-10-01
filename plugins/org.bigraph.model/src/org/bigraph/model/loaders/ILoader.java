package org.bigraph.model.loaders;

import org.bigraph.model.resources.IFileWrapper;

public interface ILoader {
	interface Participant {
		void setLoader(ILoader loader);
	}
	
	interface InheritableParticipant extends Participant {
		InheritableParticipant newInstance();
	}
	
	ILoader getParent();
	
	void addParticipant(Participant p);
	Iterable<? extends Participant> getParticipants();
	
	void addNotice(LoaderNotice notice);
	void addNotice(LoaderNotice.Type type, String message);
	
	/**
	 * Returns the {@link IFileWrapper} registered with this {@link ILoader}.
	 * @return an {@link IFileWrapper} (can be <code>null</code>)
	 */
	IFileWrapper getFile();
}
