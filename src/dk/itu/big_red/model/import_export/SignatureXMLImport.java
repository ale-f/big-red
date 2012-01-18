package dk.itu.big_red.model.import_export;

import org.eclipse.core.resources.IFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.import_export.Import;
import dk.itu.big_red.import_export.ImportFailedException;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.PortSpec;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.Control.Kind;
import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.utilities.DOM;
import dk.itu.big_red.utilities.geometry.Ellipse;
import dk.itu.big_red.utilities.geometry.Rectangle;

public class SignatureXMLImport extends Import<Signature> {
	private ChangeGroup cg = new ChangeGroup();
	
	@Override
	public Signature importObject() throws ImportFailedException {
		try {
			Document d =
				DOM.validate(DOM.parse(source), RedPlugin.getResource("resources/schema/signature.xsd"));
			return makeSignature(d.getDocumentElement());
		} catch (Exception e) {
			throw new ImportFailedException(e);
		}
	}

	private Control makeControl(Element e) throws ImportFailedException {
		Control model = new Control();
		
		model.setName(DOM.getAttributeNS(e, XMLNS.SIGNATURE, "name"));
		
		String kind = DOM.getAttributeNS(e, XMLNS.SIGNATURE, "kind");
		if (kind != null) {
			model.setKind(
				kind.equals("active") ? Kind.ACTIVE :
				kind.equals("passive") ? Kind.PASSIVE : Kind.ATOMIC);
		}
		
		boolean generatePolygon = false;
		Element el = DOM.removeNamedChildElement(e, XMLNS.BIG_RED, "shape");
		if (el != null) {
			AppearanceGenerator.setShape(el, model);
		} else generatePolygon = true;
		
		el = DOM.removeNamedChildElement(e, XMLNS.BIG_RED, "appearance");
		if (el != null)
			AppearanceGenerator.setAppearance(el, model, cg);
		
		AppearanceGenerator.attributesToModel(e, model);
		
		for (Element j :
			DOM.getNamedChildElements(e, XMLNS.SIGNATURE, "port")) {
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
	
	public Signature makeSignature(Element e) throws ImportFailedException {
		Signature sig = new Signature();
		
		cg.clear();
		
		for (Element j :
			DOM.getNamedChildElements(e, XMLNS.SIGNATURE, "control")) {
			Control i = makeControl(j);
			if (i != null)
				sig.addControl(i);
		}
		
		try {
			if (cg.size() != 0)
				sig.tryApplyChange(cg);
		} catch (ChangeRejectedException ex) {
			throw new ImportFailedException(ex);
		}
		
		return sig;
	}
	
	private PortSpec makePortSpec(Element e, boolean ignoreAppearanceData) {
		PortSpec model = new PortSpec();
		
		model.setName(DOM.getAttributeNS(e, XMLNS.SIGNATURE, "name"));
		
		Element el = DOM.removeNamedChildElement(e, XMLNS.BIG_RED, "port-appearance");
		if (el != null && !ignoreAppearanceData) {
			model.setDistance(DOM.getDoubleAttribute(el, XMLNS.BIG_RED, "distance"));
			model.setSegment(DOM.getIntAttribute(el, XMLNS.BIG_RED, "segment"));
		}
		
		return model;
	}
	
	public static Signature importFile(IFile file) throws ImportFailedException {
		Object o = RedPlugin.getObjectService().getObject(file);
		if (o != null && o instanceof Signature)
			return (Signature)o;
		
		SignatureXMLImport im = new SignatureXMLImport();
		try {
			im.setInputStream(file.getContents());
		} catch (Exception e) {
			throw new ImportFailedException(e);
		}
		Signature s = im.importObject().setFile(file);
		RedPlugin.getObjectService().setObject(file, s);
		return s;
	}
}
