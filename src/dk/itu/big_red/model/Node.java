package dk.itu.big_red.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.RGB;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dk.itu.big_red.model.interfaces.IColourable;
import dk.itu.big_red.util.DOM;
import dk.itu.big_red.util.Utility;

public class Node extends Thing implements PropertyChangeListener, IColourable {
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
		setControl(Signature.DEFAULT_CONTROL);
	}
	
	public Node(Control control) {
		setControl(control);
	}
	
	private Control control = null;
	
	public Thing clone() throws CloneNotSupportedException {
		Node result = new Node();
		result._overwrite(this);
		result.setControl(this.control);
		return result;
	}
	
	public boolean canContain(Thing child) {
		Class<? extends Thing> c = child.getClass();
		return (c == Node.class || c == Site.class);
	}
	
	public void setLayout(Rectangle layout) {
		Rectangle layoutCopy = new Rectangle(layout);
		if (!control.isResizable()) {
			layoutCopy.width = getLayout().width;
			layoutCopy.height = getLayout().height;
		}
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
		if (control != null) {
			Control oldControl = this.control;
			this.control = control;
			
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
			listeners.firePropertyChange(PROPERTY_CONTROL, oldControl, control);
		} else {
			setControl(Signature.DEFAULT_CONTROL);
		}
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
	
	public Point getPortAnchorPosition(String port) {
		Point pt = null;
		if (getControl().hasPort(port)) {
			int offset = getControl().getOffset(port);
			switch (offset) {
			case 0:
				pt = new Point((layout.width / 2), 0);
				break;
			case 1:
				pt = new Point(0, (layout.height / 2));
				break;
			case 2:
				pt = new Point((layout.width / 2), layout.height);
				break;
			case 3:
				pt = new Point(layout.width, (layout.height / 2));
				break;
			default:
				break;
			}
		}
		return pt;
	}
	
	public org.w3c.dom.Node toXML(org.w3c.dom.Node d) {
		Element r = mintElement(d);
		Document doc = d.getOwnerDocument();
		r.setAttribute("control", getControl().getLongName());
		
		if (getComment() != null && getComment().length() > 0)
			r.setAttribute("comment", getComment());
		
		for (Port p : ports) {
			List<EdgeConnection> e = p.getConnections();
			if (e.size() != 0) {
				Element pointE = doc.createElement("point");
				DOM.applyAttributesToElement(pointE,
						"port", p.getName(),
						"edge", e.get(0).getParent().hashCode());
				r.appendChild(pointE);
			}
		}
		
		if (getChildrenArray().size() != 0) {
			for (Thing b : getChildrenArray())
				r.appendChild(b.toXML(r));
		}
		
		return r;
	}
	
	@Override
	public void fromXML(org.w3c.dom.Node d) {
		getBigraph().idRegistry.put(DOM.getAttribute(d, "id"), this);
		
		setControl(getSignature().getControl(DOM.getAttribute(d, "control")));
		
		Rectangle layout = new Rectangle();
		layout.x = DOM.getIntAttribute(d, "x");
		layout.y = DOM.getIntAttribute(d, "y");
		layout.width = DOM.getIntAttribute(d, "width");
		layout.height = DOM.getIntAttribute(d, "height");
		setLayout(layout);
		
		String comment = DOM.getAttribute(d, "comment");
		if (comment != null) {
			if (comment.length() > 0)
				setComment(comment);
		}
		
		setFillColour(DOM.getColorAttribute(d, "fill"));
		setOutlineColour(DOM.getColorAttribute(d, "outline"));
		
		NodeList l = ((Element)d).getElementsByTagName("children").item(0).getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			org.w3c.dom.Node t = l.item(i);
			if (t.getAttributes() != null) {
				Thing nc = (Thing)ModelFactory.getNewObject(t.getNodeName());
				addChild(nc);
				nc.fromXML(t);
			}
		}
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
}
