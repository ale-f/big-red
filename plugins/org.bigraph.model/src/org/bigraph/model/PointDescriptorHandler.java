package org.bigraph.model;

import org.bigraph.model.Point.ChangeConnectDescriptor;
import org.bigraph.model.Point.ChangeDisconnectDescriptor;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

final class PointDescriptorHandler
		extends HandlerUtilities.DescriptorHandlerImpl {
	@Override
	public boolean tryValidateChange(Process context, IChangeDescriptor change)
			throws ChangeCreationException {
		final Resolver resolver = context.getResolver();
		final PropertyScratchpad scratch = context.getScratch();
		if (change instanceof ChangeConnectDescriptor) {
			ChangeConnectDescriptor co = (ChangeConnectDescriptor)change;
			Point p = tryLookup(co,
					co.getPoint(), scratch, resolver, Point.class);
			tryLookup(co, co.getLink(), scratch, resolver, Link.class);
			
			Link currentLink = p.getLink(scratch);
			if (currentLink != null)
				throw new ChangeCreationException(co,
						"" + p.toString(scratch) +
						" is already connected to " +
								currentLink.toString(scratch));
		} else if (change instanceof ChangeDisconnectDescriptor) {
			ChangeDisconnectDescriptor co = (ChangeDisconnectDescriptor)change;
			Point p = co.getPoint().lookup(scratch, resolver);
			Link l = co.getLink().lookup(scratch, resolver);
			
			if (p == null)
				throw new ChangeCreationException(co,
						"" + co.getPoint() + " didn't resolve to a Point");
			if (l == null)
				throw new ChangeCreationException(co,
						"" + co.getLink() + " didn't resolve to a Link");

			Link currentLink = p.getLink(scratch);
			if (currentLink == null ||
					!currentLink.getIdentifier(scratch).equals(co.getLink())) {
				throw new ChangeCreationException(co,
						"" + co.getPoint() + " isn't connected to " +
						co.getLink());
			}
		} else return false;
		return true;
	}

	@Override
	public boolean executeChange(Resolver r, IChangeDescriptor change) {
		if (change instanceof ChangeConnectDescriptor) {
			ChangeConnectDescriptor co = (ChangeConnectDescriptor)change;
			co.getLink().lookup(null, r).addPoint(
					co.getPoint().lookup(null, r));
		} else if (change instanceof ChangeDisconnectDescriptor) {
			ChangeDisconnectDescriptor co = (ChangeDisconnectDescriptor)change;
			co.getLink().lookup(null, r).removePoint(
					co.getPoint().lookup(null, r));
		} else return false;
		return true;
	}

}
