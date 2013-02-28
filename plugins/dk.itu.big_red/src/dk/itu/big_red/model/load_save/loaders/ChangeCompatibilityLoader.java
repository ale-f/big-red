package dk.itu.big_red.model.load_save.loaders;

import static org.bigraph.model.loaders.RedNamespaceConstants.CHANGE;

import java.util.Arrays;

import org.bigraph.extensions.param.ParameterUtilities;
import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Control;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.NamedModelObject;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.Point;
import org.bigraph.model.Port;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Root;
import org.bigraph.model.Site;
import org.bigraph.model.Store;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.loaders.ReactionRuleXMLLoader;
import org.bigraph.model.process.IParticipant;
import org.bigraph.model.process.IParticipantHost;
import org.w3c.dom.Element;
import dk.itu.big_red.model.Colour;
import dk.itu.big_red.model.ColourUtilities;
import dk.itu.big_red.model.ExtendedDataUtilities;
import dk.itu.big_red.model.ExtendedDataUtilities.ChangeAliasDescriptor;
import dk.itu.big_red.model.ExtendedDataUtilities.ChangeCommentDescriptor;
import dk.itu.big_red.model.LayoutUtilities;
import dk.itu.big_red.model.LayoutUtilities.ChangeLayoutDescriptor;
import dk.itu.big_red.model.load_save.RedXMLUndecorator;

import static org.bigraph.model.loaders.XMLLoader.getAttributeNS;
import static org.bigraph.model.utilities.ArrayIterable.forNodeList;
import static dk.itu.big_red.model.BigRedNamespaceConstants.BIG_RED;

