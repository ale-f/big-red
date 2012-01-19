package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.geometry.PointList;

import dk.itu.big_red.model.Control.LongParameterSpec.LongParameter;
import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.Control.ParameterSpec.Parameter;
import dk.itu.big_red.model.interfaces.IChild;
import dk.itu.big_red.model.interfaces.IControl;
import dk.itu.big_red.model.interfaces.INode;
import dk.itu.big_red.model.interfaces.IParent;
import dk.itu.big_red.model.interfaces.ISite;
import dk.itu.big_red.utilities.Lists;
import dk.itu.big_red.utilities.Lists.Pair;
import dk.itu.big_red.utilities.geometry.Geometry;
import dk.itu.big_red.utilities.geometry.Rectangle;

/**
 * 
 * @author alec
 * @see INode
 */
public class Node extends Container implements INode {
	private ArrayList<Port> ports = new ArrayList<Port>();
	private ArrayList<Parameter> parameters = new ArrayList<Parameter>();
	
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
		Control cloneControl = (Control)m.get(control);
		n.setControl(cloneControl == null ? control : cloneControl);
		
		n.setFillColour(getFillColour().getCopy());
		n.setOutlineColour(getOutlineColour().getCopy());
		
		for (Pair<Parameter, Parameter> p :
			Lists.zip(getParameters(), n.getParameters())) {
			if (p.getA() instanceof LongParameter)
				((LongParameter)p.getB()).setValue((Long)p.getA().getValue());
		}
		
		if (m != null) {
			/* Manually claim that the new Node's Ports are clones. */
			for (Pair<Port, Port> p :
				Lists.zip(getPorts(), n.getPorts()))
				m.put(p.getA(), p.getB());
		}
		return n;
	}
	
	private Control control = null;
	
	@Override
	public boolean canContain(Layoutable child) {
		Class<? extends Layoutable> c = child.getClass();
		return (c == Node.class || c == Site.class);
	}
	
	@Override
	public void setLayout(Rectangle layout) {
		if (!control.isResizable())
			layout.setSize(getLayout().getSize());
		fittedPolygon = null;
		super.setLayout(layout);
	}
	
	/**
	 * Returns the {@link Control} of this Node.
	 * @return a Control
	 */
	public Control getControl() {
		return control;
	}

	protected void setControl(Control c) {
		control = c;
		
		setFillColour(control.getFillColour().getCopy());
		setOutlineColour(control.getOutlineColour().getCopy());
		
		ports = control.createPorts();
		for (Port p : ports)
			p.setParent(this);
		
		parameters = control.createParameters();
				
		if (!control.isResizable())
			super.setLayout(
				getLayout().getCopy().setSize(control.getDefaultSize()));
	}
	
	public List<Port> getPorts() {
		return ports;
	}
	
	public List<Parameter> getParameters() {
		return parameters;
	}
	
	public Port getPort(String name) {
		for (Port p : getPorts())
			if (p.getName().equals(name))
				return p;
		return null;
	}
	
	private PointList fittedPolygon = null;
	
	/**
	 * Lazily creates and returns the <i>fitted polygon</i> for this Node (a
	 * copy of its {@link Control}'s polygon, scaled to fit inside this Node's
	 * layout).
	 * 
	 * <p>A call to {@link #setControl} or {@link #setLayout} will invalidate
	 * the fitted polygon.
	 * @return the fitted polygon
	 */
	public PointList getFittedPolygon() {
		if (fittedPolygon == null)
			if (getControl().getShape() == Shape.POLYGON)
				fittedPolygon = Geometry.fitPolygonToRectangle(getControl().getPoints(), getLayout());
		return fittedPolygon;
	}

	@Override
	public IParent getIParent() {
		return (IParent)getParent();
	}

	@Override
	public Iterable<INode> getINodes() {
		return Lists.only(children, INode.class);
	}

	@Override
	public Iterable<Port> getIPorts() {
		return ports;
	}

	@Override
	public Iterable<ISite> getISites() {
		return Lists.only(children, ISite.class);
	}
	
	@Override
	public Iterable<IChild> getIChildren() {
		return Lists.only(children, IChild.class);
	}
	
	@Override
	public IControl getIControl() {
		return control;
	}
	
	@Override
	public UserControl getUserControl() {
		return UserControl.OUTLINE_AND_FILL;
	}
}
