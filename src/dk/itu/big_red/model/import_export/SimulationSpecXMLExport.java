package dk.itu.big_red.model.import_export;

import org.w3c.dom.Element;

import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.import_export.XMLExport;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.SimulationSpec;
import dk.itu.big_red.utilities.DOM;
import dk.itu.big_red.utilities.resources.Project;

public class SimulationSpecXMLExport extends XMLExport<SimulationSpec> {

	@Override
	public void exportObject() throws ExportFailedException {
		setDocument(DOM.createDocument(XMLNS.SPEC, "spec:spec"));
		processObject(getDocumentElement(), getModel());
		finish();
	}
	
	private Element processSignature(Element e, Signature s) throws ExportFailedException {
		if (s.getFile() != null) {
			DOM.applyAttributes(e,
				"src", Project.getRelativePath(
						getModel().getFile(), s.getFile()).toString());	
		} else {
			DOM.applyAttributes(e,
				"xmlns:signature", XMLNS.SIGNATURE);
			SignatureXMLExport ex = new SignatureXMLExport();
			ex.setDocument(getDocument()).setModel(s);
			ex.processObject(e, s);
		}
		return e;
	}

	private Element processReactionRule(Element e, ReactionRule rr) throws ExportFailedException {
		if (rr.getFile() != null) {
			DOM.applyAttributes(e,
				"src", Project.getRelativePath(
						getModel().getFile(), rr.getFile()).toString());	
		} else {
			DOM.applyAttributes(e,
				"xmlns:signature", XMLNS.SIGNATURE);
			ReactionRuleXMLExport ex = new ReactionRuleXMLExport();
			ex.setDocument(getDocument()).setModel(rr);
			ex.processObject(e, rr);
		}
		return e;
	}
	
	private Element processBigraph(Element e, Bigraph b) throws ExportFailedException {
		if (b.getFile() != null) {
			DOM.applyAttributes(e,
				"src", Project.getRelativePath(
						getModel().getFile(), b.getFile()).toString());	
		} else {
			DOM.applyAttributes(e,
				"xmlns:signature", XMLNS.SIGNATURE);
			BigraphXMLExport ex = new BigraphXMLExport();
			ex.setDocument(getDocument()).setModel(b);
			ex.processObject(e, b);
		}
		return e;
	}

	@Override
	public Element processObject(Element e, SimulationSpec ss) throws ExportFailedException {
		DOM.appendChildIfNotNull(e,
			processSignature(
				newElement(XMLNS.SPEC, "spec:signature"),
				ss.getSignature()));
		
		for (ReactionRule rr : ss.getRules())
			processReactionRule(
				newElement(XMLNS.SPEC, "spec:rule"), rr);
				
		DOM.appendChildIfNotNull(e,
			processBigraph(
				newElement(XMLNS.SPEC, "spec:model"),
				ss.getModel()));
		
		return e;
	}
}
