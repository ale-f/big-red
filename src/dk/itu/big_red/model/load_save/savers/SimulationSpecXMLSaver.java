package dk.itu.big_red.model.load_save.savers;

import org.w3c.dom.Element;

import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.SimulationSpec;
import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.model.load_save.XMLSaver;
import dk.itu.big_red.model.load_save.XMLNS;

public class SimulationSpecXMLSaver extends XMLSaver {
	@Override
	public SimulationSpec getModel() {
		return (SimulationSpec)super.getModel();
	}
	
	@Override
	public SimulationSpecXMLSaver setModel(ModelObject model) {
		if (model instanceof SimulationSpec)
			super.setModel(model);
		return this;
	}
	
	@Override
	public void exportObject() throws SaveFailedException {
		setDocument(createDocument(XMLNS.SPEC, "spec:spec"));
		processObject(getDocumentElement(), getModel());
		finish();
	}
	
	@Override
	public Element processObject(Element e, Object ss_) throws SaveFailedException {
		if (!(ss_ instanceof SimulationSpec))
			throw new SaveFailedException(ss_ + " isn't a SimulationSpec");
		SimulationSpec ss = (SimulationSpec)ss_;
		
		appendChildIfNotNull(e,
			processOrReference(
				newElement(XMLNS.SPEC, "spec:signature"),
				ss.getFile(), ss.getSignature(), SignatureXMLSaver.class));
		
		for (ReactionRule rr : ss.getRules())
			appendChildIfNotNull(e,
				processOrReference(
					newElement(XMLNS.SPEC, "spec:rule"),
					ss.getFile(), rr, ReactionRuleXMLSaver.class));
		
		appendChildIfNotNull(e,
			processOrReference(
				newElement(XMLNS.SPEC, "spec:model"),
				ss.getFile(), ss.getModel(), BigraphXMLSaver.class));
		
		return e;
	}
}
