package dk.itu.big_red.model.load_save.loaders;

import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.geometry.PointList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.PortSpec;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.Control.Kind;
import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.load_save.LoadFailedException;
import dk.itu.big_red.model.load_save.IRedNamespaceConstants;
import dk.itu.big_red.utilities.geometry.Ellipse;
import dk.itu.big_red.utilities.geometry.Rectangle;

public class SignatureXMLLoader extends XMLLoader {
	private ChangeGroup cg = new ChangeGroup();
	
	@Override
	public Signature importObject() throws LoadFailedException {
		try {
			Document d =
				validate(parse(source),
					RedPlugin.getResource("resources/schema/signature.xsd"));
			return makeObject(d.getDocumentElement()).setFile(getFile());
		} catch (Exception e) {
			throw new LoadFailedException(e);
		}
	}

	private Control makeControl(Element e) throws LoadFailedException {
		Control model = new Control();
		
		model.setName(getAttributeNS(e, IRedNamespaceConstants.SIGNATURE, "name"));
		
		String kind = getAttributeNS(e, IRedNamespaceConstants.SIGNATURE, "kind");
		if (kind != null) {
			model.setKind(
				kind.equals("active") ? Kind.ACTIVE :
				kind.equals("passive") ? Kind.PASSIVE : Kind.ATOMIC);
		}
		
		boolean generatePolygon = false;
		Element el = removeNamedChildElement(e, IRedNamespaceConstants.BIG_RED, "shape");
		if (el != null) {
			elementToShape(el, model);
		} else generatePolygon = true;
		
		el = removeNamedChildElement(e, IRedNamespaceConstants.BIG_RED, "appearance");
		if (el != null)
			BigraphXMLLoader.elementToAppearance(el, model, cg);
		
		String label =
				getAttributeNS(e, IRedNamespaceConstants.BIG_RED, "label");
		if (label != null)
			model.setLabel(label);
		
		for (Element j :
			getNamedChildElements(e, IRedNamespaceConstants.SIGNATURE, "port")) {
			PortSpec i = makePortSpec(j, generatePolygon);
			if (i != null)
				model.addPort(i);
		}
		
		if (generatePolygon) {
			model.setShape(Shape.POLYGON);
			model.setPoints(
				new Ellipse(new Rectangle(0, 0, 30, 30)).
					getPolygon(Math.max(3, model.getPorts().size())));
			int i = 0;
			for (PortSpec p : model.getPorts()) {
				p.setSegment(i++);
				p.setDistance(0.5);
			}
		}
		
		return model;
	}
	
	@Override
	public Signature makeObject(Element e) throws LoadFailedException {
		Signature sig = new Signature();
		
		cg.clear();
		
		for (Element j :
			getNamedChildElements(e, IRedNamespaceConstants.SIGNATURE, "control")) {
			Control i = makeControl(j);
			if (i != null)
				sig.addControl(i);
		}
		
		try {
			if (cg.size() != 0)
				sig.tryApplyChange(cg);
		} catch (ChangeRejectedException ex) {
			throw new LoadFailedException(ex);
		}
		
		return sig;
	}
	
	private PortSpec makePortSpec(Element e, boolean ignoreAppearanceData) {
		PortSpec model = new PortSpec();
		
		model.setName(getAttributeNS(e, IRedNamespaceConstants.SIGNATURE, "name"));
		
		Element el = removeNamedChildElement(e, IRedNamespaceConstants.BIG_RED, "port-appearance");
		if (el != null && !ignoreAppearanceData) {
			model.setDistance(getDoubleAttribute(el, IRedNamespaceConstants.BIG_RED, "distance"));
			model.setSegment(getIntAttribute(el, IRedNamespaceConstants.BIG_RED, "segment"));
		}
		
		return model;
	}
	
	@Override
	public SignatureXMLLoader setFile(IFile f) {
		return (SignatureXMLLoader)super.setFile(f);
	}

	private static void elementToShape(Element e, Control c) {
		if (!(e.getNamespaceURI().equals(IRedNamespaceConstants.BIG_RED) &&
				e.getLocalName().equals("shape")))
			return;
	
		Control.Shape shape = Shape.OVAL;
		PointList pl = null;
		
		String s = getAttributeNS(e, IRedNamespaceConstants.BIG_RED, "shape");
		if (s != null && s.equals("polygon"))
			shape = Shape.POLYGON;
		
		if (shape == Shape.POLYGON) {
			pl = new PointList();
			for (Element pE : getChildElements(e))
				pl.addPoint(
					getIntAttribute(pE, IRedNamespaceConstants.BIG_RED, "x"),
					getIntAttribute(pE, IRedNamespaceConstants.BIG_RED, "y"));
		}
		
		c.setShape(shape);
		c.setPoints(pl);
	}
}
