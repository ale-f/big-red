package dk.itu.big_red.model.import_export;

import org.w3c.dom.Element;

import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.import_export.XMLExport;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.SimulationSpec;
import dk.itu.big_red.utilities.DOM;

public class SimulationSpecXMLExport extends XMLExport<SimulationSpec> {

	@Override
	public void exportObject() throws ExportFailedException {
		setDocument(DOM.createDocument(XMLNS.SPEC, "spec:spec"));
		processObject(getDocumentElement(), getModel());
		finish();
	}
	
	@Override
	public Element processObject(Element e, SimulationSpec ss) throws ExportFailedException {
		DOM.appendChildIfNotNull(e,
			processOrReference(
				newElement(XMLNS.SPEC, "spec:signature"),
				ss.getFile(), ss.getSignature(), SignatureXMLExport.class));
		
		for (ReactionRule rr : ss.getRules())
			DOM.appendChildIfNotNull(e,
				processOrReference(
					newElement(XMLNS.SPEC, "spec:rule"),
					ss.getFile(), rr, ReactionRuleXMLExport.class));
		
		DOM.appendChildIfNotNull(e,
			processOrReference(
				newElement(XMLNS.SPEC, "spec:model"),
				ss.getFile(), ss.getModel(), BigraphXMLExport.class));
		
		return e;
	}
}
