package org.bigraph.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bigraph.model.ModelObject;
import org.bigraph.model.PortSpec.ChangeRemovePort;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.Change;
import org.bigraph.model.interfaces.IControl;

/**
 * A Control is the bigraphical analogue of a <i>class</i> - a template from
 * which instances ({@link Node}s) should be constructed. Controls are
 * registered with a {@link Bigraph} as part of its {@link Signature}.
 * 
 * <p>In the formal bigraph model, controls define labels and numbered ports;
 * this model differs slightly by defining <i>named</i> ports and certain
 * graphical properties (chiefly shapes and default port offsets).
 * @author alec
 * @see IControl
 */
public class Control extends ModelObject implements IControl {
	/**
	 * The property name fired when the kind changes.
	 */
	@RedProperty(fired = Kind.class, retrieved = Kind.class)
	public static final String PROPERTY_KIND = "ControlKind";
	
	/**
	 * The property name fired when the name changes.
	 */
	@RedProperty(fired = String.class, retrieved = String.class)
	public static final String PROPERTY_NAME = "ControlName";
	
	/**
	 * The property name fired when the set of ports changes. If this changes
	 * from <code>null</code> to a non-null value, then a port has been added;
	 * if it changes from a non-null value to <code>null</code>, one has been
	 * removed.
	 */
	@RedProperty(fired = PortSpec.class, retrieved = List.class)
	public static final String PROPERTY_PORT = "ControlPort";
	
	/**
	 * The property name when this Control's containing {@link Signature}
	 * changes.
	 */
	@RedProperty(fired = Signature.class, retrieved = Signature.class)
	public static final String PROPERTY_SIGNATURE = "ControlSignature";
	
	abstract class ControlChange extends ModelObjectChange {
		@Override
		public Control getCreator() {
			return Control.this;
		}
	}
	
	public class ChangeName extends ControlChange {
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
		public boolean canInvert() {
			return (oldName != null);
		}
		
		@Override
		public ChangeName inverse() {
			return new ChangeName(oldName);
		}
		
		@Override
		public boolean isReady() {
			return (name != null);
		}
		
		@Override
		public String toString() {
			return "Change(set name of " + getCreator() + " to " + name + ")";
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			context.setProperty(getCreator(), PROPERTY_NAME, name);
		}
	}
	
	public class ChangeKind extends ControlChange {
		public Kind kind;
		public ChangeKind(Kind kind) {
			this.kind = kind;
		}
		
		private Kind oldKind;
		@Override
		public void beforeApply() {
			oldKind = getCreator().getKind();
		}
		
		@Override
		public boolean canInvert() {
			return (oldKind != null);
		}
		
		@Override
		public boolean isReady() {
			return (kind != null);
		}
		
		@Override
		public ChangeKind inverse() {
			return new ChangeKind(oldKind);
		}
		
		@Override
		public String toString() {
			return "Change(set kind of " + getCreator() + " to " + kind + ")";
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			context.setProperty(getCreator(), PROPERTY_KIND, kind);
		}
	}
	
	public class ChangeAddPort extends ControlChange {
		public PortSpec port;
		public String name;
		
		public ChangeAddPort(PortSpec port, String name) {
			this.port = port;
			this.name = name;
		}
		
		@Override
		public boolean isReady() {
			return (port != null && name != null);
		}
		
		@Override
		public ChangeRemovePort inverse() {
			return port.changeRemove();
		}
		
		@Override
		public String toString() {
			return "Change(add port " + port + " to " + getCreator() +
					" with name \"" + name + "\")";
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			context.<PortSpec>getModifiableList(
					getCreator(), Control.PROPERTY_PORT, getPorts()).
				add(port);
			context.setProperty(port, PortSpec.PROPERTY_NAME, name);
			context.setProperty(port, PortSpec.PROPERTY_CONTROL, getCreator());
		}
	}

	public class ChangeRemoveControl extends ControlChange {
		private Signature oldSignature;
		
		@Override
		public void beforeApply() {
			oldSignature = getSignature();
		}
		
		@Override
		public boolean canInvert() {
			return (oldSignature != null);
		}
		
		@Override
		public Change inverse() {
			return oldSignature.new ChangeAddControl(getCreator());
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			Signature s = getCreator().getSignature(context);
			
			context.<Control>getModifiableList(
					s, Signature.PROPERTY_CONTROL, s.getControls()).
				remove(getCreator());
			context.setProperty(getCreator(),
					Control.PROPERTY_SIGNATURE, null);
		}
	}
	
