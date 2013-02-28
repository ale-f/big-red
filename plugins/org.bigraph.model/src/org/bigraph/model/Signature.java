package org.bigraph.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.bigraph.model.ModelObject;
import org.bigraph.model.assistants.IObjectIdentifier;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.interfaces.ISignature;
import org.bigraph.model.names.HashMapNamespace;
import org.bigraph.model.names.Namespace;
import org.bigraph.model.names.policies.StringNamePolicy;

/**
 * The Signature is a central storage point for {@link Control}s and their
 * properties (both in terms of the bigraph model and their visual
 * representations). Every {@link Bigraph} has an associated Signature, which
 * they consult whenever they need to create a {@link Node}.
 * @author alec
 * @see ISignature
 */
public class Signature extends ModelObject
		implements ISignature, Resolver {
	private Signature parent;
	private List<Signature> signatures = new ArrayList<Signature>();
	
	/**
	 * The property name fired when a control is added or removed.
	 */
	@RedProperty(fired = Control.class, retrieved = List.class)
	public static final String PROPERTY_CONTROL = "SignatureControl";
	
	@RedProperty(fired = Signature.class, retrieved = List.class)
	public static final String PROPERTY_CHILD = "SignatureChild";
	
	@RedProperty(fired = Signature.class, retrieved = Signature.class)
	public static final String PROPERTY_PARENT = "SignatureParent";
	
	private Namespace<Control> ns = new HashMapNamespace<Control>(
			new StringNamePolicy() {
		@Override
		public String get(int value) {
			return "Control" + (value + 1);
		}
	});
	
	public Namespace<Control> getNamespace() {
		return ns;
	}
	
	private HashSet<Control> controls = new HashSet<Control>();
	
	@Override
	public Signature clone() {
		Signature s = (Signature)super.clone();
		
		for (Control c : getControls())
			s.addControl(c.clone(s));
		
		for (Signature t : getSignatures())
			s.addSignature(-1, t.clone());
		
		return s;
	}
	
	protected void addControl(Control c) {
		controls.add(c);
		c.setSignature(this);
		firePropertyChange(PROPERTY_CONTROL, null, c);
	}
	
	protected void removeControl(Control m) {
		controls.remove(m);
		m.setSignature(null);
		firePropertyChange(PROPERTY_CONTROL, m, null);
	}
	
	public Control getControl(String name) {
		return getControl(null, name);
	}
	
	public Control getControl(PropertyScratchpad context, String name) {
		Control c = getNamespace().get(context, name);
		if (c != null)
			return c;
		for (Signature s : getSignatures(context))
			if ((c = s.getControl(context, name)) != null)
				return c;
		return null;
	}
	
	@Override
	public Collection<? extends Control> getControls() {
		return controls;
	}

	@SuppressWarnings("unchecked")
	public Collection<? extends Control> getControls(
			PropertyScratchpad context) {
		return getProperty(context, PROPERTY_CONTROL, Collection.class);
	}

	public static final String CONTENT_TYPE = "dk.itu.big_red.signature";
	
	@Override
	public void dispose() {
		if (controls != null) {
			for (Control c : controls)
				c.dispose();
			controls.clear();
			controls = null;
		}
		
		if (signatures != null) {
			for (Signature s : signatures)
				s.dispose();
			signatures.clear();
			signatures = null;
		}

		super.dispose();
	}
	
	/**
	 * {@inheritDoc}
	 * <p><strong>Special notes for {@link Signature}:</strong>
	 * <ul>
	 * <li>Passing {@link #PROPERTY_CONTROL} will return a
	 * {@link List}&lt;{@link Control}&gt;, <strong>not</strong> a {@link
	 * Control}.
	 * <li>Passing {@link #PROPERTY_CHILD} will return a
	 * {@link List}&lt;{@link Signature}&gt;, <strong>not</strong> a {@link
	 * Signature}.
	 * </ul>
	 */
	@Override
	protected Object getProperty(String name) {
		if (PROPERTY_CONTROL.equals(name)) {
			return getControls();
		} else if (PROPERTY_PARENT.equals(name)) {
			return getParent();
		} else if (PROPERTY_CHILD.equals(name)) {
			return getSignatures();
		} else return super.getProperty(name);
	}
	
	public Signature getParent() {
		return parent;
	}
	
	public Signature getParent(PropertyScratchpad context) {
		return getProperty(context, PROPERTY_PARENT, Signature.class);
	}
	
	protected void setParent(Signature newValue) {
		Signature oldValue = parent;
		parent = newValue;
		firePropertyChange(PROPERTY_PARENT, oldValue, newValue);
	}
	
	public List<? extends Signature> getSignatures() {
		return signatures;
	}
	
	@SuppressWarnings("unchecked")
	public List<? extends Signature> getSignatures(
			PropertyScratchpad context) {
		return getProperty(context, PROPERTY_CHILD, List.class);
	}
	
	protected void addSignature(int position, Signature s) {
		if (position == -1) {
			signatures.add(s);
		} else signatures.add(position, s);
		s.setParent(this);
		firePropertyChange(PROPERTY_CHILD, null, s);
	}
	
	protected void removeSignature(Signature s) {
		signatures.remove(s);
		s.setParent(null);
		firePropertyChange(PROPERTY_CHILD, s, null);
	}
	
	private final Store store = new Store();
	
	@Override
	public Object lookup(
			PropertyScratchpad context, IObjectIdentifier identifier) {
		if (identifier instanceof Signature.Identifier) {
			return this;
		} else if (identifier instanceof Control.Identifier) {
			return getControl(context,
					((Control.Identifier)identifier).getName());
		} else if (identifier instanceof PortSpec.Identifier) {
			PortSpec.Identifier id = (PortSpec.Identifier)identifier;
			Control c = getControl(context, id.getControl().getName());
			if (c != null)
				return c.getNamespace().get(context, id.getName());
		} else if (identifier instanceof Store.EntryIdentifier) {
			return store.lookup(context, identifier);
		}
		return null;
	}
	
	public static final class Identifier implements ModelObject.Identifier {
		@Override
		public Signature lookup(PropertyScratchpad context, Resolver r) {
			return require(r.lookup(context, this), Signature.class);
		}
	}
	
	abstract static class SignatureChangeDescriptor
			extends ModelObjectChangeDescriptor {
		static {
			DescriptorExecutorManager.getInstance().addParticipant(new SignatureDescriptorHandler());
		}
	}
	
	public static final class ChangeAddControlDescriptor
			extends SignatureChangeDescriptor {
		private final Identifier target;
		private final Control.Identifier control;
		
		public ChangeAddControlDescriptor(
				Identifier target, Control.Identifier control) {
			this.target = target;
			this.control = control;
		}
		
		public Identifier getTarget() {
			return target;
		}
		
		public Control.Identifier getControl() {
			return control;
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeRemoveControlDescriptor(
					getTarget(), getControl());
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r) {
			Signature self = getTarget().lookup(context, r);
			Control n = new Control();
			
			context.<Control>getModifiableSet(
					self, PROPERTY_CONTROL, self.getControls()).add(n);
			context.setProperty(n, Control.PROPERTY_SIGNATURE, self);
			
			String name = getControl().getName();
			self.getNamespace().put(context, name, n);
			context.setProperty(n, Control.PROPERTY_NAME, name);
		}
	}
	
	public static final class ChangeRemoveControlDescriptor
			extends SignatureChangeDescriptor {
		private final Identifier target;
		private final Control.Identifier control;
		
		public ChangeRemoveControlDescriptor(
				Identifier target, Control.Identifier control) {
			this.target = target;
			this.control = control;
		}
		
		public Identifier getTarget() {
			return target;
		}
		
		public Control.Identifier getControl() {
			return control;
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeAddControlDescriptor(getTarget(), getControl());
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r) {
			Signature self = getTarget().lookup(context, r);
			Control co = getControl().lookup(context, r);
			
			context.<Control>getModifiableSet(
					self, PROPERTY_CONTROL, self.getControls()).remove(co);
			context.setProperty(co, Control.PROPERTY_SIGNATURE, null);
			
			self.getNamespace().remove(context, getControl().getName());
			context.setProperty(co, Control.PROPERTY_NAME, null);
		}
	}
	
	public static final class ChangeAddSignatureDescriptor
			extends SignatureChangeDescriptor {
		private final Identifier target;
		private final int position;
		private final Signature signature;
		
		public ChangeAddSignatureDescriptor(
				Identifier target, int position, Signature signature) {
			this.target = target;
			this.position = position;
			this.signature = signature;
		}
		
		public Identifier getTarget() {
			return target;
		}
		
		public int getPosition() {
			return position;
		}
		
		public Signature getSignature() {
			return signature;
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeRemoveSignatureDescriptor(
					getTarget(), getPosition(), getSignature());
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r) {
			Signature self = getTarget().lookup(context, r);
			context.<Signature>getModifiableList(
					self, PROPERTY_CHILD, self.getSignatures()).add(
							getSignature());
			context.setProperty(getSignature(), PROPERTY_PARENT, self);
		}
	}
	
	public static final class ChangeRemoveSignatureDescriptor
			extends SignatureChangeDescriptor {
		private final Identifier target;
		private final int position;
		private final Signature signature;

		public ChangeRemoveSignatureDescriptor(
				Identifier target, int position, Signature signature) {
			this.target = target;
			this.position = position;
			this.signature = signature;
		}

		public Identifier getTarget() {
			return target;
		}

		public int getPosition() {
			return position;
		}

		public Signature getSignature() {
			return signature;
		}

		@Override
		public IChangeDescriptor inverse() {
			return new ChangeAddSignatureDescriptor(
					getTarget(), getPosition(), getSignature());
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r) {
			Signature self = getTarget().lookup(context, r);
			
			context.<Signature>getModifiableList(
					self, PROPERTY_CHILD, self.getSignatures()).remove(
							getSignature());
			context.setProperty(getSignature(), PROPERTY_PARENT, null);
		}
	}
}