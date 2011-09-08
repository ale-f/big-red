package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.List;

import dk.itu.big_red.model.interfaces.IControl;
import dk.itu.big_red.model.interfaces.ISignature;
import dk.itu.big_red.util.HomogeneousIterable;

/**
 * The Signature is a central storage point for {@link Control}s and their
 * properties (both in terms of the bigraph model and their visual
 * representations). Every {@link Bigraph} has an associated Signature, which
 * they consult whenever they need to create a {@link Node}.
 * @author alec
 * @see ISignature
 */
public class Signature implements ISignature {
	public static final String[] EMPTY_STRING_ARRAY = new String[]{};
	
	private ArrayList<Control> controls = new ArrayList<Control>();
	
	public Signature() {
	}
	
	public Control addControl(Control c) {
		controls.add(c);
		return c;
	}
	
	public void removeControl(Control m) {
		if (controls.contains(m))
			controls.remove(m);
	}
	
	public Control getControl(String name) {
		for (Control c : controls)
			if (c.getLongName().equals(name))
				return c;
		return null;
	}

	public String[] getControlNames() {
		ArrayList<String> controlNames = new ArrayList<String>();
		for (Control c : controls)
			controlNames.add(c.getLongName());
		return controlNames.toArray(EMPTY_STRING_ARRAY);
	}
	
	public List<Control> getControls() {
		return controls;
	}

	@Override
	public Iterable<IControl> getIControls() {
		return new HomogeneousIterable<IControl>(controls, IControl.class);
	}
}