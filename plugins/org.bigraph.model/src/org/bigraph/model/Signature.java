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
	/**
	 * The property name fired when a control is added or removed.
	 */
	@RedProperty(fired = Control.class, retrieved = List.class)
	public static final String PROPERTY_CONTROL = "SignatureControl";
	
	abstract class SignatureChange extends ModelObjectChange {
		@Override
		public Signature getCreator() {
			return Signature.this;
		}
	}
	
	public class ChangeAddControl extends SignatureChange {
		public final Control control;
		
		public ChangeAddControl(Control control) {
			this.control = control;
		}

		@Override
		public ChangeRemoveControl inverse() {
			return control.new ChangeRemoveControl();
		}
		
		@Override
		public String toString() {
			return "Change(add control " + control + " to signature " +
					getCreator();
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
		}
	}
	
	private ArrayList<Control> controls = new ArrayList<Control>();
	
	@Override
	public Signature clone() {
		Signature s = (Signature)super.clone();
		
		for (Control c : getControls())
			s.addControl(c.clone(s));
		
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
		for (Control c : controls)
			if (c.getName().equals(name))
				return c;
		return null;
	}
	
	@Override
	public List<Control> getControls() {
		return controls;
	}

	@SuppressWarnings("unchecked")
	public List<Control> getControls(PropertyScratchpad context) {
		return (List<Control>)getProperty(context, PROPERTY_CONTROL);
	}
	
	private SignatureValidator validator =
		new SignatureValidator(this);

	public static final String CONTENT_TYPE = "dk.itu.big_red.signature";
	
	@Override
	public void tryValidateChange(IChange b) throws ChangeRejectedException {
		validator.tryValidateChange(b);
	}
	
	@Override
	public void tryApplyChange(IChange b) throws ChangeRejectedException {
		tryValidateChange(b);
		doChange(b);
	}

	@Override
	protected boolean doChange(IChange b) {
		if (super.doChange(b)) {
			/* do nothing */
		} else if (b instanceof ChangeAddControl) {
			ChangeAddControl c = (ChangeAddControl)b;
			c.getCreator().addControl(c.control);
		} else if (b instanceof ChangeRemoveControl) {
			ChangeRemoveControl c = (ChangeRemoveControl)b;
			c.getCreator().getSignature().removeControl(c.getCreator());
		} else if (b instanceof ChangeName) {
			ChangeName c = (ChangeName)b;
			c.getCreator().setName(c.name);
		} else if (b instanceof ChangeKind) {
			ChangeKind c = (ChangeKind)b;
			c.getCreator().setKind(c.kind);
		} else if (b instanceof ChangeAddPort) {
			ChangeAddPort c = (ChangeAddPort)b;
			c.getCreator().addPort(c.port);
			c.port.setName(c.name);
		} else if (b instanceof ChangeRemovePort) {
			ChangeRemovePort c = (ChangeRemovePort)b;
			c.getCreator().getControl().removePort(c.getCreator());
		} else if (b instanceof PortSpec.ChangeName) {
			PortSpec.ChangeName c = (PortSpec.ChangeName)b;
			c.getCreator().setName(c.name);
		} else return false;
		return true;
	}
	
	@Override
	public void dispose() {
		for (Control c : getControls())
			c.dispose();
		getControls().clear();
		controls = null;
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
	 * </ul>
	 */
	@Override
	protected Object getProperty(String name) {
		if (PROPERTY_CONTROL.equals(name)) {
			return getControls();
		} else return super.getProperty(name);
	}
	
	public ChangeAddControl changeAddControl(Control control) {
		return new ChangeAddControl(control);
	}
	
	@Override
	public Object lookup(PropertyScratchpad context, Identifier identifier) {
		if (identifier instanceof Control.Identifier) {
			String name = identifier.getName();
			for (Control c : getControls(context))
				if (c.getName(context).equals(name))
					return c;
		}
		return null;
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