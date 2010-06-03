package dk.itu.big_red.figure.adornments;



import org.eclipse.draw2d.AbstractConnectionAnchor;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.EllipseAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;

import dk.itu.big_red.figure.AbstractFigure;
import dk.itu.big_red.figure.NodeFigure;
import dk.itu.big_red.model.Thing;
import dk.itu.big_red.model.Node;



public class PortAnchor extends AbstractConnectionAnchor {
	/*
	 * The PortAnchor is a slightly mad version of ChopboxAnchor. It implements
	 * four different kinds of behaviour:
	 * 
	 * * if its owning figure is a rectangle, it's exactly equivalent to a
	 *   ChopboxAnchor;
	 * * if its owning figure is an ellipse, it's exactly equivalent to an
	 *   EllipseAnchor;
	 * * if it's been overriden to attach to a specific point, then it'll do
	 *   that (useful for ports); and
	 * * if its owning figure is some kind of sophisticated polygon, it
	 *   performs inordinately complicated mathematics to work out which point
	 *   on the polygon it should attach to.
	 */

	private ChopboxAnchor ca = new ChopboxAnchor() {};
	private EllipseAnchor ea = new EllipseAnchor();
	private Thing model = null;
	private String key = null;
	
	public PortAnchor(IFigure owner, Thing model, String key) {
		/*
		 * No need to call super() - it just calls setOwner anyway. (Besides,
		 * doing it would sabotage the initialisation of ca and ea; something
		 * else I didn't know about Java!)
		 */
		setKey(key);
		setModel(model);
		setOwner(owner);
	}
	
	public void setOwner(IFigure owner) {
		super.setOwner(owner);
		ca.setOwner(owner); ea.setOwner(owner);
	}
	
	@Override
	public Point getLocation(Point reference) {
		if (model != null && key != null) {
			Point p = new Point(((AbstractFigure)getOwner()).getLocation());
			p.translate(((Node)model).getPortAnchorPosition(key));
			getOwner().translateToAbsolute(p);
			return p;
		} else if (!(getOwner() instanceof NodeFigure)) {
			return ca.getLocation(reference);
		} else {
			NodeFigure o = (NodeFigure)getOwner();
			switch (o.getShape()) {
			case SHAPE_OVAL:
				return ea.getLocation(reference);
			case SHAPE_RECTANGLE:
				return ca.getLocation(reference);
			case SHAPE_TRIANGLE:
				return ca.getLocation(reference);
			default:
				return ca.getLocation(reference);
			}
		}
	}

	public Thing getModel() {
		return model;
	}

	public void setModel(Thing model) {
		this.model = model;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}