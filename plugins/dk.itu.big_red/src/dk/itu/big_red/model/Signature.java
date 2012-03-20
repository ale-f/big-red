package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;

import dk.itu.big_red.model.assistants.SignatureChangeValidator;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.IChangeable;
import dk.itu.big_red.model.interfaces.ISignature;

/**
 * The Signature is a central storage point for {@link Control}s and their
 * properties (both in terms of the bigraph model and their visual
 * representations). Every {@link Bigraph} has an associated Signature, which
 * they consult whenever they need to create a {@link Node}.
 * @author alec
 * @see ISignature
 */
public class Signature extends ModelObject implements ISignature, IChangeable {
	public static final String[] EMPTY_STRING_ARRAY = new String[]{};
	
	private ArrayList<Control> controls = new ArrayList<Control>();
	
	public Signature() {
	}
	
	@Override
	public Signature clone(Map<ModelObject, ModelObject> m) {
		Signature s = (Signature)super.clone(m);
		s.setFile(getFile());
		
		for (Control c : getControls())
			s.addControl(c.clone(m));
		
		return s;
	}
	
	/**
	 * The property name fired when a control is added or removed. The property
	 * values are {@link Control}s.
	 */
	public static final String PROPERTY_CONTROL = "SignatureControl";
	
	public Control addControl(Control c) {
		controls.add(c);
		firePropertyChange(PROPERTY_CONTROL, null, c);
		return c;
	}
	
	public void removeControl(Control m) {
		if (controls.contains(m)) {
			controls.remove(m);
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

	private void doChange(Change b) {
		if (b instanceof ChangeGroup) {
			ChangeGroup c = (ChangeGroup)b;
			for (Change i : c)
				doChange(i);
		} else if (b instanceof Colourable.ChangeFillColour) {
			Colourable.ChangeFillColour c = (Colourable.ChangeFillColour)b;
			c.getCreator().setFillColour(c.newColour);
		} else if (b instanceof Colourable.ChangeOutlineColour) {
			Colourable.ChangeOutlineColour c = (Colourable.ChangeOutlineColour)b;
			c.getCreator().setOutlineColour(c.newColour);
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
	public Object getProperty(String name) {
		if (PROPERTY_CONTROL.equals(name)) {
			return getControls();
		} else return super.getProperty(name);
	}
	
	@Override
	public Signature setFile(IFile file) {
		return (Signature)super.setFile(file);
	}
}