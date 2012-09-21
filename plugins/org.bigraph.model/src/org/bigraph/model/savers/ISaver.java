package org.bigraph.model.savers;

import java.util.List;

import org.bigraph.model.resources.IFileWrapper;

public interface ISaver {
	interface Participant {
		void setSaver(ISaver saver);
		Participant newInstance();
	}
	
	interface InheritableParticipant extends Participant {
	}
	
	ISaver getParent();
	
	void addParticipant(Participant p);
	Iterable<? extends Participant> getParticipants();
	
	interface Option {
		String getName();
		String getDescription();
		
		Object get();
		void set(Object value);
		
		/**
		 * Returns this {@link Option}'s <i>cookie</i>.
		 * <p>(Two Options with {@link Object#equals(Object) equal} cookies are
		 * considered to represent the same option.)
		 * @return an opaque, comparable object
		 */
		Object getCookie();
	}

	void addOption(Option o);
	List<? extends Option> getOptions();
	
	IFileWrapper getFile();
}
