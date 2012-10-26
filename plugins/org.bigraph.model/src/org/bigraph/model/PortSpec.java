package org.bigraph.model;

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
	
	@Override
	protected void applyRename(String name) {
		setName(getGoverningNamespace(null).rename(getName(), name));
	}
	
	@Override
	protected void simulateRename(PropertyScratchpad context, String name) {
		context.setProperty(this, PROPERTY_NAME,
				getGoverningNamespace(context).rename(
						context, getName(context), name));
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
		return (Control)getProperty(context, PROPERTY_CONTROL);
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
}
