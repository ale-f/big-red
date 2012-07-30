package dk.itu.big_red.model.load_save.loaders;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Control;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.ModelObject.ChangeExtendedData;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.Point;
import org.bigraph.model.Port;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Root;
import org.bigraph.model.Site;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.loaders.LoaderNotice;
import org.bigraph.model.resources.IFileWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.model.Colour;
import dk.itu.big_red.model.ColourUtilities;
import dk.itu.big_red.model.ExtendedDataUtilities;
import dk.itu.big_red.model.LayoutUtilities;
import dk.itu.big_red.model.load_save.RedXMLUndecorator;
import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.RULE;
import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.CHANGE;
import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.BIG_RED;

public class ReactionRuleXMLLoader extends XMLLoader {
	private ReactionRule rr = null;
	
	@Override
	public ReactionRule importObject() throws LoadFailedException {
		try {
			Document d = validate(parse(getInputStream()),
					RedPlugin.getResource("resources/schema/rule.xsd"));
			ReactionRule rr = makeObject(d.getDocumentElement());
			FileData.setFile(rr, getFile());
			return rr;
		} catch (Exception e) {
			if (e instanceof LoadFailedException) {
				throw (LoadFailedException)e;
			} else throw new LoadFailedException(e);
		}
	}

	public ReactionRule makeObject(Element e) throws LoadFailedException {
		rr = new ReactionRule();
		
		rr.setRedex(
			makeRedex(getNamedChildElement(e, RULE, "redex")));
		updateReactum(rr, getNamedChildElement(e, RULE, "changes"));
		
		return executeUndecorators(rr, e);
	}
	
	private Bigraph makeRedex(Element e) throws LoadFailedException {
		return new BigraphXMLLoader().setFile(getFile()).makeObject(e);
	}
	
	private Layoutable.Identifier getLayoutable(
			String type, String name) {
		if ("site".equals(type)) {
			return new Site.Identifier(name);
		} else if ("node".equals(type) || "root".equals(type) ||
				type == null) {
			return getContainer(type, name);
		} else if ("edge".equals(type)) {
			return new Edge.Identifier(name);
		} else if ("outername".equals(type)) {
			return new OuterName.Identifier(name);
		} else if ("link".equals(type)) {
			Link.Identifier l = _getScratchLinkIdentifier(name);
			if (l == null)
				throw new Error(
						"getLayoutable(String, String) can't retrieve an " +
						"identifier for the nonexistent bare link " + name);
			return l;
		} else if ("innername".equals(type)) {
			return new InnerName.Identifier(name);
		} else return null;
	}
	
	private Container.Identifier getContainer(
			String type, String name) {
		if ("node".equals(type)) {
			Node.Identifier n = _getScratchNodeIdentifier(name);
			if (n == null)
				throw new Error(
						"getContainer(String, String) can only retrieve " +
						"identifiers for Nodes that already exist");
			return n;
		} else if ("root".equals(type)) {
			return new Root.Identifier(name);
		} else if (type == null && name == null) {
			return new Bigraph.Identifier();
		} else return null;
	}
	
	private Node.Identifier _getScratchNodeIdentifier(String name) {
		Node n = (Node)
				rr.getReactum().getNamespace(Node.class).get(scratch, name);
		return (n != null ? n.getIdentifier(scratch) : null);
	}
	
	private Link.Identifier _getScratchLinkIdentifier(String name) {
		Link l = (Link)
				rr.getReactum().getNamespace(Link.class).get(scratch, name);
		return (l != null ? l.getIdentifier(scratch) : null);
	}
	
