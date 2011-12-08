package dk.itu.big_red.model.import_export;

import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Element;

import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.import_export.XMLExport;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Colourable;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;
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
	
	private <T, V> T findKey(Map<T, V> map, V value) {
		for (Entry<T, V> i : map.entrySet())
			if (i.getValue().equals(value))
				return i.getKey();
		return null;
	}
	
	private static void hurl() {
		throw new RuntimeException("Fatal");
	}
	
	private Element processChanges(Element e, ChangeGroup changes) {
		try {
		DOM.applyAttributes(e,
				"xmlns:change", XMLNS.CHANGE);
		
		BigraphScratchpad scratch =
				new BigraphScratchpad(getModel().getReactum());
		
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
				} else if (i.child instanceof OuterName) {
					f = DOM.applyAttributes(
							newElement(XMLNS.CHANGE, "change:add-outer-name"),
							"name", i.name);
				} else if (i.child instanceof InnerName) {
					f = DOM.applyAttributes(
							newElement(XMLNS.CHANGE, "change:add-inner-name"),
							"name", i.name);
				} else if (i.child instanceof Node) {
					if (i.parent instanceof Root) {
						f = DOM.applyAttributes(
								newElement(XMLNS.CHANGE, "change:add-node-to-root"),
								"name", i.name,
								"parent", findKey(scratch.getNamespaceFor(i.parent), i.parent));
					} else if (i.parent instanceof Node) {
						f = DOM.applyAttributes(
								newElement(XMLNS.CHANGE, "change:add-node-to-node"),
								"name", i.name,
								"parent", findKey(scratch.getNamespaceFor(i.parent), i.parent));
					} else hurl();
				} else if (i.child instanceof Site) {
					if (i.parent instanceof Root) {
						f = DOM.applyAttributes(
								newElement(XMLNS.CHANGE, "change:add-site-to-root"),
								"name", i.name,
								"parent", findKey(scratch.getNamespaceFor(i.parent), i.parent));
					} else if (i.parent instanceof Node) {
						f = DOM.applyAttributes(
								newElement(XMLNS.CHANGE, "change:add-site-to-node"),
								"name", i.name,
								"parent", findKey(scratch.getNamespaceFor(i.parent), i.parent));
					} else hurl();
				} else hurl();
				scratch.addChildFor(i.parent, i.child, i.name);
			} else if (i_ instanceof Layoutable.ChangeName) {
				Layoutable.ChangeName i = ac(i_);
				if (i.model instanceof Root) {
					f = newElement(XMLNS.CHANGE, "change:rename-root");
				} else if (i.model instanceof Node) {
					f = newElement(XMLNS.CHANGE, "change:rename-node");
				} else if (i.model instanceof Site) {
					f = newElement(XMLNS.CHANGE, "change:rename-site");
				} else if (i.model instanceof Link) {
					f = newElement(XMLNS.CHANGE, "change:rename-link");
				} else if (i.model instanceof InnerName) {
					f = newElement(XMLNS.CHANGE, "change:rename-inner-name");
				} else hurl();
				
				String oldName =
					findKey(scratch.getNamespaceFor(i.model), i.model);
				DOM.applyAttributes(f,
						"name", oldName,
						"new-name", i.newName);
				scratch.getNamespaceFor(i.model).remove(oldName);
				scratch.setNameFor(i.model, i.newName);
			} else hurl();
			
			DOM.appendChildIfNotNull(e, f);
		}
		
		return e;
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(-1);
		}
		return null;
	}
}
