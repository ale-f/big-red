package org.bigraph.model;

import java.util.Map;

import org.bigraph.model.ModelObject;
import org.bigraph.model.assistants.IPropertyProvider;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.Change;
import org.bigraph.model.interfaces.ILink;
import org.bigraph.model.interfaces.INode;
import org.bigraph.model.interfaces.IPort;


public class PortSpec extends ModelObject implements IPort {
	@RedProperty(fired = String.class, retrieved = String.class)
	public static final String PROPERTY_NAME = "PortSpecName";
	
	@RedProperty(fired = Control.class, retrieved = Control.class)
	public static final String PROPERTY_CONTROL = "PortSpecControl";
	
	private abstract class PortSpecChange extends ModelObjectChange {
		@Override
		public PortSpec getCreator() {
			return PortSpec.this;
		}
	}
	
	public class ChangeName extends PortSpecChange {
		public String name;
		public ChangeName(String name) {
			this.name = name;
		}
		
		private String oldName;
		@Override
		public void beforeApply() {
			oldName = getCreator().getName();
		}
		
		@Override
		public boolean isReady() {
			return (name != null);
		}
		
		@Override
		public boolean canInvert() {
			return (oldName != null);
		}
		
		@Override
		public Change inverse() {
			return new ChangeName(oldName);
		}
		
		@Override
		public String toString() {
			return "Change(set name of port " + getCreator() +
					" to " + name + ")";
		}
	}
	
	public class ChangeRemovePort extends PortSpecChange {
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
	}
	
	private String name;
	private Control control;
	
	@Override
	public PortSpec clone(Map<ModelObject, ModelObject> m) {
		PortSpec p = (PortSpec)super.clone(m);
		p.setName(getName());
		return p;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public String getName(IPropertyProvider context) {
		return (String)getProperty(context, PROPERTY_NAME);
	}
	
	protected void setName(String name) {
		String oldName = this.name;
		this.name = name;
		firePropertyChange(PROPERTY_NAME, oldName, name);
	}
	
	public Control getControl() {
		return control;
	}
	
	public Control getControl(IPropertyProvider context) {
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
	
	public ChangeName changeName(String name) {
		return new ChangeName(name);
	}
	
	public ChangeRemovePort changeRemove() {
		return new ChangeRemovePort();
	}
	
	@Override
	protected Object getProperty(String name) {
		if (PROPERTY_NAME.equals(name)) {
			return getName();
		} else if (PROPERTY_CONTROL.equals(name)) {
			return getControl();
		} else return super.getProperty(name);
	}
}
