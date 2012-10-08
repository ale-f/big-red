package org.bigraph.model;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;

public abstract class NamedModelObject extends ModelObject {
	@RedProperty(fired = String.class, retrieved = String.class)
	public static final String PROPERTY_NAME = "NamedModelObjectName";
	
	public static abstract class Identifier extends ModelObject.Identifier {
		public Identifier(String name) {
			super(name);
		}
		
		@Override
		public abstract NamedModelObject lookup(
				PropertyScratchpad context, Resolver r);
		
		@Override
		public abstract Identifier getRenamed(String name);
	}
	
	private String name;
	
	public String getName() {
		return name;
	}
	
	public String getName(PropertyScratchpad context) {
		return (String)getProperty(context, PROPERTY_NAME);
	}
	
	protected void setName(String name) {
		String oldName = this.name;
		this.name = name;
		firePropertyChange(PROPERTY_NAME, oldName, name);
	}
	
	@Override
	protected NamedModelObject clone() {
		NamedModelObject o = (NamedModelObject)super.clone();
		o.setName(getName());
		return o;
	}
	
	@Override
	protected Object getProperty(String name) {
		if (PROPERTY_NAME.equals(name)) {
			return getName();
		} else return super.getProperty(name);
	}
	
	@Override
	public void dispose() {
		name = null;
		
		super.dispose();
	}
}
