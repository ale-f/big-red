package org.bigraph.model.process;

/**
 * Classes implementing <strong>IParticipantHost</strong> are processes which
 * allow their behaviour to be modified by {@link IParticipant} contributions.
 * @author alec
 */
public interface IParticipantHost {
	/**
	 * Adds an {@link IParticipant} to this {@link IParticipantHost}. (This
	 * method will ordinarily cause the participant's {@link
	 * IParticipant#setHost(IParticipantHost) setHost} method to be called.)
	 * @param participant an {@link IParticipant}; must not be {@code null}
	 */
	void addParticipant(IParticipant participant);
	
	/**
	 * Returns an {@link Iterable} containing all of this {@link
	 * IParticipantHost}'s {@link IParticipant}s.
	 * @return an {@link Iterable}
	 */
	Iterable<? extends IParticipant> getParticipants();
}