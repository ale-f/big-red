package org.bigraph.model.process;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.utilities.FilteringIterable;

/**
 * Classes extending <strong>AbstractParticipantHost</strong> inherit a
 * sensible default implementation of the {@link IParticipantHost} interface.
 * @author alec
 */
public abstract class AbstractParticipantHost implements IParticipantHost {
	private List<IParticipant> participants = new ArrayList<IParticipant>();

	@Override
	public void addParticipant(IParticipant participant) {
		participants.add(participant);
		participant.setHost(this);
	}

	protected void removeParticipant(IParticipant participant) {
		participants.remove(participant);
	}
	
	@Override
	public List<? extends IParticipant> getParticipants() {
		return participants;
	}
	
	/**
	 * Returns an {@link Iterable} containing the {@link IParticipant}s of this
	 * {@link IParticipantHost} which have the specified class.
	 * @param klass the class by which to filter the participants
	 * @return an {@link Iterable}
	 */
	protected <T extends IParticipant>
			Iterable<? extends T> getParticipants(Class<T> klass) {
		return new FilteringIterable<T>(klass, this.getParticipants());
	}
}
