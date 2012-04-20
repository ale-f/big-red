package dk.itu.big_red.model.load_save.savers;

import org.w3c.dom.Element;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Colourable.ChangeFillColour;
import dk.itu.big_red.model.Colourable.ChangeOutlineColour;
import dk.itu.big_red.model.Container.ChangeAddChild;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Layoutable.ChangeLayout;
import dk.itu.big_red.model.Layoutable.ChangeName;
import dk.itu.big_red.model.Layoutable.ChangeRemove;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.ModelObject.ChangeComment;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Point.ChangeConnect;
import dk.itu.big_red.model.Point.ChangeDisconnect;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Site.ChangeAlias;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.model.load_save.IRedNamespaceConstants;

import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.RULE;
import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.CHANGE;
import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.BIG_RED;

public class ReactionRuleXMLSaver extends XMLSaver {
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
		processObject(getDocumentElement(), getModel());
		finish();
	}

	@Override
	public Element processObject(Element e, Object rr_) throws SaveFailedException {
		if (!(rr_ instanceof ReactionRule))
			throw new SaveFailedException(rr_ + " isn't a ReactionRule");
		ReactionRule rr = (ReactionRule)rr_;
		
		appendChildIfNotNull(e,
			processRedex(newElement(RULE, "rule:redex"), rr.getRedex()));
		
		try {
			if (getModel().getChanges().size() != 0)
				getModel().getReactum().tryApplyChange(
						getModel().getChanges().inverse());
		} catch (ChangeRejectedException ex) {
			throw new SaveFailedException(ex);
		}
		
		appendChildIfNotNull(e,
			processChanges(newElement(RULE, "rule:changes"), rr.getChanges()));
		return e;
	}
	
	private Element processRedex(Element e, Bigraph redex) throws SaveFailedException {
		applyAttributes(e, "xmlns:bigraph", IRedNamespaceConstants.BIGRAPH);
		BigraphXMLSaver ex = new BigraphXMLSaver();
		ex.setModel(redex);
		ex.setDocument(getDocument());
		return ex.processObject(e, ex.getModel());
	}
	
	private Element processChanges(Element e, ChangeGroup changes) throws SaveFailedException {
		applyAttributes(e, "xmlns:change", CHANGE);
		return _processChanges(e, changes);
	}
	
	private Element _serialiseChange(Change i_) {
		Element f = null;
		
		if (i_ instanceof ChangeFillColour) {
			ChangeFillColour i = (ChangeFillColour)i_;
			if ((i.getCreator() instanceof Layoutable)) {
				Layoutable l = (Layoutable)i.getCreator();
				f = applyAttributes(newElement(BIG_RED, "big-red:fill"),
						"name", l.getName(),
						"type", l.getType().toLowerCase(),
						"colour", i.newColour.toHexString());
			}
		} else if (i_ instanceof ChangeOutlineColour) {
			ChangeOutlineColour i = (ChangeOutlineColour)i_;
			if ((i.getCreator() instanceof Layoutable)) {
				Layoutable l = (Layoutable)i.getCreator();
				f = applyAttributes(newElement(BIG_RED, "big-red:outline"),
						"name", l.getName(),
						"type", l.getType().toLowerCase(),
						"colour", i.newColour.toHexString());
			}
		} else if (i_ instanceof ChangeGroup) {
			f = newElement(CHANGE, "change:group");
			for (Change c : (ChangeGroup)i_) {
				Element e = _serialiseChange(c);
				if (e != null)
					f.appendChild(e);
			}
		} else if (i_ instanceof ChangeLayout) {
			ChangeLayout i = (ChangeLayout)i_;
			f = applyAttributes(newElement(BIG_RED, "big-red:layout"),
					"name", i.getCreator().getName(),
					"type", i.getCreator().getType().toLowerCase(),
					"x", i.newLayout.x(),
					"y", i.newLayout.y(),
					"width", i.newLayout.width(),
					"height", i.newLayout.height());
		} else if (i_ instanceof ChangeAddChild) {
			ChangeAddChild i = (ChangeAddChild)i_;
			f = applyAttributes(newElement(CHANGE, "change:add"),
					"name", i.name,
					"type", i.child.getType().toLowerCase());
			if (!(i.getCreator() instanceof Bigraph))
				applyAttributes(f,
						"parent", i.getCreator().getName(),
						"parent-type", i.getCreator().getType().toLowerCase());
			if (i.child instanceof Node)
				applyAttributes(f,
						"control", ((Node)i.child).getControl().getName());
		} else if (i_ instanceof ChangeRemove) {
			ChangeRemove i = (ChangeRemove)i_;
			f = applyAttributes(newElement(CHANGE, "change:remove"),
					"name", i.getCreator().getName(),
					"type", i.getCreator().getType().toLowerCase());
		} else if (i_ instanceof ChangeName) {
			ChangeName i = (ChangeName)i_;
			f = applyAttributes(newElement(CHANGE, "change:rename"),
					"name", i.getCreator().getName(), 
					"type", i.getCreator().getType().toLowerCase(),
					"new-name", i.newName);
		} else if (i_ instanceof ChangeConnect) {
			ChangeConnect i = (ChangeConnect)i_;
			f = applyAttributes(newElement(CHANGE, "change:connect"),
					"name", i.getCreator().getName(),
					"link", i.link.getName());
			if (i.getCreator() instanceof Port)
				applyAttributes(f,
						"node", ((Port)i.getCreator()).getParent().getName());
		} else if (i_ instanceof ChangeDisconnect) {
			ChangeDisconnect i = (ChangeDisconnect)i_;
			f = applyAttributes(newElement(CHANGE, "change:disconnect"),
					"name", i.getCreator().getName());
			if (i.getCreator() instanceof Port)
				applyAttributes(f,
						"node", ((Port)i.getCreator()).getParent().getName());
		} else if (i_ instanceof ChangeAlias) {
			ChangeAlias i = (ChangeAlias)i_;
			f = applyAttributes(newElement(CHANGE, "change:site-alias"),
					"name", i.getCreator().getName());
			if (i.alias != null)
				applyAttributes(f, "alias", i.alias);
		} else if (i_ instanceof ChangeComment) {
			ChangeComment i = (ChangeComment)i_;
			f = applyAttributes(newElement(BIG_RED, "big-red:comment"),
					"name", ((Layoutable)i.getCreator()).getName(),
					"type", ((Layoutable)i.getCreator()).getType().toLowerCase());
			if (i.comment != null)
				applyAttributes(f, "comment", i.comment);
		}
		
		return f;
	}
	
	private Element _processChanges(Element e, ChangeGroup changes) throws SaveFailedException {
		for (Change i_ : changes) {
			Element f = _serialiseChange(i_);
			
			if (e != null && f != null) {
				e.appendChild(f);
				try {
					getModel().getReactum().tryApplyChange(i_);
				} catch (ChangeRejectedException ex) {
					throw new SaveFailedException(ex);
				}
			}
		}
		
		return e;
	}
}
