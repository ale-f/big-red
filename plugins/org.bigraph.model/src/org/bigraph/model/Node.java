package org.bigraph.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bigraph.model.ModelObject;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.interfaces.IChild;
import org.bigraph.model.interfaces.INode;
import org.bigraph.model.interfaces.IParent;

/**
 * 
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
	public Node clone(Map<ModelObject, ModelObject> m) {
		Node n = (Node)super.clone(m);
		
		/*
		 * If this Node's Control has a counterpart in the map, then use that
		 * (the Bigraph is probably being cloned).
		 */
		Control cloneControl = null;
		if (m != null)
			cloneControl = (Control)m.get(getControl());
		n.setControl(cloneControl == null ? getControl() : cloneControl);
		
		if (m != null) {
			/* Manually claim that the new Node's Ports are clones. */
			for (int i = 0; i < getPorts().size(); i++)
				m.put(getPorts().get(i), n.getPorts().get(i));
		}
		return n;
	}
	
	private Control control = null;
	
	@Override
	public boolean canContain(Layoutable child) {
		Class<? extends Layoutable> c = child.getClass();
		return (c == Node.class || c == Site.class);
	}
	
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
	public List<Port> getPorts() {
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
	public List<Node> getNodes() {
		return only(null, Node.class);
	}

	@Override
	public List<Site> getSites() {
		return only(null, Site.class);
	}
	
	@Override
	public List<IChild> getIChildren() {
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
		public Node lookup(Bigraph universe, PropertyScratchpad context) {
			Node n = (Node)
					universe.getNamespace(Node.class).get(context, getName());
			/* perform a control check */
			return n;
		}
		
		@Override
		public String toString() {
			return "node " + getName() + " (of " + control + ")";
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
}
