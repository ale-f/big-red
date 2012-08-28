package org.bigraph.model;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.interfaces.INode;
import org.bigraph.model.interfaces.IPort;

/**
 * Ports are one of the two kinds of object that can be connected by an
 * {@link Edge} (the other being the {@link InnerName}). Ports are only ever found
 * on a {@link Node}, and inherit their name from a {@link Control}.
 * @author alec
 */
public class Port extends Point implements IPort {
	private final PortSpec spec;
	
	@Override
	public Port newInstance() {
		return new Port(getSpec());
	}
	
	public PortSpec getSpec() {
		return spec;
	}
	
	public Port(PortSpec spec) {
		this.spec = spec;
	}
	
	@Override
	public Node getParent() {
		return (Node)super.getParent();
	}
	
	@Override
	public Node getParent(PropertyScratchpad context) {
		return (Node)super.getParent(context);
	}
	
	@Override
	public String getName() {
		return getSpec().getName();
	}

	@Override
	public INode getNode() {
		return getParent();
	}
	
	public static final class Identifier extends Point.Identifier {
		private final Node.Identifier node;
		
		public Identifier(String name, Node.Identifier node) {
			super(name);
			this.node = node;
		}
		
		public Node.Identifier getNode() {
			return node;
		}
		
		@Override
		public Port lookup(PropertyScratchpad context, Resolver r) {
			Node n = getNode().lookup(context, r);
			if (n != null) {
				Port p = n.getPort(getName());
				return (equals(p.getIdentifier(context)) ? p : null);
			} else return null;
		}
		
		@Override
		public Identifier getRenamed(String name) {
			return new Identifier(name, getNode());
		}
		
		@Override
		public boolean equals(Object obj_) {
			if (safeClassCmp(this, obj_)) {
				Identifier obj = (Identifier)obj_;
				return
						safeEquals(getName(), obj.getName()) &&
						safeEquals(getNode(), obj.getNode());
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return compositeHashCode(Identifier.class, getName(), getNode());
		}
		
		@Override
		public String toString() {
			return "port " + getName() + " of " + node;
		}
	}
	
	@Override
	public Identifier getIdentifier() {
		return getIdentifier(null);
	}
	
	@Override
	public Identifier getIdentifier(PropertyScratchpad context) {
		return new Identifier(getName(),
				getParent(context).getIdentifier(context));
	}
}
