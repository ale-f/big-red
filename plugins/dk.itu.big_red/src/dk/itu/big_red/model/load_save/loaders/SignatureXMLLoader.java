package dk.itu.big_red.model.load_save.loaders;

import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.PortSpec;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.Control.Kind;
import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.assistants.Ellipse;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.load_save.LoadFailedException;
import dk.itu.big_red.model.names.BooleanNamePolicy;
import dk.itu.big_red.model.names.LongNamePolicy;

import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.BIG_RED;
import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.SIGNATURE;

public class SignatureXMLLoader extends XMLLoader {
	private ChangeGroup cg = new ChangeGroup();
	private Signature sig;
	
	@Override
	public Signature importObject() throws LoadFailedException {
		try {
			Document d =
				validate(parse(source), "resources/schema/signature.xsd");
			return makeObject(d.getDocumentElement()).setFile(getFile());
		} catch (Exception e) {
			throw new LoadFailedException(e);
		}
	}

	private void makeControl(Element e) throws LoadFailedException {
		Control model = new Control();
		cg.add(sig.changeAddControl(model));
		cg.add(model.changeName(getAttributeNS(e, SIGNATURE, "name")));
		
		String kind = getAttributeNS(e, SIGNATURE, "kind");
		if (kind != null) {
			cg.add(model.changeKind(
				kind.equals("active") ? Kind.ACTIVE :
				kind.equals("passive") ? Kind.PASSIVE : Kind.ATOMIC));
		}
		
		String parameter = getAttributeNS(e, SIGNATURE, "parameter");
		if (parameter != null) {
			if (parameter.equals("LONG")) {
				model.setParameterPolicy(new LongNamePolicy());
			} else if (parameter.equals("BOOLEAN")) {
				model.setParameterPolicy(new BooleanNamePolicy());
			}
		}
		
		boolean generatePolygon = false;
		Element el = removeNamedChildElement(e, BIG_RED, "shape");
		if (el != null) {
			elementToShape(el, model);
		} else generatePolygon = true;
		
		el = removeNamedChildElement(e, BIG_RED, "appearance");
		if (el != null)
			BigraphXMLLoader.elementToAppearance(el, model, cg);
		
		String label =
				getAttributeNS(e, BIG_RED, "label");
		if (label != null)
			cg.add(model.changeLabel(label));
		
		for (Element j :
			getNamedChildElements(e, SIGNATURE, "port")) {
			String name = getAttributeNS(e, SIGNATURE, "name");
			PortSpec i = makePortSpec(j, generatePolygon);
			if (i != null)
				cg.add(model.changeAddPort(i, name));
		}
		
		if (generatePolygon) {
			cg.add(model.changeShape(Shape.POLYGON));
			cg.add(model.changePoints(
				new Ellipse(new Rectangle(0, 0, 30, 30)).
					getPolygon(Math.max(3, model.getPorts().size()))));
			int i = 0;
			for (PortSpec p : model.getPorts()) {
				cg.add(p.changeSegment(i++));
				cg.add(p.changeDistance(0.5));
			}
		}
	}
	
	@Override
	public Signature makeObject(Element e) throws LoadFailedException {
		sig = new Signature();
		
		cg.clear();
		
		for (Element j : getNamedChildElements(e, SIGNATURE, "control"))
			makeControl(j);
		
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
		
		Element el = removeNamedChildElement(e, BIG_RED, "port-appearance");
		if (el != null && !ignoreAppearanceData) {
			int segment = getIntAttribute(el, BIG_RED, "segment");
			double distance = getDoubleAttribute(el, BIG_RED, "distance");
			cg.add(model.changeSegment(segment),
					model.changeDistance(distance));
		}
		
		return model;
	}
	
	@Override
	public SignatureXMLLoader setFile(IFile f) {
		return (SignatureXMLLoader)super.setFile(f);
	}

	private void elementToShape(Element e, Control c) {
		if (!(e.getNamespaceURI().equals(BIG_RED) &&
				e.getLocalName().equals("shape")))
			return;
	
		Control.Shape shape = Shape.OVAL;
		PointList pl = null;
		
		String s = getAttributeNS(e, BIG_RED, "shape");
		if (s != null && s.equals("polygon"))
			shape = Shape.POLYGON;
		
		cg.add(c.changeShape(shape));
		
		if (shape == Shape.POLYGON) {
			pl = new PointList();
			for (Element pE : getChildElements(e))
				pl.addPoint(
					getIntAttribute(pE, BIG_RED, "x"),
					getIntAttribute(pE, BIG_RED, "y"));
			cg.add(c.changePoints(pl));
		}
	}
}
