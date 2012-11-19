package org.bigraph.model.savers;

import org.bigraph.model.ModelObject;
import org.bigraph.model.ReactionRule;
import org.w3c.dom.Element;

import static org.bigraph.model.loaders.RedNamespaceConstants.EDIT;
import static org.bigraph.model.loaders.RedNamespaceConstants.RULE;
import static org.bigraph.model.loaders.RedNamespaceConstants.BIGRAPH;

public class ReactionRuleXMLSaver extends XMLSaver {
	public ReactionRuleXMLSaver() {
		this(null);
	}
	
	public ReactionRuleXMLSaver(ISaver parent) {
		super(parent);
		setDefaultNamespace(RULE);
	}
	
	@Override
	public ReactionRule getModel() {
		return (ReactionRule)super.getModel();
	}
	
	@Override
	public ReactionRuleXMLSaver setModel(ModelObject model) {
		if (model == null || model instanceof ReactionRule)
			super.setModel(model);
		return this;
	}
	
	@Override
	public void exportObject() throws SaveFailedException {
		setDocument(createDocument(RULE, "rule:rule"));
		processModel(getDocumentElement());
		finish();
	}

	@Override
	public Element processModel(Element e) throws SaveFailedException {
		ReactionRule rr = getModel();
		
		appendChildIfNotNull(e,
			processOrReference(
				newElement(BIGRAPH, "bigraph:bigraph"),
				rr.getRedex(), new BigraphXMLSaver(this)));
		
		appendChildIfNotNull(e,
			processOrReference(
				newElement(EDIT, "edit:edit"),
				rr.getEdit(), new EditXMLSaver(this)));
		
		return executeDecorators(rr, e);
	}
}
