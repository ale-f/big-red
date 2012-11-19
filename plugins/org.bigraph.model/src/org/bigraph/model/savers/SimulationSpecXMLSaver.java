package org.bigraph.model.savers;

import org.bigraph.model.ModelObject;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.SimulationSpec;
import org.w3c.dom.Element;

import static org.bigraph.model.loaders.RedNamespaceConstants.RULE;
import static org.bigraph.model.loaders.RedNamespaceConstants.SPEC;
import static org.bigraph.model.loaders.RedNamespaceConstants.BIGRAPH;
import static org.bigraph.model.loaders.RedNamespaceConstants.SIGNATURE;

public class SimulationSpecXMLSaver extends XMLSaver {
	public SimulationSpecXMLSaver() {
		this(null);
	}
	
	public SimulationSpecXMLSaver(ISaver parent) {
		super(parent);
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
			processOrReference(newElement(SIGNATURE, "signature:signature"),
				ss.getSignature(), new SignatureXMLSaver(this)));
		
		for (ReactionRule rr : ss.getRules())
			appendChildIfNotNull(e,
				processOrReference(newElement(RULE, "rule:rule"),
					rr, new ReactionRuleXMLSaver(this)));
		
		appendChildIfNotNull(e,
			processOrReference(newElement(BIGRAPH, "bigraph:bigraph"),
				ss.getModel(), new BigraphXMLSaver(this)));
		
		return executeDecorators(ss, e);
	}
}
