package dk.itu.big_red.model.import_export;

import org.w3c.dom.Element;

import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.import_export.XMLExport;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Colourable;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.assistants.BigraphScratchpad;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.util.DOM;

public class ReactionRuleXMLExport extends XMLExport<ReactionRule> {

	@Override
	public void exportObject() throws ExportFailedException {
		setDocument(DOM.createDocument(XMLNS.RULE, "rule:rule"));
		processRule(getDocumentElement(), getModel());
		finish();
	}

	public Element processRule(Element e, ReactionRule rr) throws ExportFailedException {
		DOM.appendChildIfNotNull(e,
			processRedex(newElement(XMLNS.RULE, "rule:redex"), rr.getRedex()));
		DOM.appendChildIfNotNull(e,
			processChanges(newElement(XMLNS.RULE, "rule:changes"), rr.getChanges()));
		return e;
	}
	
	private Element processRedex(Element e, Bigraph redex) throws ExportFailedException {
		DOM.applyAttributes(e,
				"xmlns:bigraph", XMLNS.BIGRAPH);
		BigraphXMLExport ex = new BigraphXMLExport();
		ex.setModel(redex);
		ex.setDocument(getDocument());
		return ex.processBigraph(e, ex.getModel());
	}
	
	@SuppressWarnings("unchecked")
	private static <T, V> T ac(V i) {
		return (T)i;
	}
	
	private Element processChanges(Element e, ChangeGroup changes) {
		DOM.applyAttributes(e,
				"xmlns:change", XMLNS.CHANGE);
		
		BigraphScratchpad scratch =
				new BigraphScratchpad(getModel().getRedex());
		
		for (Change i_ : changes) {
			Element f = null;
			
			if (i_ instanceof Colourable.ChangeColour ||
					i_ instanceof Layoutable.ChangeLayout) {
				continue;
			} else if (i_ instanceof ChangeGroup) {
				processChanges(e, (ChangeGroup)i_);
			} else if (i_ instanceof Container.ChangeAddChild) {
				Container.ChangeAddChild i = ac(i_);
				if (i.child instanceof Root) {
					f = DOM.applyAttributes(
							newElement(XMLNS.CHANGE, "change:add-root"),
							"name", i.name);
				} else if (i.child instanceof Edge) {
					f = DOM.applyAttributes(
							newElement(XMLNS.CHANGE, "change:add-edge"),
							"name", i.name);
				} else throw new RuntimeException("Fatal");
				scratch.addChildFor(i.parent, i.child, i.name);
			} else throw new RuntimeException("Fatal");
			
			DOM.appendChildIfNotNull(e, f);
		}
		
		return e;
	}
}
