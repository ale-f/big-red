package dk.itu.big_red.model;

import java.util.HashMap;


import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dk.itu.big_red.util.DOM;

public class Name extends Thing {
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
		getListeners().firePropertyChange(PROPERTY_RENAME, oldName, name);
	}

	public NameType getType() {
		return type;
	}

	public void setType(NameType type) {
		NameType oldType = this.type;
		System.out.println(oldType + " -> " + type);
		this.type = type;
		getListeners().firePropertyChange(PROPERTY_TYPE, oldType, type);
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
		
		Element edgesE = doc.createElement("edges"); 
		for (Edge e : getSourceEdges()) {
			Element edge = doc.createElement("edge");
			edge.setAttribute("target", Integer.toString(e.getTarget().hashCode()));
			edge.setAttribute("sourceKey", e.getSourceKey());
			edge.setAttribute("targetKey", e.getTargetKey());
			edgesE.appendChild(edge);
		}
		r.appendChild(edgesE);

		return r;
	}
	
	@Override
	public Name fromXML(org.w3c.dom.Node d, HashMap<String, Thing> idRegistry) {
		idRegistry.put(DOM.getAttribute(d, "id"), this);
		
		Rectangle layout = new Rectangle();
		layout.x = DOM.getIntAttribute(d, "x");
		layout.y = DOM.getIntAttribute(d, "y");
		layout.width = DOM.getIntAttribute(d, "width");
		layout.height = DOM.getIntAttribute(d, "height");
		setLayout(layout);
		
		setType(NameType.valueOf(DOM.getAttribute(d, "type")));
		
		return this;
	}
}
