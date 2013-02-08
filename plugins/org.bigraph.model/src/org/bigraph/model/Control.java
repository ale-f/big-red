package org.bigraph.model;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.assistants.ExtendedDataUtilities;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.interfaces.IControl;
import org.bigraph.model.names.HashMapNamespace;
import org.bigraph.model.names.Namespace;
import org.bigraph.model.names.policies.StringNamePolicy;

/**
 * A Control is the bigraphical analogue of a <i>class</i> - a template from
 * which instances ({@link Node}s) should be constructed. Controls are
 * registered with a {@link Bigraph} as part of its {@link Signature}.
 * <p>In the formal bigraph model, controls define labels and numbered ports;
 * this model differs slightly by defining <i>named</i> ports and certain
 * graphical properties (chiefly shapes and default port offsets).
 * @author alec
 * @see IControl
 */
public class Control extends NamedModelObject implements IControl {
	/**
	 * The property name fired when the kind changes.
	 */
	@RedProperty(fired = Kind.class, retrieved = Kind.class)
	public static final String PROPERTY_KIND = "eD!+ControlKind";
	
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
	
	abstract static class ControlChangeDescriptor
			extends ModelObjectChangeDescriptor {
		static {
			DescriptorExecutorManager.getInstance().addParticipant(
					new ControlDescriptorHandler());
		}
	}
	
	@Override
	protected Namespace<Control>
			getGoverningNamespace(PropertyScratchpad context) {
		return getSignature(context).getNamespace();
	}
	
	public static final class ChangeKindDescriptor
			extends ControlChangeDescriptor {
		private final Identifier target;
		private final Kind oldValue, newValue;
		
		public ChangeKindDescriptor(Identifier target,
				Kind oldValue, Kind newValue) {
			this.target = target;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}
		
		public ChangeKindDescriptor(PropertyScratchpad context,
				Control mo, Kind newValue) {
			this(mo.getIdentifier(context), mo.getKind(context), newValue);
		}
		
		public Identifier getTarget() {
			return target;
		}
		
		public Kind getOldValue() {
			return oldValue;
		}
		
		public Kind getNewValue() {
			return newValue;
		}
		
		@Override
		public ChangeKindDescriptor inverse() {
			return new ChangeKindDescriptor(
					getTarget(), getNewValue(), getOldValue());
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r) {
			Control c = getTarget().lookup(context, r);
			context.setProperty(c, PROPERTY_KIND, getNewValue());
		}
	}
	
	public static final class ChangeAddPortSpecDescriptor
			extends ControlChangeDescriptor {
		private final PortSpec.Identifier spec;
		
		public ChangeAddPortSpecDescriptor(PortSpec.Identifier spec) {
			this.spec = spec;
		}
		
		public PortSpec.Identifier getSpec() {
			return spec;
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeRemovePortSpecDescriptor(getSpec());
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r) {
			Control self = getSpec().getControl().lookup(context, r);
			PortSpec p = new PortSpec();
			
			context.<PortSpec>getModifiableList(
					self, Control.PROPERTY_PORT, self.getPorts()).add(p);
			context.setProperty(p, PortSpec.PROPERTY_CONTROL, self);
			
			String name = getSpec().getName();
			self.getNamespace().put(context, name, p);
			context.setProperty(p, PortSpec.PROPERTY_NAME, name);
		}
	}
	
	public static final class ChangeRemovePortSpecDescriptor
			extends ControlChangeDescriptor {
		private final PortSpec.Identifier spec;
		
		public ChangeRemovePortSpecDescriptor(PortSpec.Identifier spec) {
			this.spec = spec;
		}
		
		public PortSpec.Identifier getSpec() {
			return spec;
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeAddPortSpecDescriptor(getSpec());
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r) {
			Control self = getSpec().getControl().lookup(context, r);
			PortSpec p = getSpec().lookup(context, r);
			
			context.<PortSpec>getModifiableList(
					self, Control.PROPERTY_PORT, self.getPorts()).remove(p);
			context.setProperty(p, PortSpec.PROPERTY_CONTROL, null);
			
			self.getNamespace().put(context, getSpec().getName(), null);
			context.setProperty(p, PortSpec.PROPERTY_NAME, null);
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
	
	private Namespace<PortSpec> ns = new HashMapNamespace<PortSpec>(
			new StringNamePolicy());
	
	public Namespace<PortSpec> getNamespace() {
		return ns;
	}
	
	private ArrayList<PortSpec> ports = new ArrayList<PortSpec>();
	
	private Signature signature = null;
	
	protected Control clone(Signature m) {
		Control c = (Control)super.clone();
		
		m.getNamespace().put(c.getName(), c);
		
		c.setKind(getKind());
		
		for (PortSpec p : getPorts())
			c.addPort(p.clone(c));
		
		return c;
	}
	
	public Kind getKind() {
		return getKind(null);
	}
	
	public Kind getKind(PropertyScratchpad context) {
		Kind k = ExtendedDataUtilities.getProperty(
				context, this, PROPERTY_KIND, Kind.class);
		return (k == null ? Kind.ACTIVE : k);
	}
	
	protected void setKind(Kind kind) {
		setExtendedData(PROPERTY_KIND, kind);
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
		return getProperty(context, PROPERTY_SIGNATURE, Signature.class);
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
	public List<? extends PortSpec> getPorts() {
		/* The order of ports is important, so this method should return a List
		 * rather than a bare Collection */
		return ports;
	}
	
	@SuppressWarnings("unchecked")
	public List<? extends PortSpec> getPorts(PropertyScratchpad context) {
		return getProperty(context, PROPERTY_PORT, List.class);
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
		if (PROPERTY_PORT.equals(name)) {
			return getPorts();
		} else if (PROPERTY_KIND.equals(name)) {
			return getKind();
		} else if (PROPERTY_SIGNATURE.equals(name)) {
			return getSignature();
		} else return super.getProperty(name);
	}
	
	@Override
	public void dispose() {
		if (ports != null) {
			for (PortSpec i : ports)
				i.dispose();
			ports.clear();
			ports = null;
		}
		
		super.dispose();
	}
	
	public static final class Identifier extends NamedModelObject.Identifier {
		public Identifier(String name) {
			super(name);
		}
		
		@Override
		public Control lookup(PropertyScratchpad context, Resolver r) {
			return require(r.lookup(context, this), Control.class);
		}
		
		@Override
		public Identifier getRenamed(String name) {
			return new Identifier(name);
		}
		
		@Override
		public String toString() {
			return "control " + getName();
		}
	}
	
	@Override
	public Identifier getIdentifier() {
		return getIdentifier(null);
	}
	
	@Override
	public Identifier getIdentifier(PropertyScratchpad context) {
		return new Identifier(getName(context));
	}
}