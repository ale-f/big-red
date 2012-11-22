package org.bigraph.model.process;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.loaders.Loader;
import org.bigraph.model.savers.Saver;

/**
 * The <strong>ParticipantManager</strong> is an implementation of {@link
 * IParticipantFactory} which hosts other factories.
 * @see #getInstance()
 * @author alec
 */
public class ParticipantManager implements IParticipantFactory {
	private static final class Holder {
		private static final ParticipantManager INSTANCE =
				new ParticipantManager();
	}
	
	/**
	 * Returns the shared {@link ParticipantManager} instance, used by {@link
	 * Saver}s and {@link Loader}s to find participants.
	 * @return the shared {@link ParticipantManager}
	 */
	public static final ParticipantManager getInstance() {
		return Holder.INSTANCE;
	}
	
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
