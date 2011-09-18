package dk.itu.big_red.model;

import dk.itu.big_red.model.assistants.NamespaceManager;
import dk.itu.big_red.model.interfaces.internal.INameable;

public abstract class NameableContainer extends Container implements INameable {
	@Override
	public String getName() {
		return getBigraph().getNamespaceManager().requireName(getClass(), this);
	}

	@Override
	public void setName(String name) {
		NamespaceManager nm = getBigraph().getNamespaceManager();
		String oldName = nm.getName(getClass(), this);
		if (name != null) {
			if (nm.setName(getClass(), this, name))
				firePropertyChange(PROPERTY_NAME, oldName, name);
		} else {
			String newName = nm.requireName(getClass(), this);
			if (!newName.equals(oldName))
				firePropertyChange(PROPERTY_NAME, oldName, newName);
		}
	}
}
