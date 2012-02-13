package dk.itu.big_red.model.import_export;

import org.w3c.dom.Element;

import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.import_export.XMLExport;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Colourable.ChangeFillColour;
import dk.itu.big_red.model.Colourable.ChangeOutlineColour;
import dk.itu.big_red.model.Container.ChangeAddChild;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Container.ChangeRemoveChild;
import dk.itu.big_red.model.Layoutable.ChangeLayout;
import dk.itu.big_red.model.Layoutable.ChangeName;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Point.ChangeConnect;
import dk.itu.big_red.model.Point.ChangeDisconnect;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Site.ChangeAlias;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.utilities.DOM;

public class ReactionRuleXMLExport extends XMLExport {

	@Override
	public ReactionRule getModel() {
		return (ReactionRule)super.getModel();
	}
	
	@Override
	public ReactionRuleXMLExport setModel(ModelObject model) {
		if (model instanceof ReactionRule)
			super.setModel(model);
		return this;
	}
	
	@Override
	public void exportObject() throws ExportFailedException {
		setDocument(DOM.createDocument(XMLNS.RULE, "rule:rule"));
		processObject(getDocumentElement(), getModel());
		finish();
	}

	@Override
	public Element processObject(Element e, Object rr_) throws ExportFailedException {
		if (!(rr_ instanceof ReactionRule))
			throw new ExportFailedException(rr_ + " isn't a ReactionRule");
		ReactionRule rr = (ReactionRule)rr_;
		
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
		return ex.processObject(e, ex.getModel());
	}
	
	private Element processChanges(Element e, ChangeGroup changes) throws ExportFailedException {
		DOM.applyAttributes(e,
				"xmlns:change", XMLNS.CHANGE);
		
		return _processChanges(e, changes);
	}
	
	private Element _processChanges(Element e, ChangeGroup changes) throws ExportFailedException {
		for (Change i_ : changes) {
			Element f = null;
			
			if (i_ instanceof ChangeFillColour) {
				ChangeFillColour i = (ChangeFillColour)i_;
				if ((i.getCreator() instanceof Layoutable)) {
					Layoutable l = (Layoutable)i.getCreator();
					f = DOM.applyAttributes(
							newElement(XMLNS.BIG_RED, "big-red:fill"),
							"name", l.getName(),
							"type", l.getType().toLowerCase(),
							"colour", i.newColour.toHexString());
				}
			} else if (i_ instanceof ChangeOutlineColour) {
				ChangeOutlineColour i = (ChangeOutlineColour)i_;
				if ((i.getCreator() instanceof Layoutable)) {
					Layoutable l = (Layoutable)i.getCreator();
					f = DOM.applyAttributes(
							newElement(XMLNS.BIG_RED, "big-red:outline"),
							"name", l.getName(),
							"type", l.getType().toLowerCase(),
							"colour", i.newColour.toHexString());
				}
			} else if (i_ instanceof ChangeGroup) {
				_processChanges(e, (ChangeGroup)i_);
			} else if (i_ instanceof ChangeLayout) {
				ChangeLayout i = (ChangeLayout)i_;
				f = DOM.applyAttributes(
						newElement(XMLNS.BIG_RED, "big-red:layout"),
						"name", i.getCreator().getName(),
						"type", i.getCreator().getType().toLowerCase(),
						"x", i.newLayout.getX(),
						"y", i.newLayout.getY(),
						"width", i.newLayout.getWidth(),
						"height", i.newLayout.getHeight());
			} else if (i_ instanceof ChangeAddChild) {
				ChangeAddChild i = (ChangeAddChild)i_;
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
			} else if (i_ instanceof ChangeRemoveChild) {
				ChangeRemoveChild i = (ChangeRemoveChild)i_;
				f = DOM.applyAttributes(
						newElement(XMLNS.CHANGE, "change:remove"),
						"name", i.child.getName(),
						"type", i.child.getType().toLowerCase());
			} else if (i_ instanceof ChangeName) {
				ChangeName i = (ChangeName)i_;
				f = DOM.applyAttributes(
						newElement(XMLNS.CHANGE, "change:rename"),
						"name", i.getCreator().getName(), 
						"type", i.getCreator().getType().toLowerCase(),
						"new-name", i.newName);
			} else if (i_ instanceof ChangeConnect) {
				ChangeConnect i = (ChangeConnect)i_;
				f = DOM.applyAttributes(
						newElement(XMLNS.CHANGE, "change:connect"),
						"name", i.getCreator().getName(),
						"link", i.link.getName());
				if (i.getCreator() instanceof Port)
					DOM.applyAttributes(f,
							"node", ((Port)i.getCreator()).getParent().getName());
			} else if (i_ instanceof ChangeDisconnect) {
				ChangeDisconnect i = (ChangeDisconnect)i_;
				f = DOM.applyAttributes(
						newElement(XMLNS.CHANGE, "change:disconnect"),
						"name", i.getCreator().getName());
				if (i.getCreator() instanceof Port)
					DOM.applyAttributes(f,
							"node", ((Port)i.getCreator()).getParent().getName());
			} else if (i_ instanceof ChangeAlias) {
				ChangeAlias i = (ChangeAlias)i_;
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
