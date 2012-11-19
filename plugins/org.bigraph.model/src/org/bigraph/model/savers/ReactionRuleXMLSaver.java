package org.bigraph.model.savers;

import org.bigraph.model.Edit;
import org.bigraph.model.ModelObject;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
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
		
		Edit ed = new Edit();
		ChangeGroup cg = new ChangeGroup();
		
		int i = 0;
		for (IChangeDescriptor cd : rr.getChanges())
			cg.add(ed.changeDescriptorAdd(i++, cd));
		
		try {
			ed.tryApplyChange(cg);
		} catch (ChangeRejectedException cre) {
			throw new SaveFailedException(cre);
		}
		
		appendChildIfNotNull(e,
			processOrReference(
				newElement(EDIT, "edit:edit"),
				ed, new EditXMLSaver(this)));
		
		return executeDecorators(rr, e);
	}
}
