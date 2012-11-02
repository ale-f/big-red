package org.bigraph.model;

import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.interfaces.ILink;
import org.bigraph.model.interfaces.INode;
import org.bigraph.model.interfaces.IPort;
import org.bigraph.model.names.Namespace;

public class PortSpec extends NamedModelObject implements IPort {
	@RedProperty(fired = Control.class, retrieved = Control.class)
	public static final String PROPERTY_CONTROL = "PortSpecControl";
	
	private abstract class PortSpecChange extends ModelObjectChange {
		@Override
		public PortSpec getCreator() {
			return PortSpec.this;
		}
	}
	
	@Override
	protected Namespace<PortSpec>
			getGoverningNamespace(PropertyScratchpad context) {
		return getControl(context).getNamespace();
	}
	
	public final class ChangeRemovePort extends PortSpecChange {
		private String oldName;
		private Control oldControl;
		
		@Override
		public void beforeApply() {
			oldName = getCreator().getName();
			oldControl = getCreator().getControl();
		}
		
		@Override
		public boolean canInvert() {
			return (oldName != null && oldControl != null);
		}
		
		@Override
		public Change inverse() {
			return oldControl.new ChangeAddPort(getCreator(), oldName);
		}
		
		@Override
		public String toString() {
			return "Change(remove port " + getCreator() + ")";
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			Control c = getCreator().getControl(context);
			
			context.<PortSpec>getModifiableList(
					c, Control.PROPERTY_PORT, c.getPorts()).
				remove(getCreator());
			context.setProperty(getCreator(), PortSpec.PROPERTY_CONTROL, null);
			
			c.getNamespace().remove(getCreator().getName(context));
			context.setProperty(getCreator(), PROPERTY_NAME, null);
		}
	}
	
	static {
		ExecutorManager.getInstance().addHandler(new PortSpecHandler());
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
	
	public IChange changeRemove() {
		return new ChangeRemovePort();
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
				getControl().getIdentifier(context));
	}
}
