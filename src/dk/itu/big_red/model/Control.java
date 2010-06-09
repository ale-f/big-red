package dk.itu.big_red.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dk.itu.big_red.exceptions.DuplicateControlException;
import dk.itu.big_red.model.interfaces.IPropertyChangeNotifier;
import dk.itu.big_red.model.interfaces.IXMLisable;
import dk.itu.big_red.util.DOM;

/**
 * A Control is the bigraphical analogue of a <i>class</i> - a template from
 * which instances ({@link Node}s) should be constructed. Controls are
 * registered with a {@link Bigraph} as part of its {@link Signature}.
 * 
 * <p>In the formal bigraph model, controls define labels and numbered ports;
 * this model differs slightly by defining <i>named</i> ports and certain
 * graphical properties (chiefly shapes and default port offsets).
 * @author alec
 *
 */
public class Control implements IPropertyChangeNotifier, IXMLisable {
	public static enum Shape {
		SHAPE_RECTANGLE,
		SHAPE_OVAL,
		SHAPE_TRIANGLE,
		SHAPE_POLYGON
	}

	/**
	 * The property name fired when the label (the one- or two-character
	 * caption that appears next to {@link Node}s on the bigraph) changes.
	 */
	public static final String PROPERTY_LABEL = "ControlLabel";
	/**
	 * The property name fired when the name changes.
	 */
	public static final String PROPERTY_NAME = "ControlName";
	/**
	 * The property name fired when the shape changes.
	 */
	public static final String PROPERTY_SHAPE = "ControlShape";
	/**
	 * The property name fired when the set of points defining this control's
	 * polygon changes.
	 */
	public static final String PROPERTY_POLYGON = "ControlPolygon";
	/**
	 * The property name fired when the default size changes. (This only
	 * really matters for existing {@link Node}s if they aren't resizable.)
	 */
	public static final String PROPERTY_DEFAULT_SIZE = "ControlDefaultSize";
	/**
	 * The property name fired when the resizability changes. If this changes
	 * from <code>true</code> to <code>false</code>, listeners should make sure
	 * that any {@link Node}s with this Control are resized to the default
	 * size.
	 * @see Control#getDefaultSize
	 */
	public static final String PROPERTY_RESIZABLE = "ControlResizable";
	/**
	 * The property name fired when the set of ports changes. If this changes
	 * from <code>null</code> to a non-null value, then a port has been added;
	 * if it changes from a non-null value to <code>null</code>, one has been
	 * removed.
	 */
	public static final String PROPERTY_PORT = "ControlPort";
	
	private PropertyChangeSupport listeners =
		new PropertyChangeSupport(this);
	
	private ArrayList<String> portNames = new ArrayList<String>();
	private ArrayList<Integer> portOffsets = new ArrayList<Integer>();
	private PointList polygonSpec = new PointList();
	
	private Control.Shape shape;
	private String longName;
	private String label;
	private Point defaultSize;
	private boolean resizable;
	
	Control(String longName, String label, Control.Shape shape, Point defaultSize, boolean constraintModifiable) throws DuplicateControlException {
		setLongName(longName);
		setLabel(label);
		setShape(shape);
		setDefaultSize(defaultSize);
		setResizable(constraintModifiable);
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		String oldLabel = this.label;
		this.label = label;
		listeners.firePropertyChange(PROPERTY_LABEL, oldLabel, label);
	}
	
	public Control.Shape getShape() {
		return shape;
	}
	
	public void setShape(Control.Shape shape) {
		Control.Shape oldShape = this.shape;
		this.shape = shape;
		listeners.firePropertyChange(PROPERTY_SHAPE, oldShape, shape);
	}

	public void setLongName(String longName) {
		if (longName != null)
			this.longName = longName;
	}

	public String getLongName() {
		return longName;
	}
	
	public Point getDefaultSize() {
		return defaultSize;
	}
	
	public void setDefaultSize(Point defaultSize) {
		if (defaultSize != null) {
			Point oldSize = this.defaultSize;
			this.defaultSize = defaultSize;
			listeners.firePropertyChange(PROPERTY_DEFAULT_SIZE, oldSize, defaultSize);
		}
	}
	
	public boolean isResizable() {
		return resizable;
	}
	
	public void setResizable(Boolean resizable) {
		Boolean oldResizable = this.resizable;
		this.resizable = resizable;
		listeners.firePropertyChange(PROPERTY_RESIZABLE, oldResizable, resizable);
	}

	public void clearPorts() {
		this.portNames.clear();
		listeners.firePropertyChange(PROPERTY_PORT, null, null);
	}
	
	public void addPort(String port, int offset) {
		if (port != null && !this.portNames.contains(port)) {
			this.portNames.add(port);
			this.portOffsets.add(offset);
			listeners.firePropertyChange(PROPERTY_PORT, null, port);
		}
	}
	
	public void removePort(String port) {
		int index = this.portNames.indexOf(port);
		if (index != -1) {
			this.portNames.remove(index);
			this.portOffsets.remove(index);
			listeners.firePropertyChange(PROPERTY_PORT, port, null);
		}
	}
	
	public boolean hasPort(String port) {
		return this.portNames.contains(port);
	}
	
	public ArrayList<String> getPortNames() {
		return portNames;
	}
	
	/**
	 * Produces a <i>new</i> array of {@link Port}s to give to a {@link Node}.
	 * @return an array of Ports
	 */
	public ArrayList<Port> getPortsArray() {
		ArrayList<Port> r = new ArrayList<Port>();
		for (int i = 0; i < this.portNames.size(); i++)
			r.add(new Port(this.portNames.get(i), this.portOffsets.get(i)));
		return r;
	}
	
	public int getOffset(String port) {
		return this.portOffsets.get(this.portNames.indexOf(port));
	}
	
	/**
	 * If this object's shape is {@link Shape#SHAPE_POLYGON}, then gets a copy
	 * of the list of points defining its polygon.
	 * @return a list of points defining a polygon, or <code>null</code> if
	 *         this object's shape is not {@link Shape#SHAPE_POLYGON}
	 * @see Control#getShape
	 * @see Control#setShape
	 */
	public PointList getPolygon() {
		if (this.shape == Shape.SHAPE_POLYGON)
			return this.polygonSpec.getCopy();
		else return null;
	}
	
	/**
	 * If this object's shape is {@link Shape#SHAPE_POLYGON}, then sets the
	 * list of points defining its polygon to a copy of the list provided.
	 * @param newPolygon a list of points defining a new polygon
	 * @see Control#getShape
	 * @see Control#setShape
	 */
	public void setPolygon(PointList newPolygon) {
		if (this.shape == Shape.SHAPE_POLYGON) {
			PointList oldPolygon = this.polygonSpec;
			this.polygonSpec = newPolygon.getCopy();
			listeners.firePropertyChange(PROPERTY_POLYGON, oldPolygon, newPolygon);
		}
	}
	
	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.removePropertyChangeListener(listener);
	}
	
	@Override
	public Node toXML(Node d) {
		Document doc = d.getOwnerDocument();
		Element r = doc.createElement("control");
		DOM.applyAttributesToElement(r,
				"name", getLongName(),
				"label", getLabel(),
				"shape", getShape(),
				"width", getDefaultSize().x,
				"height", getDefaultSize().y,
				"resizable", this.resizable);
		for (String port : getPortNames()) {
			Element portE = doc.createElement("port");
			portE.setAttribute("key", port);
			portE.setAttribute("offset", Integer.toString(getOffset(port)));
			r.appendChild(portE);
		}
		return r;
	}
	
	@Override
	public void fromXML(Node d) {
		
	}
}