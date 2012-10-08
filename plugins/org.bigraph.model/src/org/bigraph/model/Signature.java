package org.bigraph.model;

import java.util.ArrayList;
import java.util.List;
import org.bigraph.model.Control.ChangeRemoveControl;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Control.ChangeAddPort;
import org.bigraph.model.Control.ChangeKind;
import org.bigraph.model.Control.ChangeName;
import org.bigraph.model.PortSpec.ChangeRemovePort;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.assistants.validators.SignatureValidator;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IChangeExecutor;
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
		implements ISignature, IChangeExecutor,
				ModelObject.Identifier.Resolver {
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
	
	abstract class SignatureChange extends ModelObjectChange {
		@Override
		public Signature getCreator() {
			return Signature.this;
		}
	}
	
	public final class ChangeAddControl extends SignatureChange {
		public final Control control;
		public final String name;
		
		public ChangeAddControl(Control control, String name) {
			this.control = control;
			this.name = name;
		}

		@Override
		public ChangeRemoveControl inverse() {
			return control.new ChangeRemoveControl();
		}
		
		@Override
		public String toString() {
			return "Change(add control " + control + " to signature " +
					getCreator() + " with name " + name + ")";
		}
		
		@Override
		public boolean isReady() {
			return (control != null);
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			context.<Control>getModifiableList(
					getCreator(), PROPERTY_CONTROL, getControls()).
				add(control);
			context.setProperty(control,
					Control.PROPERTY_SIGNATURE, getCreator());
			
			getCreator().getNamespace().put(context, name, control);
			context.setProperty(control, Control.PROPERTY_NAME, name);
		}
	}
	
	public final class ChangeAddSignature extends SignatureChange {
		public final Signature signature;
		
		public ChangeAddSignature(Signature signature) {
			this.signature = signature;
		}
		
		@Override
		public ChangeRemoveSignature inverse() {
			return signature.new ChangeRemoveSignature();
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			context.<Signature>getModifiableList(
					getCreator(), PROPERTY_CHILD, getSignatures()).
				add(signature);
			context.setProperty(signature,
					PROPERTY_PARENT, getCreator());
		}
	}
	
	public final class ChangeRemoveSignature extends SignatureChange {
		public ChangeRemoveSignature() {
		}
		
		private Signature oldParent;
		
		@Override
		public void beforeApply() {
			oldParent = getCreator().getParent();
		}
		
		@Override
		public ChangeAddSignature inverse() {
			return oldParent.new ChangeAddSignature(getCreator());
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			Signature self = getCreator();
			Signature parent = self.getParent(context);
			
			context.<Signature>getModifiableList(
					parent, PROPERTY_CHILD, parent.getSignatures()).
				remove(self);
			context.setProperty(self, PROPERTY_PARENT, null);
		}
	}
	
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
	
	private ArrayList<Control> controls = new ArrayList<Control>();
	
	@Override
	public Signature clone() {
		Signature s = (Signature)super.clone();
		
		for (Control c : getControls())
			s.addControl(c.clone(s));
		
		for (Signature t : getSignatures())
			s.addSignature(t.clone());
		
		return s;
	}
	
	protected void addControl(Control c) {
		if (controls.add(c)) {
			c.setSignature(this);
			firePropertyChange(PROPERTY_CONTROL, null, c);
		}
	}
	
	protected void removeControl(Control m) {
		if (controls.remove(m)) {
			m.setSignature(null);
			firePropertyChange(PROPERTY_CONTROL, m, null);
		}
	}
	
	public Control getControl(String name) {
		for (Control c : getControls())
			if (c.getName().equals(name))
				return c;
		Control c = null;
		for (Signature s : getSignatures())
			if ((c = s.getControl(name)) != null)
				return c;
		return null;
	}
	
	@Override
	public List<? extends Control> getControls() {
		return controls;
	}

	@SuppressWarnings("unchecked")
	public List<? extends Control> getControls(PropertyScratchpad context) {
		return (List<? extends Control>)
				getProperty(context, PROPERTY_CONTROL);
	}
	
	private SignatureValidator validator = new SignatureValidator(this);

	public static final String CONTENT_TYPE = "dk.itu.big_red.signature";
	
	@Override
	public void tryValidateChange(IChange b) throws ChangeRejectedException {
		validator.tryValidateChange(b);
	}
	
	@Override
	public void tryApplyChange(IChange b) throws ChangeRejectedException {
		tryValidateChange(b);
		ChangeExecutor.INSTANCE.doChange(b);
	}

	static final class ChangeExecutor extends ModelObject.ChangeExecutor {
		private static final ChangeExecutor INSTANCE = new ChangeExecutor();
		
		@Override
		protected boolean doChange(IChange b) {
			if (super.doChange(b)) {
				/* do nothing */
			} else if (b instanceof ChangeAddControl) {
				ChangeAddControl c = (ChangeAddControl)b;
				Namespace<Control> ns = c.getCreator().getNamespace();
				c.control.setName(ns.put(c.name, c.control));
				c.getCreator().addControl(c.control);
			} else if (b instanceof ChangeRemoveControl) {
				ChangeRemoveControl c = (ChangeRemoveControl)b;
				Namespace<Control> ns =
						c.getCreator().getSignature().getNamespace();
				c.getCreator().getSignature().removeControl(c.getCreator());
				ns.remove(c.getCreator().getName());
			} else if (b instanceof ChangeName) {
				ChangeName c = (ChangeName)b;
				Namespace<Control> ns =
						c.getCreator().getSignature().getNamespace();
				c.getCreator().setName(
						ns.rename(c.getCreator().getName(), c.name));
			} else if (b instanceof ChangeKind) {
				ChangeKind c = (ChangeKind)b;
				c.getCreator().setKind(c.kind);
			} else if (b instanceof ChangeAddPort) {
				ChangeAddPort c = (ChangeAddPort)b;
				c.getCreator().addPort(c.port);
				c.port.setName(
						c.getCreator().getNamespace().put(c.name, c.port));
			} else if (b instanceof ChangeRemovePort) {
				ChangeRemovePort c = (ChangeRemovePort)b;
				Namespace<PortSpec> ns =
						c.getCreator().getControl().getNamespace();
				c.getCreator().getControl().removePort(c.getCreator());
				ns.remove(c.getCreator().getName());
			} else if (b instanceof PortSpec.ChangeName) {
				PortSpec.ChangeName c = (PortSpec.ChangeName)b;
				PortSpec p = c.getCreator();
				p.setName(p.getControl().getNamespace().rename(
						p.getName(), c.name));
			} else if (b instanceof ChangeAddSignature) {
				ChangeAddSignature c = (ChangeAddSignature)b;
				c.getCreator().addSignature(c.signature);
			} else if (b instanceof ChangeRemoveSignature) {
				ChangeRemoveSignature c = (ChangeRemoveSignature)b;
				c.getCreator().getParent().removeSignature(c.getCreator());
			} else return false;
			return true;
		}
	}
	
	@Override
	public void dispose() {
		for (Control c : getControls())
			c.dispose();
		getControls().clear();
		controls = null;
		
		for (Signature s : getSignatures())
			s.dispose();
		getSignatures().clear();
		signatures = null;

		validator = null;
		
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
		return (Signature)getProperty(context, PROPERTY_PARENT);
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
		return (List<? extends Signature>)
				getProperty(context, PROPERTY_CHILD);
	}
	
	protected void addSignature(Signature s) {
		if (signatures.add(s)) {
			s.setParent(this);
			firePropertyChange(PROPERTY_CHILD, null, s);
		}
	}
	
	protected void removeSignature(Signature s) {
		if (signatures.remove(s)) {
			s.setParent(null);
			firePropertyChange(PROPERTY_CHILD, s, null);
		}
	}
	
	public IChange changeAddControl(Control control, String name) {
		return new ChangeAddControl(control, name);
	}
	
	public IChange changeAddSignature(Signature signature) {
		return new ChangeAddSignature(signature);
	}
	
	public IChange changeRemoveSignature() {
		return new ChangeRemoveSignature();
	}
	
	@Override
	public Object lookup(PropertyScratchpad context, Identifier identifier) {
		if (identifier instanceof Control.Identifier) {
			return getControl(identifier.getName());
		} else return null;
	}
	
	@Override
	public boolean equals(Object obj) {
		return safeClassCmp(this, obj) &&
				((Signature)obj).getControls().equals(getControls());
	}
	
	@Override
	public int hashCode() {
		return compositeHashCode(getControls());
	}
}