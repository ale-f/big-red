package dk.itu.big_red.model.load_save.savers;

import org.w3c.dom.Element;

import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.SimulationSpec;
import dk.itu.big_red.model.load_save.SaveFailedException;

import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.SPEC;

public class SimulationSpecXMLSaver extends XMLSaver {
	public SimulationSpecXMLSaver() {
		setDefaultNamespace(SPEC);
	}
	
	@Override
	public SimulationSpec getModel() {
		return (SimulationSpec)super.getModel();
	}
	
	@Override
	public SimulationSpecXMLSaver setModel(ModelObject model) {
		if (model == null || model instanceof SimulationSpec)
			super.setModel(model);
		return this;
	}
	
	@Override
	public void exportObject() throws SaveFailedException {
		setDocument(createDocument(SPEC, "spec:spec"));
		processObject(getDocumentElement(), getModel());
		finish();
	}
	
	@Override
	public Element processObject(Element e, Object ss_) throws SaveFailedException {
		if (!(ss_ instanceof SimulationSpec))
			throw new SaveFailedException(ss_ + " isn't a SimulationSpec");
		SimulationSpec ss = (SimulationSpec)ss_;
		
		appendChildIfNotNull(e,
			processOrReference(newElement(SPEC, "spec:signature"),
				ss.getSignature(), SignatureXMLSaver.class));
		
		for (ReactionRule rr : ss.getRules())
			appendChildIfNotNull(e,
				processOrReference(newElement(SPEC, "spec:rule"),
					rr, ReactionRuleXMLSaver.class));
		
		appendChildIfNotNull(e,
			processOrReference(newElement(SPEC, "spec:model"),
				ss.getModel(), BigraphXMLSaver.class));
		
		return executeDecorators(ss, e);
	}
}
