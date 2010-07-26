package dk.itu.big_red.model;

import dk.itu.big_red.model.NamespaceManager.NameType;
import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.model.interfaces.INameable;

public class Root extends Thing implements INameable {
	@Override
	public Thing clone() throws CloneNotSupportedException {
		return new Root()._overwrite(this);
	}
	
	@Override
	public boolean canContain(ILayoutable child) {
		Class<? extends ILayoutable> c = child.getClass();
		return (c == Node.class || c == Site.class);
	}
	
	@Override
	public void setParent(ILayoutable p) {
		super.setParent(p);
		setName(null);
	}
	
	public String getName() {
		return getBigraph().getNamespace().getName(getClass(), this);
	}
	
	public void setName(String name) {
		NamespaceManager nm = getBigraph().getNamespace();
		String oldName = nm.getName(getClass(), this);
		if (name != null) {
			if (nm.setName(getClass(), name, this))
				listeners.firePropertyChange(PROPERTY_NAME, oldName, name);
		} else {
			String newName = nm.newName(getClass(), this, NameType.NAME_NUMERIC);
			if (!newName.equals(oldName))
				listeners.firePropertyChange(PROPERTY_NAME, oldName, newName);
		}
	}

}
