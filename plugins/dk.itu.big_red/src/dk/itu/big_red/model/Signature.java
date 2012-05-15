package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dk.itu.big_red.model.Control.ChangeAddPort;
import dk.itu.big_red.model.Control.ChangeKind;
import dk.itu.big_red.model.Control.ChangeName;
import dk.itu.big_red.model.Control.ChangeRemovePort;
import dk.itu.big_red.model.assistants.IPropertyProviderProxy;
import dk.itu.big_red.model.assistants.PropertyScratchpad;
import dk.itu.big_red.model.assistants.RedProperty;
import dk.itu.big_red.model.assistants.SignatureChangeValidator;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.IChangeExecutor;
import dk.itu.big_red.model.interfaces.ISignature;

/**
 * The Signature is a central storage point for {@link Control}s and their
 * properties (both in terms of the bigraph model and their visual
 * representations). Every {@link Bigraph} has an associated Signature, which
 * they consult whenever they need to create a {@link Node}.
 * @author alec
 * @see ISignature
 */
public class Signature extends ModelObject implements ISignature, IChangeExecutor {
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
		
		public Control control;
		
		public SignatureChange(Control control) {
			this.control = control;
		}
		
		@Override
		public boolean isReady() {
			return (control != null);
		}
	}
	
	public class ChangeAddControl extends SignatureChange {
		public ChangeAddControl(Control control) {
			super(control);
		}

		@Override
		public ChangeRemoveControl inverse() {
			return new ChangeRemoveControl(control);
		}
		
		@Override
		public String toString() {
			return "Change(add control " + control + " to signature " +
					getCreator();
		}
	}
	
	public class ChangeRemoveControl extends SignatureChange {
		public ChangeRemoveControl(Control control) {
			super(control);
		}

		@Override
		public ChangeAddControl inverse() {
			return new ChangeAddControl(control);
		}
		
		@Override
		public String toString() {
			return "Change(remove control " + control + " from signature " +
					getCreator();
		}
	}
	
	private ArrayList<Control> controls = new ArrayList<Control>();
	
	@Override
	public Signature clone(Map<ModelObject, ModelObject> m) {
		Signature s = (Signature)super.clone(m);
		
		for (Control c : getControls())
			s.addControl(c.clone(m));
		
		return s;
	}
	
	protected void addControl(Control c) {
		if (controls.add(c)) {
			c.setSignature(this);
			firePropertyChange(PROPERTY_CONTROL, null, c);
		}
	}
	
	public void addControl(PropertyScratchpad context, Control c) {
		context.<Control>getModifiableList(
				this, PROPERTY_CONTROL, getControls()).add(c);
		context.setProperty(c, Control.PROPERTY_SIGNATURE, this);
	}
	
	protected void removeControl(Control m) {
		if (controls.remove(m)) {
			m.setSignature(null);
			firePropertyChange(PROPERTY_CONTROL, m, null);
		}
	}
	
	public void removeControl(PropertyScratchpad context, Control c) {
		context.<Control>getModifiableList(
				this, PROPERTY_CONTROL, getControls()).remove(c);
		context.setProperty(c, Control.PROPERTY_SIGNATURE, null);
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
	public List<Control> getControls(IPropertyProviderProxy context) {
		return (List<Control>)getProperty(context, PROPERTY_CONTROL);
	}
	
	private SignatureChangeValidator validator =
		new SignatureChangeValidator(this);

	public static final String CONTENT_TYPE = "dk.itu.big_red.signature";
	
	@Override
	public void tryValidateChange(Change b) throws ChangeRejectedException {
		validator.tryValidateChange(b);
	}
	
	@Override
	public void tryApplyChange(Change b) throws ChangeRejectedException {
		tryValidateChange(b);
		doChange(b);
	}

	@Override
	protected void doChange(Change b) {
		super.doChange(b);
		if (b instanceof ChangeAddControl) {
			ChangeAddControl c = (ChangeAddControl)b;
			c.getCreator().addControl(c.control);
		} else if (b instanceof ChangeRemoveControl) {
			ChangeRemoveControl c = (ChangeRemoveControl)b;
			c.getCreator().removeControl(c.control);
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
			c.getCreator().removePort(c.port);
		} else if (b instanceof PortSpec.ChangeName) {
			PortSpec.ChangeName c = (PortSpec.ChangeName)b;
			c.getCreator().setName(c.name);
		}
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
	
	public ChangeRemoveControl changeRemoveControl(Control control) {
		return new ChangeRemoveControl(control);
	}
}