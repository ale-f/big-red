package org.bigraph.model.process;

import java.util.ArrayList;
import java.util.List;

/**
 * The <strong>ParticipantManager</strong> is an implementation of {@link
 * IParticipantFactory} which hosts other factories.
 * @see #getInstance()
 * @author alec
 */
public class ParticipantManager implements IParticipantFactory {
	private List<IParticipantFactory> factories =
			new ArrayList<IParticipantFactory>();
	
	/**
	 * Adds a factory to this {@link ParticipantManager}.
	 * @param factory an {@link IParticipantFactory}; must not be {@code null}
	 */
	public void addFactory(IParticipantFactory factory) {
		factories.add(factory);
	}
	
	/**
	 * Removes a factory from this {@link ParticipantManager}.
	 * @param factory an {@link IParticipantFactory}; must not be {@code null}
	 */
	public void removeFactory(IParticipantFactory factory) {
		factories.remove(factory);
	}
	
	/**
	 * Asks all of this {@link ParticipantManager}'s factories to contribute
	 * participants to an {@link IParticipantHost}.
	 */
	@Override
	public void addParticipants(IParticipantHost host) {
		if (host == null)
			return;
		for (IParticipantFactory i : factories)
			i.addParticipants(host);
	}
}
