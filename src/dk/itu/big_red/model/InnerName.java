package dk.itu.big_red.model;

import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.model.interfaces.INameable;


public class InnerName extends Point implements INameable {
	@Override
	public InnerName clone() {
		System.out.println("! Clone?");
		return new InnerName();
	}
	
	public boolean canContain(Thing child) {
		return false;
	}
	
	@Override
	public void setParent(ILayoutable p) {
		super.setParent(p);
		setName(null);
	}
	
	@Override
	public Bigraph getBigraph() {
		return getParent().getBigraph();
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
			String newName = nm.newName(getClass(), this);
			if (!newName.equals(oldName))
				listeners.firePropertyChange(PROPERTY_NAME, oldName, newName);
		}
	}
}
