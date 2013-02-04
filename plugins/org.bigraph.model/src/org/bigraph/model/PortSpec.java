package org.bigraph.model;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.interfaces.ILink;
import org.bigraph.model.interfaces.INode;
import org.bigraph.model.interfaces.IPort;
import org.bigraph.model.names.Namespace;

public class PortSpec extends NamedModelObject implements IPort {
	@RedProperty(fired = Control.class, retrieved = Control.class)
	public static final String PROPERTY_CONTROL = "PortSpecControl";
	
	@Override
	protected Namespace<PortSpec>
			getGoverningNamespace(PropertyScratchpad context) {
		return getControl(context).getNamespace();
	}
	
	private Control control;
	
	protected PortSpec clone(Control c) {
		PortSpec p = (PortSpec)super.clone();
		c.getNamespace().put(p.getName(), p);
		return p;
	}
	
	public Control getControl() {
		return control;
	}
	
	public Control getControl(PropertyScratchpad context) {
		return getProperty(context, PROPERTY_CONTROL, Control.class);
	}
	
	protected void setControl(Control control) {
		Control oldControl = this.control;
		this.control = control;
		firePropertyChange(PROPERTY_CONTROL, oldControl, control);
	}
	
	@Override
	public ILink getLink() {
		return null;
	}

	@Override
	public INode getNode() {
		return null;
	}
	
	@Override
	protected Object getProperty(String name) {
		if (PROPERTY_CONTROL.equals(name)) {
			return getControl();
		} else return super.getProperty(name);
	}
	
	@Override
	public void dispose() {
		control = null;
		
		super.dispose();
	}
	
	public static final class Identifier extends NamedModelObject.Identifier {
		private final Control.Identifier control;
		
		public Identifier(String name, Control.Identifier control) {
			super(name);
			this.control = control;
		}
		
		public Control.Identifier getControl() {
			return control;
		}
		
		@Override
		public PortSpec lookup(PropertyScratchpad context, Resolver r) {
			PortSpec n = require(r.lookup(context, this), PortSpec.class);
			if (n != null && equals(n.getIdentifier(context))) {
				return n;
			} else return null;
		}
		
		@Override
		public Identifier getRenamed(String name) {
			return new PortSpec.Identifier(name, getControl());
		}
		
		@Override
		public boolean equals(Object obj_) {
			if (safeClassCmp(this, obj_)) {
				Identifier obj = (Identifier)obj_;
				return
						safeEquals(getName(), obj.getName()) &&
						safeEquals(getControl(), obj.getControl());
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			/* To ensure that the contract for hashCode() isn't violated,
			 * don't use getControl() in this method */
			return compositeHashCode(Identifier.class, getName());
		}
		
		@Override
		public String toString() {
			return "port specification " + getName() + 
					" (of " + getControl() + ")";
		}
	}
	
	@Override
	public Identifier getIdentifier() {
		return getIdentifier(null);
	}
	
	@Override
	public Identifier getIdentifier(PropertyScratchpad context) {
		return new Identifier(getName(context),
				getControl(context).getIdentifier(context));
	}
}
