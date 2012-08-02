package dk.itu.big_red.model.load_save.savers;

import org.bigraph.model.ModelObject;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.SimulationSpec;
import org.bigraph.model.savers.SaveFailedException;
import org.bigraph.model.savers.SignatureXMLSaver;
import org.bigraph.model.savers.XMLSaver;
import org.w3c.dom.Element;

import static org.bigraph.model.loaders.RedNamespaceConstants.SPEC;

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
		processModel(getDocumentElement());
		finish();
	}
	
	@Override
	public Element processModel(Element e) throws SaveFailedException {
		SimulationSpec ss = getModel();
		
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
