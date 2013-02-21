package dk.itu.big_red.editors.bigraph.commands;

import java.util.ArrayList;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Port;
import org.bigraph.model.assistants.BigraphOperations;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import dk.itu.big_red.editors.bigraph.parts.LinkPart;

public class ModelDeleteCommand extends ChangeCommand {
	private ChangeDescriptorGroup cg = new ChangeDescriptorGroup();
	
	public ModelDeleteCommand() {
		setChange(cg);
	}
	
	private ArrayList<Object> objects = new ArrayList<Object>();
	
	public void addObject(Object m) {
		if (m != null && !(m instanceof Bigraph) && !(m instanceof Port)) {
			objects.add(m);
			if (m instanceof LinkPart.Connection) {
				setTarget(((LinkPart.Connection)m).getLink().getBigraph());
			} else if (m instanceof Layoutable) {
				setTarget(((Layoutable)m).getBigraph());
			}
		}
	}

	private PropertyScratchpad scratch = null;

	public void setTarget(Bigraph target) {
		super.setContext(target);
		if (scratch == null)
			scratch = new PropertyScratchpad();
	}
	
	@Override
	public void prepare() {
		cg.clear();
		if (scratch != null)
			scratch.clear();
		for (Object m : objects) {
			if (m instanceof LinkPart.Connection) {
				BigraphOperations.disconnectPoint(
						cg, scratch, ((LinkPart.Connection)m).getPoint());
			} else if (m instanceof Layoutable) {
				BigraphOperations.removeObject(cg, scratch, (Layoutable)m);
			}
		}
	}
}
