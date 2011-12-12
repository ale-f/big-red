package dk.itu.big_red.model.import_export;

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
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
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
		
		try {
			getModel().getReactum().tryApplyChange(
					getModel().getChanges().inverse());
		} catch (ChangeRejectedException ex) {
			throw new ExportFailedException(ex);
		}
		
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
	
	private static void hurl() throws ExportFailedException {
		throw new ExportFailedException("Aieee!");
	}
	
	private Element processChanges(Element e, ChangeGroup changes) throws ExportFailedException {
		DOM.applyAttributes(e,
				"xmlns:change", XMLNS.CHANGE);
		
		return _processChanges(e, changes);
	}
	
	private Element _processChanges(Element e, ChangeGroup changes) throws ExportFailedException {
		for (Change i_ : changes) {
			Element f = null;
			
			if (i_ instanceof Colourable.ChangeColour ||
					i_ instanceof Layoutable.ChangeLayout) {
				/* do nothing */;
			} else if (i_ instanceof ChangeGroup) {
				_processChanges(e, (ChangeGroup)i_);
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
								"parent", i.parent.getName());
					} else if (i.parent instanceof Node) {
						f = DOM.applyAttributes(
								newElement(XMLNS.CHANGE, "change:add-node-to-node"),
								"name", i.name,
								"parent", i.parent.getName());
					} else hurl();
				} else if (i.child instanceof Site) {
					if (i.parent instanceof Root) {
						f = DOM.applyAttributes(
								newElement(XMLNS.CHANGE, "change:add-site-to-root"),
								"name", i.name,
								"parent", i.parent.getName());
					} else if (i.parent instanceof Node) {
						f = DOM.applyAttributes(
								newElement(XMLNS.CHANGE, "change:add-site-to-node"),
								"name", i.name,
								"parent", i.parent.getName());
					} else hurl();
				} else hurl();
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
				
				DOM.applyAttributes(f,
						"name", i.model.getName(),
						"new-name", i.newName);
			} else if (i_ instanceof Point.ChangeConnect) {
				Point.ChangeConnect i = ac(i_);
				if (i.point instanceof InnerName) {
					f = DOM.applyAttributes(
							newElement(XMLNS.CHANGE, "change:connect-inner-name"),
							"name", i.point.getName(),
							"link", i.link.getName());
				} else if (i.point instanceof Port) {
					f = DOM.applyAttributes(
							newElement(XMLNS.CHANGE, "change:connect-port"),
							"node", i.point.getParent().getName(),
							"name", i.point.getName(),
							"link", i.link.getName());
				} else hurl();
			} else if (i_ instanceof Point.ChangeDisconnect) {
				Point.ChangeDisconnect i = ac(i_);
				if (i.point instanceof InnerName) {
					f = DOM.applyAttributes(
							newElement(XMLNS.CHANGE, "change:disconnect-inner-name"),
							"name", i.point.getName());
				} else if (i.point instanceof Port) {
					f = DOM.applyAttributes(
							newElement(XMLNS.CHANGE, "change:disconnect-port"),
							"node", i.point.getParent().getName(),
							"name", i.point.getName());
				} else hurl();
			} else hurl();
			
			/**
			 * Don't try to do anything with ChangeGroups - their Changes are
			 * individually dealt with.
			 */
			if (!(i_ instanceof ChangeGroup)) {
				DOM.appendChildIfNotNull(e, f);
				try {
					getModel().getReactum().tryApplyChange(i_);
				} catch (ChangeRejectedException ex) {
					throw new ExportFailedException(ex);
				}
			}
		}
		
		return e;
	}
}
