package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.PointList;
import dk.itu.big_red.model.interfaces.IControl;
import dk.itu.big_red.model.interfaces.IPort;
import dk.itu.big_red.util.Utility;

/**
 * A Control is the bigraphical analogue of a <i>class</i> - a template from
 * which instances ({@link Node}s) should be constructed. Controls are
 * registered with a {@link Bigraph} as part of its {@link Signature}.
 * 
 * <p>In the formal bigraph model, controls define labels and numbered ports;
 * this model differs slightly by defining <i>named</i> ports and certain
 * graphical properties (chiefly shapes and default port offsets).
 * @author alec
 * @see IControl
 */
public class Control extends Colourable implements IControl {
	public static enum Shape {
		/**
		 * An oval.
		 */
		SHAPE_OVAL,
		/**
		 * A polygon.
		 */
		SHAPE_POLYGON
	}

	/**
	 * The property name fired when the label (the one- or two-character
	 * caption that appears next to {@link Node}s on the bigraph) changes.
	 * The property values are {@link String}s.
	 */
	public static final String PROPERTY_LABEL = "ControlLabel";
	
	/**
	 * The property name fired when the name changes. The property values are
	 * {@link String}s.
	 */
	public static final String PROPERTY_NAME = "ControlName";
	
	/**
	 * The property name fired when the shape changes. The property values are
	 * {@link Control.Shape}s.
	 */
	public static final String PROPERTY_SHAPE = "ControlShape";
	
	/**
	 * The property name fired when the set of points defining this control's
	 * polygon changes. The property values are {@link PointList}s.
	 */
	public static final String PROPERTY_POINTS = "ControlPoints";
	
	/**
	 * The property name fired when the default size changes. (This only
	 * really matters for existing {@link Node}s if they aren't resizable.)
	 * The property values are {@link Dimension}s.
	 */
	public static final String PROPERTY_DEFAULT_SIZE = "ControlDefaultSize";
	
	/**
	 * The property name fired when the resizability changes. If this changes
	 * from <code>true</code> to <code>false</code>, listeners should make sure
	 * that any {@link Node}s with this Control are resized to the default
	 * size. The property values are {@link Boolean}s.
	 * @see Control#getDefaultSize
	 */
	public static final String PROPERTY_RESIZABLE = "ControlResizable";
	
	/**
	 * The property name fired when the set of ports changes. If this changes
	 * from <code>null</code> to a non-null value, then a port has been added;
	 * if it changes from a non-null value to <code>null</code>, one has been
	 * removed. The property values are {@link PortSpec}s.
	 */
	public static final String PROPERTY_PORT = "ControlPort";
	
	public static final PointList POINTS_QUAD = new PointList(new int[] {
			0, 0,
			0, 40,
			-40, 40,
			-40, 0
	});
	
	public static final PointList POINTS_TRIANGLE = new PointList(new int[] {
			0, -40,
			20, 0,
			-20, 0
	});
	
	private ArrayList<PortSpec> ports = new ArrayList<PortSpec>();
	private PointList points = new PointList();
	
	private Control.Shape shape;
	private String longName;
	private String label;
	private Dimension defaultSize;
	private boolean resizable;
	public Control() {
		setLongName("Unknown");
		setLabel("?");
		setShape(Control.Shape.SHAPE_POLYGON, POINTS_QUAD);
		setDefaultSize(new Dimension(50, 50));
		setResizable(true);
	}
	
	public Control(String longName, String label, Control.Shape shape, PointList points, Dimension defaultSize, boolean constraintModifiable) {
		setLongName(longName);
		setLabel(label);
		setShape(shape, points);
		setDefaultSize(defaultSize);
		setResizable(constraintModifiable);
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		String oldLabel = this.label;
		this.label = label;
		firePropertyChange(PROPERTY_LABEL, oldLabel, label);
	}
	
	public Control.Shape getShape() {
		return shape;
	}
	
	/**
	 * If this object's shape is {@link Shape#SHAPE_POLYGON}, then gets a copy
	 * of the list of points defining its polygon.
	 * @return a list of points defining a polygon, or <code>null</code> if
	 *         this object's shape is not {@link Shape#SHAPE_POLYGON}
	 * @see Control#getShape
	 * @see Control#setShape
	 */
	public PointList getPoints() {
		if (shape == Shape.SHAPE_POLYGON)
			return points.getCopy();
		else return null;
	}
	
