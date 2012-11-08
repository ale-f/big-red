package org.bigraph.model.process;

public interface IParticipantHost {
	void addParticipant(IParticipant participant);
	Iterable<? extends IParticipant> getParticipants();
}