	public static enum Kind {
		ATOMIC {
			@Override
			public String toString() {
				return "atomic";
			}
		},
		ACTIVE {
			@Override
			public String toString() {
				return "active";
			}
		},
		PASSIVE {
			@Override
			public String toString() {
				return "passive";
			}
		};
	}
	
	private ArrayList<PortSpec> ports = new ArrayList<PortSpec>();
	
	private String name = "Unknown";
	private Control.Kind kind = Kind.ACTIVE;
	private Signature signature = null;
	
	@Override
	public Control clone(Map<ModelObject,ModelObject> m) {
		Control c = (Control)super.clone(m);
		
		c.setName(getName());
		c.setKind(getKind());
		
		for (PortSpec p : getPorts())
			c.addPort(p.clone(m));
		
		return c;
	}

	protected void setName(String name) {
		if (name != null) {
			String oldName = this.name;
			this.name = name;
			firePropertyChange(PROPERTY_NAME, oldName, name);
		}
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	public String getName(PropertyScratchpad context) {
		return (String)getProperty(context, PROPERTY_NAME);
	}
	
	public Kind getKind() {
		return kind;
	}
	
	public Kind getKind(PropertyScratchpad context) {
		return (Kind)getProperty(context, PROPERTY_KIND);
	}
	
	protected void setKind(Kind kind) {
		Kind oldKind = this.kind;
		this.kind = kind;
		firePropertyChange(PROPERTY_KIND, oldKind, kind);
	}
	
	protected void addPort(PortSpec p) {
		if (ports.add(p)) {
			p.setControl(this);
			firePropertyChange(PROPERTY_PORT, null, p);
		}
	}
	
	protected void removePort(PortSpec p) {
		if (ports.remove(p)) {
			p.setControl(null);
			firePropertyChange(PROPERTY_PORT, p, null);
		}
	}
	
	public Signature getSignature() {
		return signature;
	}
	
	public Signature getSignature(PropertyScratchpad context) {
		return (Signature)getProperty(context, PROPERTY_SIGNATURE);
	}
	
	void setSignature(Signature signature) {
		Signature oldSignature = this.signature;
		this.signature = signature;
		firePropertyChange(PROPERTY_SIGNATURE, oldSignature, signature);
	}
	
	/**
	 * Gets the {@link List} of this {@link Control}'s {@link PortSpec}s.
	 * @return a reference to the internal list; modify it and <s>vampire
	 * bats</s> undocumented behaviour will steal your blood while you sleep
	 * @see #createPorts()
	 */
	@Override
	public List<PortSpec> getPorts() {
		return ports;
	}
	
	@SuppressWarnings("unchecked")
	public List<PortSpec> getPorts(PropertyScratchpad context) {
		return (List<PortSpec>)getProperty(context, PROPERTY_PORT);
	}
	
	/**
	 * Produces a <i>new</i> array of {@link Port}s to give to a {@link Node}.
	 * @return an array of Ports
	 * @see #getPorts()
	 */
	public ArrayList<Port> createPorts() {
		ArrayList<Port> r = new ArrayList<Port>();
		for (PortSpec i : ports)
			r.add(new Port(i));
		return r;
	}
	
	/**
	 * {@inheritDoc}
	 * <p><strong>Special notes for {@link Control}:</strong>
	 * <ul>
	 * <li>Passing {@link #PROPERTY_PORT} will return a {@link List}&lt;{@link
	 * PortSpec}&gt;, <strong>not</strong> a {@link PortSpec}.
	 * </ul>
	 */
	@Override
	protected Object getProperty(String name) {
		if (PROPERTY_NAME.equals(name)) {
			return getName();
		} else if (PROPERTY_PORT.equals(name)) {
			return getPorts();
		} else if (PROPERTY_KIND.equals(name)) {
			return getKind();
		} else if (PROPERTY_SIGNATURE.equals(name)) {
			return getSignature();
		} else return super.getProperty(name);
	}
	
	@Override
	public void dispose() {
		kind = null;
		name = null;
		
		super.dispose();
	}
	
	public Change changeName(String name) {
		return new ChangeName(name);
	}
	
	public ChangeKind changeKind(Kind kind) {
		return new ChangeKind(kind);
	}
	
	public ChangeAddPort changeAddPort(PortSpec port, String name) {
		return new ChangeAddPort(port, name);
	}
	
	public ChangeRemoveControl changeRemove() {
		return new ChangeRemoveControl();
	}
}