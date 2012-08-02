package dk.itu.big_red.model.load_save.savers;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Edge;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.Point;
import org.bigraph.model.Port;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Root;
import org.bigraph.model.Site;
import org.bigraph.model.Container.ChangeAddChildDescriptor;
import org.bigraph.model.Layoutable.ChangeNameDescriptor;
import org.bigraph.model.Layoutable.ChangeRemoveDescriptor;
import org.bigraph.model.ModelObject.ChangeExtendedDataDescriptor;
import org.bigraph.model.Point.ChangeConnectDescriptor;
import org.bigraph.model.Point.ChangeDisconnectDescriptor;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.loaders.RedNamespaceConstants;
import org.bigraph.model.savers.SaveFailedException;
import org.bigraph.model.savers.XMLSaver;
import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Element;

import dk.itu.big_red.model.Colour;
import dk.itu.big_red.model.ColourUtilities;
import dk.itu.big_red.model.ExtendedDataUtilities;
import dk.itu.big_red.model.LayoutUtilities;
import dk.itu.big_red.model.ParameterUtilities;

import static org.bigraph.model.loaders.RedNamespaceConstants.BIG_RED;
import static org.bigraph.model.loaders.RedNamespaceConstants.CHANGE;
import static org.bigraph.model.loaders.RedNamespaceConstants.RULE;

public class ReactionRuleXMLSaver extends XMLSaver {
	public ReactionRuleXMLSaver() {
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
			processRedex(newElement(RULE, "rule:redex"), rr.getRedex()));
		appendChildIfNotNull(e,
			processChanges(newElement(RULE, "rule:changes"), rr.getChanges()));
		
		return executeDecorators(rr, e);
	}
	
	private Element processRedex(Element e, Bigraph redex) throws SaveFailedException {
		applyAttributes(e, "xmlns:bigraph", RedNamespaceConstants.BIGRAPH);
		BigraphXMLSaver ex = new BigraphXMLSaver();
		ex.addNewDecorators(getDecorators()).setDocument(getDocument()).
				setFile(getFile()).setModel(redex);
		return ex.processModel(e);
	}
	
	/* XXX: change decoration (?) */
	
	private Element processChanges(Element e, ChangeDescriptorGroup changes)
			throws SaveFailedException {
		applyAttributes(e, "xmlns:change", CHANGE);
		
		for (IChangeDescriptor i_ : changes) {
			Element f = _serialiseChange(i_);
			
			if (e != null && f != null)
				e.appendChild(f);
		}
		
		return e;
	}
	
	private String typeFor(ModelObject.Identifier i) {
		if (i instanceof Node.Identifier) {
			return "node";
		} else if (i instanceof Site.Identifier) {
			return "site";
		} else if (i instanceof Edge.Identifier) {
			return "edge";
		} else if (i instanceof OuterName.Identifier) {
			return "outername";
		} else if (i instanceof Root.Identifier) {
			return "root";
		} else return null;
	}
	
	private Element _serialiseChange(IChangeDescriptor i_) {
		Element f = null;
		
		if (i_ instanceof ChangeExtendedDataDescriptor) {
			ChangeExtendedDataDescriptor i = (ChangeExtendedDataDescriptor)i_;
			
			ModelObject.Identifier l = i.getTarget();
			String key = i.getKey();
			Object newValue = i.getNewValue();
			if (ExtendedDataUtilities.COMMENT.equals(key)) {
				f = newElement(BIG_RED, "big-red:comment");
				if (newValue != null)
					applyAttributes(f, "comment", newValue);
			} else if (ColourUtilities.FILL.equals(key)) {
				f = applyAttributes(newElement(BIG_RED, "big-red:fill"),
						"colour", ((Colour)newValue).toHexString());
			} else if (ColourUtilities.OUTLINE.equals(key)) {
				f = applyAttributes(newElement(BIG_RED, "big-red:outline"),
						"colour", ((Colour)newValue).toHexString());
			} else if (LayoutUtilities.LAYOUT.equals(key)) {
				Rectangle r = (Rectangle)newValue;
				f = applyAttributes(newElement(BIG_RED, "big-red:layout"),
						"x", r.x(), "y", r.y(),
						"width", r.width(), "height", r.height());
			} else if (ExtendedDataUtilities.ALIAS.equals(key)) {
				f = applyAttributes(newElement(CHANGE, "change:site-alias"));
				if (newValue != null)
					applyAttributes(f, "alias", newValue);
			} else if (ParameterUtilities.PARAMETER.equals(key)){
				f = applyAttributes(newElement(CHANGE, "change:node-parameter"),
						"parameter", newValue);
			}
			if (f != null)
				applyAttributes(f,
						"name", l.getName(),
						"type", typeFor(l));
		} else if (i_ instanceof ChangeDescriptorGroup) {
			f = newElement(CHANGE, "change:group");
			for (IChangeDescriptor c : (ChangeDescriptorGroup)i_) {
				Element e = _serialiseChange(c);
				if (e != null)
					f.appendChild(e);
			}
		} else if (i_ instanceof ChangeAddChildDescriptor) {
			ChangeAddChildDescriptor i = (ChangeAddChildDescriptor)i_;
			f = applyAttributes(newElement(CHANGE, "change:add"),
					"name", i.getChild().getName(),
					"type", typeFor(i.getChild()));
			Container.Identifier parent = i.getParent();
			if (!(parent instanceof Bigraph.Identifier))
				applyAttributes(f,
						"parent", parent.getName(),
						"parent-type", typeFor(parent));
			if (i.getChild() instanceof Node.Identifier)
				applyAttributes(f,
					"control", 
					((Node.Identifier)i.getChild()).getControl().getName());
		} else if (i_ instanceof ChangeRemoveDescriptor) {
			ChangeRemoveDescriptor i = (ChangeRemoveDescriptor)i_;
			f = applyAttributes(newElement(CHANGE, "change:remove"),
					"name", i.getTarget().getName(),
					"type", typeFor(i.getTarget()));
		} else if (i_ instanceof ChangeNameDescriptor) {
			ChangeNameDescriptor i = (ChangeNameDescriptor)i_;
			f = applyAttributes(newElement(CHANGE, "change:rename"),
					"name", i.getTarget().getName(), 
					"type", typeFor(i.getTarget()),
					"new-name", i.getNewName());
		} else if (i_ instanceof ChangeConnectDescriptor) {
			ChangeConnectDescriptor i = (ChangeConnectDescriptor)i_;
			Point.Identifier p = i.getPoint();
			f = applyAttributes(newElement(CHANGE, "change:connect"),
					"name", p.getName(),
					"link", i.getLink().getName());
			if (p instanceof Port.Identifier)
				applyAttributes(f,
						"node", ((Port.Identifier)p).getNode().getName());
		} else if (i_ instanceof ChangeDisconnectDescriptor) {
			ChangeDisconnectDescriptor i = (ChangeDisconnectDescriptor)i_;
			Point.Identifier p = i.getPoint();
			f = applyAttributes(newElement(CHANGE, "change:disconnect"),
					"name", p.getName());
			if (p instanceof Port.Identifier)
				applyAttributes(f,
						"node", ((Port.Identifier)p).getNode().getName());
		}
		
		return f;
	}
}
