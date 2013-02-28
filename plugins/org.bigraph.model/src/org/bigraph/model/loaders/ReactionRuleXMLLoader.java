package org.bigraph.model.loaders;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Edit;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.resources.IFileWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import static org.bigraph.model.loaders.RedNamespaceConstants.EDIT;
import static org.bigraph.model.loaders.RedNamespaceConstants.RULE;
import static org.bigraph.model.loaders.RedNamespaceConstants.BIGRAPH;
import static org.bigraph.model.utilities.ArrayIterable.forNodeList;

public class ReactionRuleXMLLoader extends XMLLoader {
	@Deprecated
	public interface CompatibilityChangeLoader {
		void setReactionRule(ReactionRule rr);
		IChangeDescriptor changeDescriptorFromElement(org.w3c.dom.Node n);
	}
	
	@Deprecated
	private CompatibilityChangeLoader ccl;
	
	@Deprecated
	public void setCCL(CompatibilityChangeLoader ccl) {
		if (this.ccl == null) {
			this.ccl = ccl;
		} else throw new RuntimeException(
				"BUG: tried to register multiple CompatibilityChangeLoaders");
	}
	
	public ReactionRuleXMLLoader() {
	}
	
	public ReactionRuleXMLLoader(Loader parent) {
		super(parent);
	}
	
	private final ReactionRule rr = new ReactionRule();
	
	@Override
	public ReactionRule importObject() throws LoadFailedException {
		try {
			Document d = validate(parse(getInputStream()),
					Schemas.getRuleSchema());
			ReactionRule rr = makeObject(d.getDocumentElement());
			FileData.setFile(rr, getFile());
			return rr;
		} catch (LoadFailedException e) {
			throw e;
		} catch (Exception e) {
			throw new LoadFailedException(e);
		}
	}

	@Override
	public ReactionRule makeObject(Element e) throws LoadFailedException {
		cycleCheck();
		String replacement = getAttributeNS(e, RULE, "src");
		if (replacement != null)
			return loadRelative(replacement, ReactionRule.class,
					new ReactionRuleXMLLoader(this));
		executeUndecorators(rr, e);
		
		rr.setRedex(loadSub(
				selectFirst(
					getNamedChildElement(e, BIGRAPH, "bigraph"),
					getNamedChildElement(e, RULE, "redex")),
				RULE, Bigraph.class, new BigraphXMLLoader(this)));
		
		populateRRDescriptorGroup(e);
		
		return rr;
	}
	
	private void populateRRDescriptorGroup(Element root)
			throws LoadFailedException {
		ChangeDescriptorGroup cdg = rr.getEdit().getDescriptors();
		Element e = getNamedChildElement(root, RULE, "changes");
		if (e != null) {
			if (ccl == null)
				throw new LoadFailedException(
						"Can't load a <rule:changes /> element without a " +
						"ChangeCompatibilityLoader");
			ccl.setReactionRule(rr);
			for (Node i : forNodeList(e.getChildNodes())) {
				IChangeDescriptor c = ccl.changeDescriptorFromElement(i);
				if (c != null)
					cdg.add(c);
			}
		} else {
			e = getNamedChildElement(root, EDIT, "edit");
			if (e != null) {
				Edit ed = new EditXMLLoader(this).makeObject(e);
				for (IChangeDescriptor cd : ed.getDescriptors())
					cdg.add(cd);
			}
		}
	}
	
	@Override
	public ReactionRuleXMLLoader setFile(IFileWrapper f) {
		return (ReactionRuleXMLLoader)super.setFile(f);
	}
}
