package dk.itu.big_red.model.import_export;

import org.w3c.dom.Element;

import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.import_export.XMLExport;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Colourable;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.utilities.DOM;

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
			if (getModel().getChanges().size() != 0)
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
	
	private Element processChanges(Element e, ChangeGroup changes) throws ExportFailedException {
		DOM.applyAttributes(e,
				"xmlns:change", XMLNS.CHANGE);
		
		return _processChanges(e, changes);
	}
	
	private Element _processChanges(Element e, ChangeGroup changes) throws ExportFailedException {
		for (Change i_ : changes) {
			Element f = null;
			
			if (i_ instanceof Colourable.ChangeFillColour ||
					i_ instanceof Colourable.ChangeOutlineColour) {
				/* do nothing */;
			} else if (i_ instanceof ChangeGroup) {
				_processChanges(e, (ChangeGroup)i_);
			} else if (i_ instanceof Layoutable.ChangeLayout) {
				Layoutable.ChangeLayout i = ac(i_);
				f = DOM.applyAttributes(
						newElement(XMLNS.BIG_RED, "big-red:layout"),
						"name", i.getCreator().getName(),
						"type", i.getCreator().getType().toLowerCase(),
						"x", i.newLayout.getX(),
						"y", i.newLayout.getY(),
						"width", i.newLayout.getWidth(),
						"height", i.newLayout.getHeight());
			} else if (i_ instanceof Container.ChangeAddChild) {
				Container.ChangeAddChild i = ac(i_);
				f = DOM.applyAttributes(
						newElement(XMLNS.CHANGE, "change:add"),
						"name", i.name,
						"type", i.child.getType().toLowerCase());
				if (!(i.getCreator() instanceof Bigraph))
					DOM.applyAttributes(f,
							"parent", i.getCreator().getName(),
							"parent-type", i.getCreator().getType().toLowerCase());
				if (i.child instanceof Node)
					DOM.applyAttributes(f,
							"control", ((Node)i.child).getControl().getName());
			} else if (i_ instanceof Container.ChangeRemoveChild) {
				Container.ChangeRemoveChild i = ac(i_);
				f = DOM.applyAttributes(
						newElement(XMLNS.CHANGE, "change:remove"),
						"name", i.child.getName(),
						"type", i.child.getType().toLowerCase());
			} else if (i_ instanceof Layoutable.ChangeName) {
				Layoutable.ChangeName i = ac(i_);
				f = DOM.applyAttributes(
						newElement(XMLNS.CHANGE, "change:rename"),
						"name", i.getCreator().getName(), 
						"type", i.getCreator().getType().toLowerCase(),
						"new-name", i.newName);
			} else if (i_ instanceof Point.ChangeConnect) {
				Point.ChangeConnect i = ac(i_);
				f = DOM.applyAttributes(
						newElement(XMLNS.CHANGE, "change:connect"),
						"name", i.getCreator().getName(),
						"link", i.link.getName());
				if (i.getCreator() instanceof Port)
					DOM.applyAttributes(f,
							"node", ((Port)i.getCreator()).getParent().getName());
			} else if (i_ instanceof Point.ChangeDisconnect) {
				Point.ChangeDisconnect i = ac(i_);
				f = DOM.applyAttributes(
						newElement(XMLNS.CHANGE, "change:disconnect"),
						"name", i.getCreator().getName());
				if (i.getCreator() instanceof Port)
					DOM.applyAttributes(f,
							"node", ((Port)i.getCreator()).getParent().getName());
			} else if (i_ instanceof Site.ChangeAlias) {
				Site.ChangeAlias i = ac(i_);
				f = DOM.applyAttributes(
						newElement(XMLNS.CHANGE, "change:site-alias"),
						"name", i.getCreator().getName(),
						"alias", i.alias);
			}
			
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