	/**
	 * Sets this Control's {@link Shape}. <code>points</code> must <i>not</i>
	 * be <code>null</code> if <code>shape</code> is {@link
	 * Shape#SHAPE_POLYGON}, but it <i>must</i> be <code>null</code> otherwise.
	 * @param shape the new Shape
	 * @param points a {@link PointList} specifying a polygon
	 */
	public void setShape(Control.Shape shape, PointList points) {
		if ((points == null && shape == Shape.SHAPE_POLYGON) ||
			(points != null && shape == Shape.SHAPE_OVAL))
			return;
		Control.Shape oldShape = this.shape;
		this.shape = shape;
		firePropertyChange(PROPERTY_SHAPE, oldShape, shape);
		
		PointList oldPoints = this.points;
		this.points = points;
		firePropertyChange(PROPERTY_POINTS, oldPoints, points);
	}

	public void setLongName(String longName) {
		if (longName != null) {
			this.longName = longName;
			if (longName.length() > 1)
				setLabel(longName.substring(0, 1).toUpperCase());
		}
	}

	public String getLongName() {
		return longName;
	}
	
	public Dimension getDefaultSize() {
		return defaultSize;
	}
	
	public void setDefaultSize(Dimension defaultSize) {
		if (defaultSize != null) {
			Dimension oldSize = this.defaultSize;
			this.defaultSize = defaultSize;
			firePropertyChange(PROPERTY_DEFAULT_SIZE, oldSize, defaultSize);
		}
	}
	
	public boolean isResizable() {
		return resizable;
	}
	
	public void setResizable(Boolean resizable) {
		Boolean oldResizable = this.resizable;
		this.resizable = resizable;
		firePropertyChange(PROPERTY_RESIZABLE, oldResizable, resizable);
	}

	public void clearPorts() {
		ports.clear();
		firePropertyChange(PROPERTY_PORT, null, null);
	}
	
	public void addPort(PortSpec p) {
		if (p != null) {
			PortSpec q = new PortSpec(p.getName(), p.getSegment(), p.getDistance());
			ports.add(q);
			firePropertyChange(PROPERTY_PORT, null, q);
		}
	}
	
	public void removePort(String port) {
		PortSpec p = getPort(port);
		if (p != null) {
			ports.remove(p);
			firePropertyChange(PROPERTY_PORT, p, null);
		}
	}
	
	public boolean hasPort(String port) {
		return (getPort(port) != null);
	}
	
	public List<PortSpec> getPorts() {
		return ports;
	}
	
	/**
	 * Produces a <i>new</i> array of {@link Port}s to give to a {@link Node}.
	 * @return an array of Ports
	 */
	public ArrayList<Port> getPortsArray() {
		ArrayList<Port> r = new ArrayList<Port>();
		for (PortSpec i : ports)
			r.add(new Port(i.getName(), i.getSegment(), i.getDistance()));
		return r;
	}
	
	public PortSpec getPort(String name) {
		for (PortSpec i : ports)
			if (i.getName().equals(name))
				return i;
		return null;
	}
	
	@Override
	public Iterable<IPort> getIPorts() {
		return Utility.only(ports, IPort.class);
	}

	@Override
	public String getName() {
		return getLongName();
	}
	
	/**
	 * {@inheritDoc}
	 * <p><strong>Special notes for {@link Control}:</strong>
	 * <ul>
	 * <li>Passing {@link #PROPERTY_PORT} will return a {@link List}&lt;{@link
	 * PortSpec}&gt;, <strong>not</strong> a {@link PortSpec}.
	 * </ul>
	 */
	@Override
	public Object getProperty(String name) {
		if (name.equals(PROPERTY_DEFAULT_SIZE)) {
			return getDefaultSize();
		} else if (name.equals(PROPERTY_LABEL)) {
			return getLabel();
		} else if (name.equals(PROPERTY_NAME)) {
			return getName();
		} else if (name.equals(PROPERTY_POINTS)) {
			return getPoints();
		} else if (name.equals(PROPERTY_PORT)) {
			return getPorts();
		} else if (name.equals(PROPERTY_RESIZABLE)) {
			return isResizable();
		} else if (name.equals(PROPERTY_SHAPE)) {
			return getShape();
		} else return super.getProperty(name);
	}
}