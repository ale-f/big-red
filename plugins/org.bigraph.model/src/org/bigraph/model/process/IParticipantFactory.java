package org.bigraph.model.process;


public interface IParticipantFactory {
	IParticipant createParticipant(IParticipantHost host);
}