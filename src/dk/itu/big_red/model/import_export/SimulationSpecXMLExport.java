package dk.itu.big_red.model.import_export;

import org.w3c.dom.Element;

import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.import_export.XMLExport;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.SimulationSpec;
import dk.itu.big_red.utilities.DOM;

public class SimulationSpecXMLExport extends XMLExport {
	@Override
	public SimulationSpec getModel() {
		return (SimulationSpec)super.getModel();
	}
	
	@Override
	public SimulationSpecXMLExport setModel(ModelObject model) {
		if (model instanceof SimulationSpec)
			super.setModel(model);
		return this;
	}
	
	@Override
	public void exportObject() throws ExportFailedException {
		setDocument(DOM.createDocument(XMLNS.SPEC, "spec:spec"));
		processObject(getDocumentElement(), getModel());
		finish();
	}
	
	@Override
	public Element processObject(Element e, Object ss_) throws ExportFailedException {
		if (!(ss_ instanceof SimulationSpec))
			throw new ExportFailedException(ss_ + " isn't a SimulationSpec");
		SimulationSpec ss = (SimulationSpec)ss_;
		
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
