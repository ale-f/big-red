package org.bigraph.model.process;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.utilities.FilteringIterable;

public abstract class AbstractParticipantHost implements IParticipantHost {
	private List<IParticipant> participants = new ArrayList<IParticipant>();

	@Override
	public void addParticipant(IParticipant participant) {
		participants.add(participant);
		participant.setHost(this);
	}

	@Override
	public List<? extends IParticipant> getParticipants() {
		return participants;
	}
	
	protected <T extends IParticipant>
			Iterable<? extends T> getParticipants(Class<T> klass) {
		return getParticipants(this, klass);
	}
	
	protected static <T extends IParticipant> Iterable<? extends T>
			getParticipants(IParticipantHost self, Class<T> klass) {
		return new FilteringIterable<T>(klass, self.getParticipants());
	}
}
