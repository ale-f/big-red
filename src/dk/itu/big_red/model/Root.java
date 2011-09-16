package dk.itu.big_red.model;

import dk.itu.big_red.model.NamespaceManager.NameType;
import dk.itu.big_red.model.interfaces.IChild;
import dk.itu.big_red.model.interfaces.INode;
import dk.itu.big_red.model.interfaces.IRoot;
import dk.itu.big_red.model.interfaces.ISite;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;
import dk.itu.big_red.model.interfaces.internal.INameable;
import dk.itu.big_red.util.HomogeneousIterable;

/**
 * 
 * @author alec
 * @see IRoot
 */
public class Root extends Container implements INameable, IRoot {
	@Override
	public Container clone() throws CloneNotSupportedException {
		return new Root()._overwrite(this);
	}
	
	@Override
	public boolean canContain(ILayoutable child) {
		Class<? extends ILayoutable> c = child.getClass();
		return (c == Node.class || c == Site.class);
	}
	
	@Override
	public String getName() {
		return getBigraph().getNamespaceManager().getRequiredName(getClass(), this);
	}
	
	@Override
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
	public Iterable<INode> getINodes() {
		return new HomogeneousIterable<INode>(children, INode.class);
	}

	@Override
	public Iterable<ISite> getISites() {
		return new HomogeneousIterable<ISite>(children, ISite.class);
	}

	@Override
	public Iterable<IChild> getIChildren() {
		return new HomogeneousIterable<IChild>(children, IChild.class);
	}
}
