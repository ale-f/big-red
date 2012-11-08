package org.bigraph.model.process;

import java.util.ArrayList;
import java.util.List;

public class ParticipantManager implements IParticipantFactory {
	private static final class Holder {
		private static final ParticipantManager INSTANCE =
				new ParticipantManager();
	}
	
	public static final ParticipantManager getInstance() {
		return Holder.INSTANCE;
	}
	
	private List<IParticipantFactory> factories =
			new ArrayList<IParticipantFactory>();
	
	public void addFactory(IParticipantFactory factory) {
		factories.add(factory);
	}
	
	public void removeFactory(IParticipantFactory factory) {
		factories.remove(factory);
	}
	
	@Override
	public void addParticipants(IParticipantHost host) {
		if (host == null)
			return;
		for (IParticipantFactory i : factories)
			i.addParticipants(host);
	}
}
