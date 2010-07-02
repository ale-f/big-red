package dk.itu.big_red.model;

import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.util.DOM;

public class InnerName extends Point {
	public static final String PROPERTY_NAME = "NameName";
	public static final String PROPERTY_TYPE = "NameType";
	public static enum NameType {
		NAME_INNER,
		NAME_OUTER
	};
	
	@Override
	public InnerName clone() {
		System.out.println("! Clone?");
		return new InnerName();
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
		listeners.firePropertyChange(PROPERTY_NAME, oldName, name);
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
	public void fromXML(org.w3c.dom.Node d) {
		Rectangle layout = new Rectangle();
		layout.x = DOM.getIntAttribute(d, "x");
		layout.y = DOM.getIntAttribute(d, "y");
		layout.width = DOM.getIntAttribute(d, "width");
		layout.height = DOM.getIntAttribute(d, "height");
		setLayout(layout);
		
		setType(NameType.valueOf(DOM.getAttribute(d, "type")));
	}
	
	@Override
	public Bigraph getBigraph() {
		return getParent().getBigraph();
	}
}
