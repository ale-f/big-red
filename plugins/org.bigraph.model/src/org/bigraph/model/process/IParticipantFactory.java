package org.bigraph.model.process;

/**
 * Classes implementing <strong>IParticipantFactory</strong> can install
 * {@link IParticipant}s into {@link IParticipantHost}s.
 * @author alec
 *
 */
public interface IParticipantFactory {
	/**
	 * Adds some participants to an {@link IParticipantHost}.
	 * @param host an {@link IParticipantHost}; must not be {@code null}
	 */
	void addParticipants(IParticipantHost host);
}