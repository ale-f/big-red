package dk.itu.big_red.model;

import java.util.HashMap;


import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.model.interfaces.IConnectable;
import dk.itu.big_red.util.DOM;

public class Name extends Thing implements IConnectable {
	public static final String PROPERTY_NAME = "NameName";
	public static final String PROPERTY_TYPE = "NameType";
	public static enum NameType {
		NAME_INNER,
		NAME_OUTER
	};
	
	@Override
	public Thing clone() throws CloneNotSupportedException {
		return new Name()._overwrite(this);
	}
	
	public boolean canContain(Thing child) {
		return false;
	}
	
	private String name = "?";
	private NameType type = NameType.NAME_INNER;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		String oldName = this.name;
		this.name = name;
		listeners.firePropertyChange(PROPERTY_RENAME, oldName, name);
	}

	public NameType getType() {
		return type;
	}

	public void setType(NameType type) {
		NameType oldType = this.type;
		System.out.println(oldType + " -> " + type);
		this.type = type;
		listeners.firePropertyChange(PROPERTY_TYPE, oldType, type);
	}
	
	@Override
	public void connect(Edge e) {
		
	}

	@Override
	public void disconnect(Edge e) {
		
	}
	
	@Override
	public org.w3c.dom.Node toXML(org.w3c.dom.Node d) {
		/*
		 * Override in subclasses!
		 */
		Document doc = d.getOwnerDocument();
		
		org.w3c.dom.Element r = mintElement(d);
		r.setAttribute("name", getName());
		r.setAttribute("type", getType().toString());
		r.setAttribute("x", Integer.toString(getLayout().x));
		r.setAttribute("y", Integer.toString(getLayout().y));
		r.setAttribute("width", Integer.toString(getLayout().width));
		r.setAttribute("height", Integer.toString(getLayout().height));
		
		/* EDGE XML */

		return r;
	}
	
	@Override
	public void fromXML(org.w3c.dom.Node d) {
		getBigraph().idRegistry.put(DOM.getAttribute(d, "id"), this);
		
		Rectangle layout = new Rectangle();
		layout.x = DOM.getIntAttribute(d, "x");
		layout.y = DOM.getIntAttribute(d, "y");
		layout.width = DOM.getIntAttribute(d, "width");
		layout.height = DOM.getIntAttribute(d, "height");
		setLayout(layout);
		
		setType(NameType.valueOf(DOM.getAttribute(d, "type")));
	}

	@Override
	public boolean isConnected(Edge e) {
		// TODO Auto-generated method stub
		return false;
	}
}
