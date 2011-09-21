package dk.itu.big_red.model;

import dk.itu.big_red.model.assistants.NamespaceManager;
import dk.itu.big_red.model.interfaces.IParent;
import dk.itu.big_red.model.interfaces.ISite;
import dk.itu.big_red.model.interfaces.internal.INameable;

/**
 * @author alec
 * @see ISite
 */
public class Site extends LayoutableModelObject implements INameable, ISite {
	@Override
	public Site clone() {
		Site s = new Site();
		
		s.setParent(getParent());
		s.setLayout(getLayout().getCopy());
		
		return s;
	}
	
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

	@Override
	public IParent getIParent() {
		return (IParent)getParent();
	}

	@Override
	public NameType getNameType() {
		return NameType.NAME_NUMERIC;
	}
}
