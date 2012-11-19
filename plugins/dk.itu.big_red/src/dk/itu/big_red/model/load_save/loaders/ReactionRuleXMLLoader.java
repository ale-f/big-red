package dk.itu.big_red.model.load_save.loaders;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Edit;
import org.bigraph.model.ModelObject.ChangeExtendedData;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.loaders.BigraphXMLLoader;
import org.bigraph.model.loaders.EditXMLLoader;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.loaders.Loader;
import org.bigraph.model.loaders.LoaderNotice;
import org.bigraph.model.loaders.Schemas;
import org.bigraph.model.loaders.XMLLoader;
import org.bigraph.model.resources.IFileWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dk.itu.big_red.model.LayoutUtilities;
import static org.bigraph.model.loaders.RedNamespaceConstants.EDIT;
import static org.bigraph.model.loaders.RedNamespaceConstants.RULE;
import static org.bigraph.model.loaders.RedNamespaceConstants.BIGRAPH;

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
		} else throw new Error(
				"BUG: tried to register multiple CompatibilityChangeLoaders");
	}
	
	public ReactionRuleXMLLoader() {
	}
	
	public ReactionRuleXMLLoader(Loader parent) {
		super(parent);
	}
	
	private ReactionRule rr = null;
	
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
		if (replacement != null) {
			return loadRelative(replacement, ReactionRule.class,
					new ReactionRuleXMLLoader(this));
		} else rr = new ReactionRule();
		
		rr.setRedex(loadSub(
				selectFirst(
					getNamedChildElement(e, BIGRAPH, "bigraph"),
					getNamedChildElement(e, RULE, "redex")),
				RULE, Bigraph.class, new BigraphXMLLoader(this)));
		
		populateRRDescriptorGroup(e);
		updateReactum();
		
		executeUndecorators(rr, e);
		return rr;
	}
	
	private PropertyScratchpad scratch = new PropertyScratchpad();
	
	private void populateRRDescriptorGroup(Element root)
			throws LoadFailedException {
		ChangeDescriptorGroup cdg = rr.getChanges();
		Element e = getNamedChildElement(root, RULE, "changes");
		if (e != null) {
			if (ccl == null)
				throw new LoadFailedException(
						"Can't load a <rule:changes /> element without a " +
						"ChangeCompatibilityLoader");
			ccl.setReactionRule(rr);
			NodeList nl = e.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				IChangeDescriptor c =
						ccl.changeDescriptorFromElement(nl.item(i));
				if (c != null)
					cdg.add(c);
			}
		} else {
			e = getNamedChildElement(root, EDIT, "edit");
			if (e != null) {
				Edit ed = new EditXMLLoader(this).makeObject(e);
				for (IChangeDescriptor cd : ed.getChildren())
					cdg.add(cd);
			}
		}
	}
	
	private void updateReactum() throws LoadFailedException {
		Bigraph reactum = rr.getReactum();
		ChangeDescriptorGroup cdg = rr.getChanges();
		
		ChangeGroup cg = null;
		try {
			cg = cdg.createChange(null, reactum);
			reactum.tryValidateChange(cg);
		} catch (ChangeCreationException cce) {
			throw new LoadFailedException(cce);
		} catch (ChangeRejectedException cre) {
			IChange ch = cre.getRejectedChange();
			if (ch instanceof ChangeExtendedData) {
				ChangeExtendedData cd = (ChangeExtendedData)ch;
				if (LayoutUtilities.LAYOUT.equals(cd.key)) {
					addNotice(LoaderNotice.Type.WARNING,
							"Layout data invalid; replacing.");
					cg.add(LayoutUtilities.relayout(scratch, reactum));
				} else throw new LoadFailedException(cre);
			} else throw new LoadFailedException(cre);
		}
		
		try {
			reactum.tryApplyChange(cg);
		} catch (ChangeRejectedException cre) {
			throw new LoadFailedException(cre);
		}
	}
	
	@Override
	public ReactionRuleXMLLoader setFile(IFileWrapper f) {
		return (ReactionRuleXMLLoader)super.setFile(f);
	}
}
