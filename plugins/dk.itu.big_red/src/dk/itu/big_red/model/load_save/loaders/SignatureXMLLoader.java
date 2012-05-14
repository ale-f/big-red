package dk.itu.big_red.model.load_save.loaders;

import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.PortSpec;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.Control.Kind;
import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.assistants.Ellipse;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.load_save.LoadFailedException;
import dk.itu.big_red.model.names.policies.BooleanNamePolicy;
import dk.itu.big_red.model.names.policies.INamePolicy;
import dk.itu.big_red.model.names.policies.LongNamePolicy;

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
			Signature s = makeObject(d.getDocumentElement());
			ExtendedDataUtilities.setFile(s, getFile());
			return s;
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
			INamePolicy n = null;
			if (parameter.equals("LONG")) {
				n = new LongNamePolicy();
			} else if (parameter.equals("BOOLEAN")) {
				n = new BooleanNamePolicy();
			}
			if (n != null)
				cg.add(ExtendedDataUtilities.changeParameterPolicy(model, n));
		}
		
		boolean generatePolygon = false;
		Element el = getNamedChildElement(e, BIG_RED, "shape");
		if (el != null) {
			elementToShape(el, model);
		} else generatePolygon = true;
		
		String label =
				getAttributeNS(e, BIG_RED, "label");
		if (label != null)
			cg.add(model.changeLabel(label));
		
		for (Element j : getNamedChildElements(e, SIGNATURE, "port"))
			makePortSpec(j, model, generatePolygon);
		
		if (generatePolygon) {
			cg.add(ExtendedDataUtilities.changeShape(model,
				new Ellipse(new Rectangle(0, 0, 30, 30)).
					getPolygon(Math.max(3, model.getPorts().size()))));
			int i = 0;
			for (PortSpec p : model.getPorts()) {
				cg.add(ExtendedDataUtilities.changeSegment(p, i++));
				cg.add(ExtendedDataUtilities.changeDistance(p, 0.5));
			}
		}
		
		executeUndecorators(model, e);
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
		
		return executeUndecorators(sig, e);
	}
	
	private PortSpec makePortSpec(
			Element e, Control c, boolean ignoreAppearanceData) {
		PortSpec model = new PortSpec();
		cg.add(c.changeAddPort(model, getAttributeNS(e, SIGNATURE, "name")));
		
		return executeUndecorators(model, e);
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
		
		if (shape == Shape.POLYGON) {
			pl = new PointList();
			for (Element pE : getChildElements(e))
				pl.addPoint(
					getIntAttribute(pE, BIG_RED, "x"),
					getIntAttribute(pE, BIG_RED, "y"));
			cg.add(ExtendedDataUtilities.changeShape(c, pl));
		} else cg.add(ExtendedDataUtilities.changeShape(c, new Ellipse()));
	}
}
