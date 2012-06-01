package dk.itu.big_red.model.assistants;

import java.util.ArrayList;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Control.Kind;
import dk.itu.big_red.model.ModelObject.ChangeExtendedData;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.ChangeValidator;
import dk.itu.big_red.model.names.Namespace;
import dk.itu.big_red.model.names.policies.INamePolicy;
import dk.itu.big_red.model.names.policies.PositiveIntegerNamePolicy;

/**
 * The <strong>BigraphIntegrityValidator</strong> is the basic validator that
 * all changes to {@link Bigraph}s must go through; it checks for both model
 * consistency and visual sensibleness.
 * @author alec
 *
 */
public class BigraphIntegrityValidator extends ChangeValidator<Bigraph> {
	private final PropertyScratchpad scratch;
	private Change activeChange = null;
	
	public BigraphIntegrityValidator(Bigraph changeable) {
		super(changeable);
		scratch = new PropertyScratchpad();
	}
	
	private ArrayList<Layoutable> layoutChecks =
		new ArrayList<Layoutable>();
	
	protected void rejectChange(String rationale)
			throws ChangeRejectedException {
		super.rejectChange(activeChange, rationale);
	}
	
	private void runLayoutChecks() throws ChangeRejectedException {
		for (Layoutable i : layoutChecks) {
			Container parent = i.getParent(scratch);
			Rectangle layout = ExtendedDataUtilities.getLayout(scratch, i);
			checkObjectCanContain(parent, layout);
			if (i instanceof Container)
				checkLayoutCanContainChildren((Container)i, layout);
		}
	}
	
	private void checkObjectCanContain(Layoutable o, Rectangle nl) throws ChangeRejectedException {
		if (o != null && !(o instanceof Bigraph)) {
			Rectangle tr =
				ExtendedDataUtilities.getLayout(scratch, o).getCopy().setLocation(0, 0);
			if (!tr.contains(nl))
				rejectChange(
					"The object can no longer fit into its container");
		}
	}
	
	private void checkLayoutCanContainChildren(Container c, Rectangle nl) throws ChangeRejectedException {
		nl = nl.getCopy().setLocation(0, 0);
		for (Layoutable i : c.getChildren()) {
			Rectangle layout = ExtendedDataUtilities.getLayout(scratch, i);
			if (!nl.contains(layout))
				rejectChange("The object is no longer big enough to accommodate its children");
		}
	}
	
	private void checkEligibility(Layoutable... l) throws ChangeRejectedException {
		for (Layoutable i : l)
			if (i.getBigraph(scratch) != getChangeable())
				rejectChange(i + " is not part of this Bigraph");
	}
	
	@Override
	public void tryValidateChange(Change b)
			throws ChangeRejectedException {
		activeChange = b;
		
		scratch.clear();
		
		layoutChecks.clear();
		
		_tryValidateChange(b);
		
		runLayoutChecks();
		
		activeChange = null;
	}
	
	private void checkName(Change b, Layoutable l, String cdt) throws ChangeRejectedException {
		if (cdt == null)
			rejectChange(b, "Setting an object's name to null is no longer supported");
		Namespace<Layoutable> ns =
				getChangeable().getNamespace(Bigraph.getNSI(l));
		if (ns == null)
			return; /* not subject to any checks */
		if (ns.get(scratch, cdt) != null)
			if (!ns.get(scratch, cdt).equals(l))
				rejectChange("Names must be unique");
		if (ns.getPolicy().normalise(cdt) == null)
			rejectChange(b, "\"" + cdt + "\" is not a valid name for " + l);
	}
	
	protected void _tryValidateChange(Change b)
			throws ChangeRejectedException {
		if (!b.isReady()) {
			rejectChange("The Change is not ready");
		} else if (b instanceof ChangeGroup) {
			for (Change c : (ChangeGroup)b)
				_tryValidateChange(c);
		} else if (b instanceof Point.ChangeConnect) {
			Point.ChangeConnect c = (Point.ChangeConnect)b;
			checkEligibility(c.link, c.getCreator());
			if (c.getCreator().getLink(scratch) != null)
				rejectChange(b,
					"Connections can only be established to Points that " +
					"aren't already connected");
			c.link.addPoint(scratch, c.getCreator());
		} else if (b instanceof Point.ChangeDisconnect) {
			Point.ChangeDisconnect c = (Point.ChangeDisconnect)b;
			checkEligibility(c.getCreator());
			Link l = c.getCreator().getLink(scratch);
			if (l == null)
				rejectChange("The Point is already disconnected");
			l.removePoint(scratch, c.getCreator());
		} else if (b instanceof Container.ChangeAddChild) {
			Container.ChangeAddChild c = (Container.ChangeAddChild)b;
			
			if (c.getCreator() instanceof Node &&
				((Node)c.getCreator()).getControl().getKind() == Kind.ATOMIC)
				rejectChange(
						((Node)c.getCreator()).getControl().getName() +
						" is an atomic control");
			
			checkName(b, c.child, c.name);

			if (c.child instanceof Edge) {
				if (!(c.getCreator() instanceof Bigraph))
					rejectChange("Edges must be children of the top-level Bigraph");
			} else {
				if (c.child instanceof Container)
					if (((Container)c.child).getChildren(scratch).size() != 0)
						rejectChange(b, c.child + " already has child objects");
				if (!c.getCreator().canContain(c.child))
					rejectChange(b,
						c.getCreator().getType() + "s can't contain " +
						c.child.getType() + "s");
				if (!layoutChecks.contains(c.child))
					layoutChecks.add(c.child);
			}
			
			c.getCreator().addChild(scratch, c.child, c.name);
		} else if (b instanceof Layoutable.ChangeRemove) {
			Layoutable.ChangeRemove c = (Layoutable.ChangeRemove)b;
			Layoutable ch = c.getCreator();
			checkEligibility(ch);
			if (ch instanceof Container)
				if (((Container)ch).getChildren(scratch).size() != 0)
					rejectChange(b, ch + " has child objects which must be removed first");
			Container cp = ch.getParent(scratch);
			if (cp == null)
				rejectChange(b, cp + " is not the parent of " + ch);
			cp.removeChild(scratch, ch);
			getChangeable().getNamespace(Bigraph.getNSI(ch)).
				remove(scratch, ch.getName());
		} else if (b instanceof ModelObject.ChangeExtendedData) {
			ChangeExtendedData c = (ChangeExtendedData)b;
			if (c.validator != null) {
				String rationale = c.validator.validate(c, scratch);
				if (rationale != null)
					rejectChange(rationale);
			}
			scratch.setProperty(c.getCreator(), c.key, c.newValue);
		} else if (b instanceof Layoutable.ChangeName) {
			Layoutable.ChangeName c = (Layoutable.ChangeName)b;
			checkEligibility(c.getCreator());
			checkName(b, c.getCreator(), c.newName);
			c.getCreator().setName(scratch, c.newName);
		} else {
			rejectChange("The change was not recognised by the validator");
		}
	}
}
