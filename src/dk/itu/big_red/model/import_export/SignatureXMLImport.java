package dk.itu.big_red.model.import_export;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.import_export.Import;
import dk.itu.big_red.import_export.ImportFailedException;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.PortSpec;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.model.assistants.ModelFactory;
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
			return (Signature)process(d.getDocumentElement());
		} catch (Exception e) {
			throw new ImportFailedException(e);
		}
	}

	private void processControl(Element e, Control model) throws ImportFailedException {
		model.setLongName(DOM.getAttributeNS(e, XMLNS.SIGNATURE, "name"));
		
		Element el = DOM.removeNamedChildElement(e, XMLNS.BIG_RED, "shape");
		if (el != null)
			AppearanceGenerator.setShape(el, model);
		
		el = DOM.removeNamedChildElement(e, XMLNS.BIG_RED, "appearance");
		if (el != null)
			AppearanceGenerator.setAppearance(el, model, cg);
		
		AppearanceGenerator.attributesToModel(e, model);
		
		for (Element j : DOM.getChildElements(e)) {
			Object i = process(j);
			if (i instanceof PortSpec)
				model.addPort((PortSpec)i);
		}
	}
	
	private void processSignature(Element e, Signature model) throws ImportFailedException {
		for (Element j : DOM.getChildElements(e)) {
			Object i = process(j);
			if (i instanceof Control)
				model.addControl((Control)i);
		}
	}
	
	private void processPort(Element e, PortSpec model) {
		model.setName(DOM.getAttributeNS(e, XMLNS.SIGNATURE, "name"));
		
		Element el = DOM.removeNamedChildElement(e, XMLNS.BIG_RED, "port-appearance");
		if (el != null) {
			model.setDistance(DOM.getDoubleAttribute(el, XMLNS.BIG_RED, "distance"));
			model.setSegment(DOM.getIntAttribute(el, XMLNS.BIG_RED, "segment"));
		}
	}
	
	private Object process(Element e) throws ImportFailedException {
		Object model = ModelFactory.getNewObject(e.getTagName());
		if (model instanceof Port)
			model = new PortSpec();
		
		if (model instanceof Signature)
			processSignature(e, (Signature)model);
		else if (model instanceof Control)
			processControl(e, (Control)model);
		else if (model instanceof PortSpec)
			processPort(e, (PortSpec)model);
		return model;
	}
	
	public static Signature importFile(IFile file) throws ImportFailedException {
		SignatureXMLImport s = new SignatureXMLImport();
		try {
			s.setInputStream(file.getContents());
		} catch (CoreException e) {
			throw new ImportFailedException(e);
		}
		return s.importObject().setFile(file);
	}
}
