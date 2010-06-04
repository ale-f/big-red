package dk.itu.big_red.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

import org.eclipse.draw2d.geometry.Point;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dk.itu.big_red.exceptions.DuplicateControlException;

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
public class Control {
	public static enum Shape {
		SHAPE_RECTANGLE,
		SHAPE_OVAL,
		SHAPE_TRIANGLE
	}

	public static final String PROPERTY_LABEL = "MetaclassLabel";
	public static final String PROPERTY_SHAPE = "MetaclassShape";
	public static final String PROPERTY_DEFAULT_SIZE = "MetaclassDefaultSize";
	public static final String PROPERTY_RESIZABLE = "MetaclassResizable";
	public static final String PROPERTY_PORT = "MetaclassPort";
	
	private PropertyChangeSupport listeners =
		new PropertyChangeSupport(this);
	
	private ArrayList<String> ports = new ArrayList<String>();
	private ArrayList<Integer> offsets = new ArrayList<Integer>();
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
		this.ports.clear();
		listeners.firePropertyChange(PROPERTY_PORT, null, null);
	}
	
	public void addPort(String port, int offset) {
		if (port != null && !this.ports.contains(port)) {
			this.ports.add(port);
			this.offsets.add(offset);
			listeners.firePropertyChange(PROPERTY_PORT, null, port);
		}
	}
	
	public void removePort(String port) {
		int index = this.ports.indexOf(port);
		if (index != -1) {
			this.ports.remove(index);
			this.offsets.remove(index);
			listeners.firePropertyChange(PROPERTY_PORT, port, null);
		}
	}
	
	public boolean hasPort(String port) {
		return this.ports.contains(port);
	}
	
	public ArrayList<String> getPorts() {
		return ports;
	}
	
	public int getOffset(String port) {
		return this.offsets.get(this.ports.indexOf(port));
	}

	public Node toXML() {
		return null;
	}

	public Node toXML(Node d) {
		Document doc = d.getOwnerDocument();
		Element r = doc.createElement("metaclass");
		r.setAttribute("name", getLongName());
		r.setAttribute("control", getLabel());
		r.setAttribute("shape", getShape().toString());
		r.setAttribute("width", Integer.toString(getDefaultSize().x));
		r.setAttribute("height", Integer.toString(getDefaultSize().y));
		r.setAttribute("resizable", Boolean.toString(resizable));
		Element portsE = doc.createElement("ports");
		for (String port : getPorts()) {
			Element portE = doc.createElement("port");
			portE.setAttribute("key", port);
			portE.setAttribute("offset", Integer.toString(getOffset(port)));
			portsE.appendChild(portE);
		}
		r.appendChild(portsE);
		return r;
	}
	
	public void addListener(PropertyChangeListener o) {
		listeners.addPropertyChangeListener(o);
	}
	
	public void removeListener(PropertyChangeListener o) {
		listeners.removePropertyChangeListener(o);
	}
}