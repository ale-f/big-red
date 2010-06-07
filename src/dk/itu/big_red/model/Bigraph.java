package dk.itu.big_red.model;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilderFactory;


import org.eclipse.draw2d.geometry.Point;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dk.itu.big_red.commands.EdgeCreateCommand;
import dk.itu.big_red.model.factories.ThingFactory;
import dk.itu.big_red.util.DOM;

public class Bigraph extends Thing {
	protected Signature signature = new Signature();

	public Thing clone() throws CloneNotSupportedException {
		return new Bigraph()._overwrite(this);
	}
	public boolean canContain(Thing child) {
		Class<? extends Thing> c = child.getClass();
		return (c == Root.class || c == Name.class);
	}
	
	@Override
	public Node toXML() {
		DOMImplementation impl = DOM.getImplementation();
		
		Document doc = impl.createDocument(null, "big-red", null);
		Node node = doc.getDocumentElement();
		
		node.appendChild(doc.createComment(
			"This is a Big Red XML bigraph definition. " +
			"DO NOT EDIT IT UNLESS YOU KNOW WHAT YOU'RE DOING - " +
			"whitespace is significant and no attributes are optional!"));
		
		Node portzE = doc.createElement("ports");
		for (String key : signature.getPorts()) {
			Element p = doc.createElement("port");
			p.setAttribute("key", key);
			for (String port : signature.getConnections(key)) {
				Element x = doc.createElement("can-connect-to");
				x.setTextContent(port);
				p.appendChild(x);
			}
			portzE.appendChild(p);
		}
		node.appendChild(portzE);
		
		Node metaclassesE = doc.createElement("metaclasses");
		for (Control k : getSignature().getControls())
			metaclassesE.appendChild(k.toXML(metaclassesE));
		node.appendChild(metaclassesE);
		
		Node bigraphE = doc.createElement("bigraph");
		for (Thing b : getChildrenArray())
			bigraphE.appendChild(b.toXML(bigraphE));
		node.appendChild(bigraphE);
		return doc;
	}
	
	public static Bigraph fromXML(org.w3c.dom.Document doc) {
		HashMap<String, Thing> elements = new HashMap<String, Thing>();
		
		Bigraph r = new Bigraph();
		
		ArrayList<Node> mcs =
			DOM.getNamedChildNodes(doc.getElementsByTagName("ports").item(0), "port");
		for (Node t : mcs) {
			String port = DOM.getAttribute(t, "key");
			NodeList ccs = t.getChildNodes();
			for (int j = 0; j < ccs.getLength(); j++) {
				Node u = ccs.item(j);
				if (u.getAttributes() != null) {
					String dst = u.getTextContent();
					r.getSignature().allowConnection(port, dst);
				}
			}
		}
		
		mcs =
			DOM.getNamedChildNodes(doc.getElementsByTagName("metaclasses").item(0), "metaclass");
		for (Node t : mcs) {
			String name = DOM.getAttribute(t, "name");
			Control.Shape shape =
				Control.Shape.valueOf(DOM.getAttribute(t, "shape"));
			String control = DOM.getAttribute(t, "control");
			
			Point defaultSize = new Point(
				DOM.getIntAttribute(t, "width"),
				DOM.getIntAttribute(t, "height"));
			
			boolean resizable =
				DOM.getAttribute(t, "resizable").equals("true");
			
			Control mc =
				r.getSignature().
				registerControl(name, control, shape, defaultSize, resizable);
			
			Node p = DOM.getNamedChildNode(t, "ports");
			ArrayList<Node> ports = DOM.getNamedChildNodes(p, "port");
			for (Node u : ports) {
				String port = DOM.getAttribute(u, "key");
				int offset = DOM.getIntAttribute(u, "offset");
					
				mc.addPort(port, offset);
			}
		}
		
		NodeList l = doc.getElementsByTagName("bigraph").item(0).getChildNodes();
		for (int i = 0; i < l.getLength(); i++) {
			Node t = l.item(i);
			if (t.getAttributes() != null) {
				Thing nc = ThingFactory.getNewObject(t.getNodeName());
				r.addChild(nc);
				nc.fromXML(t, elements);
			}
		}
		
		l = doc.getElementsByTagName("edges");
		for (int i = 0; i < l.getLength(); i++) {
			Node t = l.item(i);
			ArrayList<Node> edgesc = DOM.getNamedChildNodes(t, "edge");
			for (Node u : edgesc) {
				String src = DOM.getAttribute(u.getParentNode().getParentNode(), "id");
				String dst = DOM.getAttribute(u, "target");
				String srcKey = DOM.getAttribute(u, "sourceKey");
				String dstKey = DOM.getAttribute(u, "targetKey");
				
				Thing srcNode = ((Thing)elements.get(src)),
				         dstNode = ((Thing)elements.get(dst));
				
				EdgeCreateCommand cmd = new EdgeCreateCommand();
				cmd.setObject(new Edge());
				cmd.setSource(srcNode);
				cmd.setTarget(dstNode);
				cmd.setSourceKey(srcKey);
				cmd.setTargetKey(dstKey);
				cmd.execute();
			}
		}
		return r;
	}
	
	public static Bigraph fromXML(String filename) {
		File file = new File(filename);
		Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return Bigraph.fromXML(doc);
	}
	
	public Bigraph getBigraph() {
		return this;
	}
	
	public Signature getSignature() {
		return signature;
	}
}
