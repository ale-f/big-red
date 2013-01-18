package example.org.bigraph.extensions.scope;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.Container;
import org.bigraph.model.Node;
import org.bigraph.model.Point;
import org.bigraph.model.Point.ChangeConnect;
import org.bigraph.model.Port;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IStepValidator;
import org.bigraph.model.process.IParticipantHost;
import org.bigraph.model.utilities.FilteringIterable;

public class ValidateScope implements IStepValidator {
	@Override
	public final void setHost(IParticipantHost host) {
		/* do nothing */
	}
	
	private static boolean isScoped(PropertyScratchpad context, Port p) {
		return Scope.isScoped(context, p.getSpec());
	}
	
	private static void checkAll(
			IChange ch, PropertyScratchpad context, Port original,
			Iterable<? extends Port> ps)
			throws ChangeRejectedException {
		Node n = original.getParent(context);
		for (Port p : ps) {
			if (isScoped(context, p) && p != original)
				throw new ChangeRejectedException(ch,
						"Scope problem: Two scoped ports can't be connected " +
						"to the same link");
			Container parent = p.getParent(context);
			while (parent != null) {
				if (parent != n) {
					parent = parent.getParent(context);
				} else break;
			}
			if (parent == null)
				throw new ChangeRejectedException(ch,
						"Scope problem: " + p + " is not a child of " + n);
		}
	}
	
	@Override
	public boolean tryValidateChange(Process context, IChange change_)
			throws ChangeRejectedException {
		final PropertyScratchpad scratch = context.getScratch();
		if (change_ instanceof ChangeConnect) {
			ChangeConnect change = (ChangeConnect)change_;
			Point p = change.getCreator();
			
			List<Point> l = new ArrayList<Point>(
					change.link.getPoints(scratch));
			l.add(p);
			
			Iterable<? extends Port> ps =
					new FilteringIterable<Port>(Port.class, l);
			for (Port i : ps) {
				if (isScoped(scratch, i)) {
					checkAll(change, scratch, i, ps);
					break;
				}
			}
		} else return false;
		return true;
	}
}