@Deprecated
public final class ChangeCompatibilityLoader
		implements ReactionRuleXMLLoader.CompatibilityChangeLoader,
		IParticipant {
	private ReactionRule rr;
	private Bigraph reactum;
	private final PropertyScratchpad scratch = new PropertyScratchpad();
	
	@Override
	public void setHost(IParticipantHost host) {
		if (host instanceof ReactionRuleXMLLoader)
			((ReactionRuleXMLLoader)host).setCCL(this);
	}
	
	@Override
	public void setReactionRule(ReactionRule rr) {
		this.rr = rr;
		reactum = null;
	}
	
	private Bigraph getReactum() {
		if (reactum == null)
			reactum = rr.getRedex().clone();
		return reactum;
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
		} else if ("innername".equals(type)) {
			return new InnerName.Identifier(name);
		} else return null;
	}
	
	private Container.Identifier getContainer(
			String type, String name) {
		if ("node".equals(type)) {
			Node.Identifier n = _getScratchNodeIdentifier(name);
			if (n == null)
				throw new RuntimeException(
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
				getReactum().getNamespace(Node.class).get(scratch, name);
		return (n != null ? n.getIdentifier(scratch) : null);
	}
	
	private Link.Identifier _getScratchLinkIdentifier(String name) {
		Link l = (Link)
				getReactum().getNamespace(Link.class).get(scratch, name);
		return (l != null ? l.getIdentifier(scratch) : null);
	}
	
	@Override
	public IChangeDescriptor changeDescriptorFromElement(org.w3c.dom.Node n) {
		IChangeDescriptor cd = null;
		if (!(n instanceof Element))
			return null;
		Element el = (Element)n;
		if (el.getNamespaceURI().equals(CHANGE)) {
			if (el.getLocalName().equals("group")) {
				ChangeDescriptorGroup cdg = new ChangeDescriptorGroup();
				for (org.w3c.dom.Node i : forNodeList(el.getChildNodes())) {
					IChangeDescriptor cp =
							changeDescriptorFromElement(i);
					if (cp != null)
						cdg.add(cp);
				}
				
				return (cdg.size() > 0 ? cdg : null);
			} else if (el.getLocalName().equals("add")) {
				String
					name = getAttributeNS(el, CHANGE, "name"),
					type = getAttributeNS(el, CHANGE, "type"),
					parentName = getAttributeNS(el, CHANGE, "parent"),
					parentType = getAttributeNS(el, CHANGE, "parent-type");
				Container.Identifier parent =
						getContainer(parentType, parentName);
				Layoutable.Identifier child = null;
				
				if (type.equals("node")) {
					String control = getAttributeNS(el, CHANGE, "control");
					child = new Node.Identifier(name,
							new Control.Identifier(control));
				} else child = getLayoutable(type, name);
				
				if (parent != null)
					cd = new Container.ChangeAddChildDescriptor(parent, child);
			} else if (el.getLocalName().equals("remove")) {
				String
					name = getAttributeNS(el, CHANGE, "name"),
					type = getAttributeNS(el, CHANGE, "type");
				Layoutable.Identifier l = getLayoutable(type, name);
				
				if (l != null)
					cd = new ChangeDescriptorGroup(Arrays.asList(
							new Store.ToStoreDescriptor(
									l, Store.getInstance().createID()),
							new Container.ChangeRemoveChildDescriptor(
									l.lookup(scratch, getReactum()).
									getParent(scratch).getIdentifier(scratch),
									l)));
			} else if (el.getLocalName().equals("rename")) {
				String
					name = getAttributeNS(el, CHANGE, "name"),
					type = getAttributeNS(el, CHANGE, "type"),
					newName = getAttributeNS(el, CHANGE, "new-name");
				Layoutable.Identifier l = getLayoutable(type, name);
				
				if (l != null)
					cd = new NamedModelObject.ChangeNameDescriptor(l, newName);
			} else if (el.getLocalName().equals("connect")) {
				String
					name = getAttributeNS(el, CHANGE, "name"),
					link = getAttributeNS(el, CHANGE, "link"),
					node = getAttributeNS(el, CHANGE, "node");
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
					name = getAttributeNS(el, CHANGE, "name"),
					node = getAttributeNS(el, CHANGE, "node");
				Point.Identifier p = null;
				if (node != null) {
					p = new Port.Identifier(name,
						_getScratchNodeIdentifier(node));
				} else p = new InnerName.Identifier(name);
				
				Link.Identifier l = p.lookup(scratch, getReactum()).
						getLink(scratch).getIdentifier(scratch);
				if (p != null)
					cd = new Point.ChangeDisconnectDescriptor(p, l);
			} else if (el.getLocalName().equals("site-alias")) {
				String
					name = getAttributeNS(el, CHANGE, "name"),
					alias = getAttributeNS(el, CHANGE, "alias");
				Site.Identifier s = new Site.Identifier(name);
				Site si = s.lookup(scratch, getReactum());
				
				if (s != null)
					cd = new ChangeAliasDescriptor(s,
							ExtendedDataUtilities.getAlias(scratch, si),
							alias);
			} else if (el.getLocalName().equals("node-parameter")) {
				String
					name = getAttributeNS(el, CHANGE, "name"),
					parameter = getAttributeNS(el, CHANGE, "parameter");
				Node.Identifier o = _getScratchNodeIdentifier(name);
				Node no = o.lookup(scratch, getReactum());
				
				if (o != null)
					cd = ParameterUtilities.changeParameterDescriptor(o,
							ParameterUtilities.getParameter(scratch, no),
							parameter);
			}
		} else if (el.getNamespaceURI().equals(BIG_RED)) {
			if (el.getLocalName().equals("layout")) {
				String
					type =
						getAttributeNS(el, BIG_RED, "type"),
					name =
						getAttributeNS(el, BIG_RED, "name");
				Layoutable.Identifier l = getLayoutable(type, name);
				Layoutable la = l.lookup(scratch, getReactum());
				
				if (l != null)
					cd = new ChangeLayoutDescriptor(l, LayoutUtilities.getLayout(scratch, la), RedXMLUndecorator.getRectangle(el));
			} else if (el.getLocalName().equals("fill")) {
				String
					colour =
						getAttributeNS(el, BIG_RED, "colour"),
					type =
						getAttributeNS(el, BIG_RED, "type"),
					name =
						getAttributeNS(el, BIG_RED, "name");
				Layoutable.Identifier l = getLayoutable(type, name);
				Layoutable la = l.lookup(scratch, getReactum());
				
				if (l != null)
					cd = new ColourUtilities.ChangeFillDescriptor(l,
							ColourUtilities.getFill(scratch, la),
							(colour != null ? new Colour(colour) : null));
			} else if (el.getLocalName().equals("outline")) {
				String
					colour =
						getAttributeNS(el, BIG_RED, "colour"),
					type =
						getAttributeNS(el, BIG_RED, "type"),
					name =
						getAttributeNS(el, BIG_RED, "name");
				Layoutable.Identifier l = getLayoutable(type, name);
				Layoutable la = l.lookup(scratch, getReactum());
				
				if (l != null)
					cd = new ColourUtilities.ChangeOutlineDescriptor(l,
							ColourUtilities.getOutline(scratch, la),
							(colour != null ? new Colour(colour) : null));
			} else if (el.getLocalName().equals("comment")) {
				String
					comment =
						getAttributeNS(el, BIG_RED, "comment"),
					type =
						getAttributeNS(el, BIG_RED, "type"),
					name =
						getAttributeNS(el, BIG_RED, "name");
				Layoutable.Identifier l = getLayoutable(type, name);
				Layoutable la = l.lookup(scratch, getReactum());
				
				if (l != null)
					cd = new ChangeCommentDescriptor(l,
							ExtendedDataUtilities.getComment(scratch, la),
							comment);
			}
		}
		if (cd != null)
			cd.simulate(scratch, getReactum());
		return cd;
	}
}
