package org.bigraph.model;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.interfaces.IEdge;

/**
  * An Edge is a connection which connects any number of {@link Port}s and
  * {@link InnerName}s. (An Edge which "connects" only one point is perfectly
  * legitimate.)
  * <p>Note that Edges represent the <i>bigraphical</i> concept of an edge
  * rather than a GEF/GMF {@link Connection}, and so they lack any concept of a
  * "source" or "target"; the Edge is always the target for a connection, and
  * {@link Point}s are always sources.
  * @author alec
  * @see IEdge
  */
public class Edge extends Link implements IEdge {
	public static final class Identifier extends Link.Identifier {
		public Identifier(String name) {
			super(name);
		}
		
		@Override
		public Edge lookup(PropertyScratchpad context, Resolver r) {
			return require(super.lookup(context, r), Edge.class);
		}
		
		@Override
		public Identifier getRenamed(String name) {
			return new Identifier(name);
		}
		
		@Override
		public String toString() {
			return "edge " + getName();
		}
	}
	
	@Override
	public Identifier getIdentifier() {
		return getIdentifier(null);
	}
	
	@Override
	public Identifier getIdentifier(PropertyScratchpad context) {
		return new Identifier(getName(context));
	}
}