	private IChangeDescriptor changeDescriptorFromElement(org.w3c.dom.Node n) {
		IChangeDescriptor cd = null;
		if (!(n instanceof Element))
			return null;
		Element el = (Element)n;
		if (el.getNamespaceURI().equals(CHANGE)) {
			if (el.getLocalName().equals("group")) {
				ChangeDescriptorGroup cdg = new ChangeDescriptorGroup();
				NodeList nl = el.getChildNodes();
				for (int i = 0; i < nl.getLength(); i++) {
					IChangeDescriptor cp =
							changeDescriptorFromElement(nl.item(i));
					if (cp != null)
						cdg.add(cp);
				}
				
				return (cdg.size() > 0 ? cdg : null);
			} else if (el.getLocalName().equals("add")) {
				String
					name = org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, CHANGE, "name"),
					type = org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, CHANGE, "type"),
					parentName = org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, CHANGE, "parent"),
					parentType = org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, CHANGE, "parent-type");
				Container.Identifier parent =
						getContainer(parentType, parentName);
				Layoutable.Identifier child = null;
				
				if (type.equals("node")) {
					String control = org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, CHANGE, "control");
					child = new Node.Identifier(name,
							new Control.Identifier(control));
				} else child = getLayoutable(type, name);
				
				if (parent != null)
					cd = new Container.ChangeAddChildDescriptor(parent, child);
			} else if (el.getLocalName().equals("remove")) {
				String
					name = org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, CHANGE, "name"),
					type = org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, CHANGE, "type");
				Layoutable.Identifier l = getLayoutable(type, name);
				
				if (l != null)
					cd = new Layoutable.ChangeRemoveDescriptor(l);
			} else if (el.getLocalName().equals("rename")) {
				String
					name = org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, CHANGE, "name"),
					type = org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, CHANGE, "type"),
					newName = org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, CHANGE, "new-name");
				Layoutable.Identifier l = getLayoutable(type, name);
				
				if (l != null)
					cd = new Layoutable.ChangeNameDescriptor(l, newName);
			} else if (el.getLocalName().equals("connect")) {
				String
					name = org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, CHANGE, "name"),
					link = org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, CHANGE, "link"),
					node = org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, CHANGE, "node");
				Point.Identifier p;
				if (node != null) {
					p = new Port.Identifier(name,
							_getScratchNodeIdentifier(node));
				} else p = new InnerName.Identifier(name);
				
				Link.Identifier l = _getScratchLinkIdentifier(link);
				if (p != null && l != null)
					cd = new Point.ChangeConnectDescriptor(p, l);
			} else if (el.getLocalName().equals("disconnect")) {
				String
					name = org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, CHANGE, "name"),
					node = org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, CHANGE, "node");
				Point.Identifier p = null;
				if (node != null) {
					p = new Port.Identifier(name,
						_getScratchNodeIdentifier(node));
				} else p = new InnerName.Identifier(name);
				
				if (p != null)
					cd = new Point.ChangeDisconnectDescriptor(p);
			} else if (el.getLocalName().equals("site-alias")) {
				String
					name = org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, CHANGE, "name"),
					alias = org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, CHANGE, "alias");
				Site.Identifier s = new Site.Identifier(name);
				
				if (s != null)
					cd = ExtendedDataUtilities.changeAliasDescriptor(s, alias);
			} else if (el.getLocalName().equals("node-parameter")) {
				String
					name = org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, CHANGE, "name"),
					parameter = org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, CHANGE, "parameter");
				Node.Identifier o =
						new Node.Identifier(name,
						_getScratchNodeIdentifier(name).getControl());
				
				if (o != null)
					cd = ExtendedDataUtilities.changeParameterDescriptor(
							o, parameter);
			}
		} else if (el.getNamespaceURI().equals(BIG_RED)) {
			if (el.getLocalName().equals("layout")) {
				String
					type =
						org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, BIG_RED, "type"),
					name =
						org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, BIG_RED, "name");
				Layoutable.Identifier l = getLayoutable(type, name);
				
				if (l != null)
					cd = LayoutUtilities.changeLayoutDescriptor(l,
							RedXMLUndecorator.getRectangle(el));
			} else if (el.getLocalName().equals("fill")) {
				String
					colour =
						org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, BIG_RED, "colour"),
					type =
						org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, BIG_RED, "type"),
					name =
						org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, BIG_RED, "name");
				Layoutable.Identifier l = getLayoutable(type, name);
				
				if (l != null)
					cd = ColourUtilities.changeFillDescriptor(l,
							new Colour(colour));
			} else if (el.getLocalName().equals("outline")) {
				String
					colour =
						org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, BIG_RED, "colour"),
					type =
						org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, BIG_RED, "type"),
					name =
						org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, BIG_RED, "name");
				Layoutable.Identifier l = getLayoutable(type, name);
				
				if (l != null)
					cd = ColourUtilities.changeOutlineDescriptor(l,
							new Colour(colour));
			} else if (el.getLocalName().equals("comment")) {
				String
					comment =
						org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, BIG_RED, "comment"),
					type =
						org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, BIG_RED, "type"),
					name =
						org.bigraph.model.loaders.XMLLoader.getAttributeNS(el, BIG_RED, "name");
				Layoutable.Identifier l = getLayoutable(type, name);
				
				if (l != null)
					cd = ExtendedDataUtilities.changeCommentDescriptor(
							l, comment);
			}
		}
		if (cd != null) {
			try {
				scratch.executeChange(
						cd.createChange(scratch, rr.getReactum()));
			} catch (ChangeCreationException cce) {
				cd = null;
			}
		}
		return cd;
	}
	
	private PropertyScratchpad scratch = new PropertyScratchpad();
	
	private void updateReactum(ReactionRule rr, Element e) throws LoadFailedException {
		Bigraph reactum = rr.getReactum();
		NodeList nl = e.getChildNodes();
		ChangeDescriptorGroup cdg = rr.getChanges();
		for (int i = 0; i < nl.getLength(); i++) {
			IChangeDescriptor c = changeDescriptorFromElement(nl.item(i));
			if (c != null)
				cdg.add(c);
		}
		
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
