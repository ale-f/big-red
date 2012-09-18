package org.bigraph.model.savers;

import java.util.List;

import org.bigraph.model.resources.IFileWrapper;

public interface ISaver {
	public interface Participant {
		void setSaver(ISaver saver);
		Participant newInstance();
	}
	
	void addParticipant(Participant p);
	Iterable<? extends Participant> getParticipants();
	
	public interface Option {
		String getName();
		String getDescription();
		
		Object get();
		void set(Object value);
	}

	void addOption(Option o);
	List<? extends Option> getOptions();
	
	IFileWrapper getFile();
}
