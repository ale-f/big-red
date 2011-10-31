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
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.util.DOM;

public class SignatureXMLImport extends Import<Signature> {
	private ChangeGroup cg = new ChangeGroup();
	
	@Override
	public Signature importObject() throws ImportFailedException {
		cg.clear();
		try {
			Document d =
				DOM.validate(DOM.parse(source), RedPlugin.getResource("schema/signature.xsd"));
			source.close();
			return makeSignature(d.getDocumentElement());
		} catch (Exception e) {
			throw new ImportFailedException(e);
		}
	}

	private Control makeControl(Element e) throws ImportFailedException {
		Control model = new Control();
		
		model.setLongName(DOM.getAttributeNS(e, XMLNS.SIGNATURE, "name"));
		
		Element el = DOM.removeNamedChildElement(e, XMLNS.BIG_RED, "shape");
		if (el != null)
			AppearanceGenerator.setShape(el, model);
		
		el = DOM.removeNamedChildElement(e, XMLNS.BIG_RED, "appearance");
		if (el != null)
			AppearanceGenerator.setAppearance(el, model, cg);
		
		AppearanceGenerator.attributesToModel(e, model);
		
		for (Element j :
			DOM.getNamedChildElements(e, XMLNS.SIGNATURE, "port")) {
			PortSpec i = makePortSpec(j);
			if (i != null)
				model.addPort(i);
		}
		
		return model;
	}
	
	public Signature makeSignature(Element e) throws ImportFailedException {
		Signature sig = new Signature();
		
		for (Element j :
			DOM.getNamedChildElements(e, XMLNS.SIGNATURE, "control")) {
			Control i = makeControl(j);
			if (i != null)
				sig.addControl(i);
		}
		
		return sig;
	}
	
	private PortSpec makePortSpec(Element e) {
		PortSpec model = new PortSpec();
		
		model.setName(DOM.getAttributeNS(e, XMLNS.SIGNATURE, "name"));
		
		Element el = DOM.removeNamedChildElement(e, XMLNS.BIG_RED, "port-appearance");
		if (el != null) {
			model.setDistance(DOM.getDoubleAttribute(el, XMLNS.BIG_RED, "distance"));
			model.setSegment(DOM.getIntAttribute(el, XMLNS.BIG_RED, "segment"));
		}
		
		return model;
	}
	
	public static Signature importFile(IFile file) throws ImportFailedException {
		SignatureXMLImport s = new SignatureXMLImport();
		try {
			s.setInputStream(file.getContents());
			return s.importObject().setFile(file);
		} catch (Exception e) {
			throw new ImportFailedException(e);
		}
	}
}
