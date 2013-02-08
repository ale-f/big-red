package example.org.bigraph.extensions.scope;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.Container;
import org.bigraph.model.Link;
import org.bigraph.model.Node;
import org.bigraph.model.Point;
import org.bigraph.model.Point.ChangeConnectDescriptor;
import org.bigraph.model.Port;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.changes.descriptors.IDescriptorStepValidator;
import org.bigraph.model.process.IParticipantHost;
import org.bigraph.model.utilities.FilteringIterable;

public class ValidateScope implements IDescriptorStepValidator {
	@Override
	public final void setHost(IParticipantHost host) {
		/* do nothing */
	}
	
	private static boolean isScoped(PropertyScratchpad context, Port p) {
		return Scope.isScoped(context, p.getSpec());
	}
	
	private static void checkAll(
			IChangeDescriptor cd, PropertyScratchpad context, Port original,
			Iterable<? extends Port> ps)
			throws ChangeCreationException {
		Node n = original.getParent(context);
		for (Port p : ps) {
			if (isScoped(context, p) && p != original)
				throw new ChangeCreationException(cd,
						"Scope problem: Two scoped ports can't be connected " +
						"to the same link");
			Container parent = p.getParent(context);
			while (parent != null) {
				if (parent != n) {
					parent = parent.getParent(context);
				} else break;
			}
			if (parent == null)
				throw new ChangeCreationException(cd,
						"Scope problem: " + p + " is not a child of " + n);
		}
	}
	
	@Override
	public boolean tryValidateChange(Process context, IChangeDescriptor change)
			throws ChangeCreationException {
		final PropertyScratchpad scratch = context.getScratch();
		final Resolver resolver = context.getResolver();
		if (change instanceof ChangeConnectDescriptor) {
			ChangeConnectDescriptor cd = (ChangeConnectDescriptor)change;
			Point point = cd.getPoint().lookup(scratch, resolver);
			Link link = cd.getLink().lookup(scratch, resolver);
			
			List<Point> l = new ArrayList<Point>(link.getPoints(scratch));
			l.add(point);
			
			Iterable<? extends Port> ps =
					new FilteringIterable<Port>(Port.class, l);
			for (Port i : ps) {
				if (isScoped(scratch, i)) {
					checkAll(cd, scratch, i, ps);
					break;
				}
			}
		} else return false;
		return true;
	}
}
