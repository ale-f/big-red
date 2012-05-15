package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.interfaces.IChild;
import dk.itu.big_red.model.interfaces.INode;
import dk.itu.big_red.model.interfaces.IParent;

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
	
	@Override
	protected void setLayout(Rectangle layout) {
		fittedPolygon = null;
		super.setLayout(layout);
	}
	
	private static final PointList fitPolygon(Node n) {
		Control c = n.getControl();
		Rectangle rectangle = n.getLayout();
		Object shape = ExtendedDataUtilities.getShape(c);
		if (!(shape instanceof PointList))
			return null;
		PointList fittedPolygon = ((PointList)shape).getCopy();

		/*
		 * Move the polygon so that its top-left corner is at (0,0).
		 */
		fittedPolygon.translate(
				fittedPolygon.getBounds().getTopLeft().getNegated());
		
		/*
		 * Work out the scaling factors that'll make the polygon fit inside
		 * the layout.
		 * 
		 * (Note that adjustedBounds.width and adjustedBounds.height are
		 * both off-by-one - getBounds() prefers < to <=, it seems.)
		 */
		Rectangle adjustedBounds = new Rectangle(fittedPolygon.getBounds());
		double xScale = rectangle.width() - 2,
		       yScale = rectangle.height() - 2;
		xScale /= adjustedBounds.width() - 1;
		yScale /= adjustedBounds.height() - 1;
		
		/*
		 * Scale all of the points.
		 */
		Point tmp = Point.SINGLETON;
		for (int i = 0; i < fittedPolygon.size(); i++) {
			fittedPolygon.getPoint(tmp, i).
				scale(xScale, yScale).translate(1, 1);
			fittedPolygon.setPoint(tmp, i);
		}
		
		return fittedPolygon;
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
			fittedPolygon = fitPolygon(this);
		return fittedPolygon;
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
		
		fittedPolygon = null;
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
	
	public Change changeParameter(String parameter) {
		return ExtendedDataUtilities.changeParameter(this, parameter);
	}
}
