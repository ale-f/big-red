package org.bigraph.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.interfaces.IChild;
import org.bigraph.model.interfaces.INode;
import org.bigraph.model.interfaces.IParent;

/**
 * @author alec
 * @see INode
 */
public class Node extends Container implements INode {
	private ArrayList<Port> ports = new ArrayList<Port>();
	
	/**
	 * Returns a new {@link Node} with the same {@link Control} as this one.
	 */
	@Override
	public Node newInstance() {
		try {
			return new Node(control);
		} catch (Exception e) {
			return null;
		}
	}

	public Node(Control control) {
		setControl(control);
	}
	
	@Override
	protected Node clone(Bigraph m) {
		Node n = (Node)super.clone(m);
		n.setControl(getControl().getIdentifier().lookup(null, m));
		return n;
	}
	
	private Control control = null;
	
	/**
	 * Returns the {@link Control} of this Node.
	 * @return a Control
	 */
	@Override
	public Control getControl() {
		return control;
	}

	protected void setControl(Control c) {
		control = c;
		
		ports = control.createPorts();
		for (Port p : ports)
			p.setParent(this);
	}
	
	@Override
	public List<? extends Port> getPorts() {
		/* The order of ports is important, so this method should return a List
		 * rather than a bare Collection */
		return ports;
	}
	
	public Port getPort(String name) {
		for (Port p : getPorts())
			if (p.getName().equals(name))
				return p;
		return null;
	}

	@Override
	public IParent getIParent() {
		return (IParent)getParent();
	}

	@Override
	public Collection<? extends Node> getNodes() {
		return only(null, Node.class);
	}

	@Override
	public Collection<? extends Site> getSites() {
		return only(null, Site.class);
	}
	
	@Override
	public Collection<? extends IChild> getIChildren() {
		return only(null, IChild.class);
	}
	
	public static final class Identifier extends Container.Identifier {
		private final Control.Identifier control;
		
		public Identifier(String name, Control.Identifier control) {
			super(name);
			this.control = control;
		}
		
		public Control.Identifier getControl() {
			return control;
		}
		
		@Override
		public Node lookup(PropertyScratchpad context, Resolver r) {
			Node n = require(r.lookup(context, this), Node.class);
			if (n != null && equals(n.getIdentifier(context))) {
				return n;
			} else return null;
		}
		
		@Override
		public Identifier getRenamed(String name) {
			return new Node.Identifier(name, getControl());
		}
		
		@Override
		public boolean equals(Object obj_) {
			if (safeClassCmp(this, obj_)) {
				Identifier obj = (Identifier)obj_;
				return
						safeEquals(getName(), obj.getName()) &&
						safeEquals(getControl(), obj.getControl());
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			/* To ensure that the contract for hashCode() isn't violated,
			 * don't use getControl() in this method */
			return compositeHashCode(Identifier.class, getName());
		}
		
		@Override
		public String toString() {
			return getControl().getName() + " " + getName();
		}
	}
	
	@Override
	public Identifier getIdentifier() {
		return getIdentifier(null);
	}
	
	@Override
	public Identifier getIdentifier(PropertyScratchpad context) {
		return new Identifier(getName(context),
				getControl().getIdentifier(context));
	}
	
	@Override
	public void dispose() {
		if (ports != null) {
			for (Port p : ports)
				p.dispose();
			ports.clear();
			ports = null;
		}
		
		super.dispose();
	}
}
