package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.List;

import dk.itu.big_red.model.NamespaceManager.NameType;
import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.model.interfaces.INameable;
import dk.itu.big_red.model.interfaces.pure.IParent;
import dk.itu.big_red.model.interfaces.pure.ISite;

public class Site extends Thing implements INameable, ISite {
	public Thing clone() throws CloneNotSupportedException {
		return new Site()._overwrite(this);
	}
	
	/**
	 * Returns false.
	 */
	@Override
	public boolean canContain(ILayoutable child) {
		return false;
	}
	
	/**
	 * Does nothing.
	 */
	@Override
	public void addChild(ILayoutable child) {
		return;
	}
	
	/**
	 * Does nothing.
	 */
	@Override
	public void removeChild(ILayoutable child) {
		return;
	}
	
	/**
	 * Returns an empty list.
	 */
	@Override
	public List<ILayoutable> getChildren() {
		return new ArrayList<ILayoutable>();
	}
	
	@Override
	public String getName() {
		return NamespaceManager.sensibleGetNameImplementation(getClass(), this, getBigraph().getNamespaceManager());
	}
	
	public void setName(String name) {
		NamespaceManager nm = getBigraph().getNamespaceManager();
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

	@Override
	public IParent getIParent() {
		return (IParent)getParent();
	}
}
