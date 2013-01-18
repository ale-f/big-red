package org.bigraph.model.process;

/**
 * Classes implementing <strong>IParticipant</strong> can participate in a
 * process being carried out by an {@link IParticipantHost}. (This interface
 * specifies nothing about the <em>form</em> of that participation, however.)
 * @author alec
 */
public interface IParticipant {
	/**
	 * Sets the {@link IParticipantHost} that will use this {@link
	 * IParticipant}. If participants need to configure their host, then they
	 * should do so in this method.
	 * <p>(This method may be called multiple times.)
	 * @param host an {@link IParticipantHost}
	 */
	void setHost(IParticipantHost host);
}
