package dk.itu.big_red.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;


import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.RGB;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dk.itu.big_red.model.factories.ThingFactory;
import dk.itu.big_red.util.DOM;
import dk.itu.big_red.util.Utility;

public class Node extends Thing implements PropertyChangeListener {
	public static final String PROPERTY_COMMENT = "NodeComment";
	public static final String PROPERTY_CONTROL = "NodeControl";
	public static final String PROPERTY_FILL_COLOUR = "NodeFillColour";
	public static final String PROPERTY_OUTLINE_COLOUR = "NodeOutlineColour";
	
	private RGB fillColour = new RGB(255, 255, 255), outlineColour = new RGB(0, 0, 0);
	
	public Node() {
		setControl(Signature.DEFAULT_CONTROL);
	}
	
	public Node(Control control) {
		setControl(control);
	}
	
	private Control control = null;
	private String comment = null;
	
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
			/*
			 * Think of a better way to do this (possibly using a
			 * NonResizableEditPolicy? Where to install it, though?)
			 */
			layoutCopy.width = getLayout().width;
			layoutCopy.height = getLayout().height;
		}
		super.setLayout(layoutCopy);
	}
	
	public void setControl(Control control) {
		if (control != null) {
			Control oldControl = this.control;
			this.control = control;
			
			if (oldControl != null)
				oldControl.removeListener(this);
			control.addListener(this);
			
			Point c = control.getDefaultSize();
			if (!control.isResizable()) {
				Rectangle nr = new Rectangle(this.getLayout());
				nr.width = c.x;
				nr.height = c.y;
				super.setLayout(nr);
			}
			getListeners().firePropertyChange(PROPERTY_CONTROL, oldControl, control);
		} else {
			setControl(Signature.DEFAULT_CONTROL);
		}
	}
	
	public Control getControl() {
		return control;
	}

	public void setFillColour(RGB fillColour) {
		RGB oldColour = getFillColour();
		this.fillColour = fillColour;
		getListeners().firePropertyChange(PROPERTY_FILL_COLOUR, oldColour, fillColour);
	}

	public RGB getFillColour() {
		return fillColour;
	}

	public void setOutlineColour(RGB outlineColour) {
		RGB oldColour = getOutlineColour();
		this.outlineColour = outlineColour;
		getListeners().firePropertyChange(PROPERTY_OUTLINE_COLOUR, oldColour, outlineColour);
	}

	public RGB getOutlineColour() {
		return outlineColour;
	}

	public String getComment() {
		return this.comment;
	}
	
	public void setComment(String comment) {
		String oldComment = getComment();
		this.comment = comment;
		getListeners().firePropertyChange(PROPERTY_COMMENT, oldComment, comment);
	}
	
	public void connect(String srcPort, Node target, String destPort, Edge e) {
		if (getSignature().canConnect(srcPort, destPort) &&
			this.getControl().hasPort(srcPort) &&
			target.getControl().hasPort(destPort)) {
			e.setSource(this, srcPort);
			e.setTarget(target, destPort);
			e.setComment("(" + this + "[" + this.getControl().getLongName() + "])." + srcPort + " -> (" + target + "[" + target.getControl().getLongName() + "])." + destPort);
			this.addEdge(e); target.addEdge(e);
		}
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
		r.setAttribute("metaclass", getControl().getLongName());
		r.setAttribute("x", Integer.toString(getLayout().x));
		r.setAttribute("y", Integer.toString(getLayout().y));
		r.setAttribute("width", Integer.toString(getLayout().width));
		r.setAttribute("height", Integer.toString(getLayout().height));
		r.setAttribute("fill", Utility.colourToString(getFillColour()));
		r.setAttribute("outline", Utility.colourToString(getOutlineColour()));
		
		if (getComment() != null)
			r.setAttribute("comment", getComment());
		
		Element edgesE = doc.createElement("edges"); 
		for (Edge e : getSourceEdges()) {
			Element edge = doc.createElement("edge");
			edge.setAttribute("target", Integer.toString(e.getTarget().hashCode()));
			edge.setAttribute("sourceKey", e.getSourceKey());
			edge.setAttribute("targetKey", e.getTargetKey());
			edgesE.appendChild(edge);
		}
		r.appendChild(edgesE);
		
		Element childrenE = doc.createElement("children");
		for (Thing b : getChildrenArray())
			childrenE.appendChild(b.toXML(childrenE));
		r.appendChild(childrenE);
		return r;
	}
	
	@Override
	public void fromXML(org.w3c.dom.Node d, HashMap<String, Thing> idRegistry) {
		idRegistry.put(DOM.getAttribute(d, "id"), this);
		
		setControl(getSignature().getControl(DOM.getAttribute(d, "metaclass")));
		
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
		
		setFillColour(Utility.colourFromString(DOM.getAttribute(d, "fill")));
		setOutlineColour(Utility.colourFromString(DOM.getAttribute(d, "outline")));
		
		NodeList l = ((Element)d).getElementsByTagName("children").item(0).getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			org.w3c.dom.Node t = l.item(i);
			if (t.getAttributes() != null) {
				Thing nc = ThingFactory.getNewObject(t.getNodeName());
				addChild(nc);
				nc.fromXML(t, idRegistry);
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
