package dk.itu.big_red.model;

import dk.itu.big_red.model.NamespaceManager.NameType;
import dk.itu.big_red.model.interfaces.IInnerName;
import dk.itu.big_red.model.interfaces.internal.INameable;

/**
 * 
 * @author alec
 * @see IInnerName
 */
public class InnerName extends Point implements INameable, IInnerName {
	@Override
	public InnerName clone() {
		System.out.println("! Clone?");
		return new InnerName();
	}
	
	public boolean canContain(Thing child) {
		return false;
	}
	
	@Override
	public Bigraph getBigraph() {
		return getParent().getBigraph();
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
			String newName = nm.newName(getClass(), this, NameType.NAME_ALPHABETIC);
			if (!newName.equals(oldName))
				listeners.firePropertyChange(PROPERTY_NAME, oldName, newName);
		}
	}
}
