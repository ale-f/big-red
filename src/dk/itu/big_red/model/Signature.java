package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;

import dk.itu.big_red.model.assistants.SignatureChangeValidator;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.IChangeable;
import dk.itu.big_red.model.interfaces.ISignature;
import dk.itu.big_red.util.resources.IFileBackable;

/**
 * The Signature is a central storage point for {@link Control}s and their
 * properties (both in terms of the bigraph model and their visual
 * representations). Every {@link Bigraph} has an associated Signature, which
 * they consult whenever they need to create a {@link Node}.
 * @author alec
 * @see ISignature
 */
public class Signature extends ModelObject implements ISignature, IChangeable, IFileBackable {
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
			if (c.getName().equals(name))
				return c;
		return null;
	}
	
	public List<Control> getControls() {
		return controls;
	}

	@Override
	public Iterable<Control> getIControls() {
		return controls;
	}

	private SignatureChangeValidator validator =
		new SignatureChangeValidator(this);
	
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
	
	private IFile file = null;
	
	@Override
	public IFile getFile() {
		return file;
	}

	@Override
	public Signature setFile(IFile file) {
		this.file = file;
		return this;
	}
}