package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;

import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.Control.ParameterSpec.Parameter;
import dk.itu.big_red.model.interfaces.IChild;
import dk.itu.big_red.model.interfaces.IControl;
import dk.itu.big_red.model.interfaces.INode;
import dk.itu.big_red.model.interfaces.IParent;
import dk.itu.big_red.model.interfaces.ISite;
import dk.itu.big_red.utilities.Lists;
import dk.itu.big_red.utilities.geometry.ReadonlyRectangle;
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
		
		/* copy parameters */
		
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
	
	private void fitPolygon() {
		PointList points = getControl().getPoints();
		if (points == null)
			return;
		ReadonlyRectangle rectangle = getLayout();
		fittedPolygon = points.getCopy();

		/*
		 * Move the polygon so that its top-left corner is at (0,0).
		 */
		fittedPolygon.translate(
				points.getBounds().getTopLeft().getNegated());
		
		/*
		 * Work out the scaling factors that'll make the polygon fit inside
		 * the layout.
		 * 
		 * (Note that adjustedBounds.width and adjustedBounds.height are
		 * both off-by-one - getBounds() prefers < to <=, it seems.)
		 */
		Rectangle adjustedBounds = new Rectangle(fittedPolygon.getBounds());
		double xScale = rectangle.getWidth() - 2,
		       yScale = rectangle.getHeight() - 2;
		xScale /= adjustedBounds.getWidth() - 1;
		yScale /= adjustedBounds.getHeight() - 1;
		
		/*
		 * Scale all of the points.
		 */
		Point tmp = Point.SINGLETON;
		for (int i = 0; i < fittedPolygon.size(); i++) {
			fittedPolygon.getPoint(tmp, i).
				scale(xScale, yScale).translate(1, 1);
			fittedPolygon.setPoint(tmp, i);
		}
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
				fitPolygon();
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
}
