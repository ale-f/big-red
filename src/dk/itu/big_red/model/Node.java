package dk.itu.big_red.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.RGB;

import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.NamespaceManager.NameType;
import dk.itu.big_red.model.interfaces.IChild;
import dk.itu.big_red.model.interfaces.IControl;
import dk.itu.big_red.model.interfaces.INode;
import dk.itu.big_red.model.interfaces.IParent;
import dk.itu.big_red.model.interfaces.IPort;
import dk.itu.big_red.model.interfaces.ISite;
import dk.itu.big_red.model.interfaces.internal.IFillColourable;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;
import dk.itu.big_red.model.interfaces.internal.INameable;
import dk.itu.big_red.model.interfaces.internal.IOutlineColourable;
import dk.itu.big_red.util.Geometry;
import dk.itu.big_red.util.HomogeneousIterable;

/**
 * 
 * @author alec
 * @see INode
 */
public class Node extends Container implements PropertyChangeListener, IFillColourable, IOutlineColourable, INameable, INode {
	/**
	 * The property name fired when the control changes. (Note that this
	 * property name is fired <i>after</i> any other changes required to change
	 * the control have been made.)
	 */
	public static final String PROPERTY_CONTROL = "NodeControl";
	
	private RGB fillColour = new RGB(255, 255, 255);
	private RGB outlineColour = new RGB(0, 0, 0);
	
	private ArrayList<Port> ports = new ArrayList<Port>();
	
	public Node() {
	}
	
	public Node(Control control) {
		setControl(control);
	}
	
	private Control control = null;
	
	@Override
	public Container clone() throws CloneNotSupportedException {
		Node result = new Node();
		result._overwrite(this);
		result.setControl(this.control);
		return result;
	}
	
	@Override
	public boolean canContain(ILayoutable child) {
		Class<? extends ILayoutable> c = child.getClass();
		return (c == Node.class || c == Site.class);
	}
	
	@Override
	public void setLayout(Rectangle layout) {
		Rectangle layoutCopy = new Rectangle(layout);
		if (control != null && !control.isResizable()) {
			layoutCopy.width = getLayout().width;
			layoutCopy.height = getLayout().height;
		}
		fittedPolygon = null;
		super.setLayout(layoutCopy);
	}
	
	/**
	 * Changes the {@link Control} of this Node. (Note that this operation will
	 * give the Node a new set of {@link Port}s, which means that all of its
	 * old ones will be disconnected.)
	 * 
	 * @param control the new Control; if <code>null</code>, the <i>default
	 *        control</i> will be used
	 */
	public void setControl(Control control) {
		Control oldControl = this.control;
		this.control = control;
		if (control != null) {
			fittedPolygon = null;
			
			if (oldControl != null)
				oldControl.removePropertyChangeListener(this);
			control.addPropertyChangeListener(this);
			
			/* XXX: disconnect old ports */
			ports = control.getPortsArray();
			for (Port p : ports)
				p.setParent(this);
			
			Point c = control.getDefaultSize();
			if (!control.isResizable()) {
				Rectangle nr = new Rectangle(this.getLayout());
				nr.width = c.x;
				nr.height = c.y;
				super.setLayout(nr);
			}
		}
		listeners.firePropertyChange(PROPERTY_CONTROL, oldControl, control);
	}
	
	/**
	 * Returns the {@link Control} of this Node.
	 * @return a Control
	 */
	public Control getControl() {
		return control;
	}

	public ArrayList<Port> getPorts() {
		return ports;
	}
	
	@Override
	public void setFillColour(RGB fillColour) {
		RGB oldColour = getFillColour();
		this.fillColour = fillColour;
		listeners.firePropertyChange(PROPERTY_FILL_COLOUR, oldColour, fillColour);
	}

	@Override
	public RGB getFillColour() {
		return fillColour;
	}

	@Override
	public void setOutlineColour(RGB outlineColour) {
		RGB oldColour = getOutlineColour();
		this.outlineColour = outlineColour;
		listeners.firePropertyChange(PROPERTY_OUTLINE_COLOUR, oldColour, outlineColour);
	}

	@Override
	public RGB getOutlineColour() {
		return outlineColour;
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent arg) {
		/*
		 * At the moment, this is unsophisticated - but it does at least
		 * propagate the visual changes immediately.
		 */
		setControl(null);
		setControl((Control)arg.getSource());
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
		if (fittedPolygon == null) {
			if (getControl() == null) {
				fittedPolygon = Geometry.fitPolygonToRectangle(Control.POINTS_QUAD, getLayout());
			} else if (getControl().getShape() == Shape.SHAPE_POLYGON) {
				fittedPolygon = Geometry.fitPolygonToRectangle(getControl().getPoints(), getLayout());
			}
		}
		return fittedPolygon;
	}
	
	@Override
	public String getName() {
		return getBigraph().getNamespaceManager().getRequiredName(getClass(), this);
	}
	
	@Override
	public void setName(String name) {
		NamespaceManager nm = getBigraph().getNamespaceManager();
		String oldName = nm.getName(getClass(), this);
		if (name != null) {
			if (nm.setName(getClass(), name, this))
				listeners.firePropertyChange(PROPERTY_NAME, oldName, name);
		} else {
			String newName = nm.newName(getClass(), this, NameType.NAME_ALPHABETIC);
			if (!newName.equals(oldName))
				listeners.firePropertyChange(PROPERTY_NAME, oldName, newName);
		}
	}

	@Override
	public IParent getIParent() {
		return (IParent)getParent();
	}

	@Override
	public Iterable<INode> getINodes() {
		return new HomogeneousIterable<INode>(children, INode.class);
	}

	@Override
	public Iterable<? extends IPort> getIPorts() {
		return ports;
	}

	@Override
	public Iterable<ISite> getISites() {
		return new HomogeneousIterable<ISite>(children, ISite.class);
	}
	
	@Override
	public Iterable<IChild> getIChildren() {
		return new HomogeneousIterable<IChild>(children, IChild.class);
	}
	
	@Override
	public IControl getIControl() {
		return control;
	}
}
