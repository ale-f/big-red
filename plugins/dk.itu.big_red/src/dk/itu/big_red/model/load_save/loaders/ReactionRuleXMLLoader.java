package dk.itu.big_red.model.load_save.loaders;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Control;
import org.bigraph.model.InnerName;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Layoutable.ChangeDescriptorGroup;
import org.bigraph.model.Layoutable.IChangeDescriptor;
import org.bigraph.model.Link;
import org.bigraph.model.Node;
import org.bigraph.model.Point;
import org.bigraph.model.Port;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Root;
import org.bigraph.model.Site;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.eclipse.core.resources.IFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dk.itu.big_red.editors.assistants.Colour;
import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.model.load_save.LoadFailedException;
import dk.itu.big_red.model.load_save.savers.RedXMLDecorator;
import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.RULE;
import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.CHANGE;
import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.BIG_RED;

public class ReactionRuleXMLLoader extends XMLLoader {
	private ReactionRule rr = null;
	
	@Override
	public ReactionRule importObject() throws LoadFailedException {
		try {
			Document d =
					validate(parse(source), "resources/schema/rule.xsd");
			ReactionRule rr = makeObject(d.getDocumentElement());
			ExtendedDataUtilities.setFile(rr, getFile());
			return rr;
		} catch (Exception e) {
			if (e instanceof LoadFailedException) {
				throw (LoadFailedException)e;
			} else throw new LoadFailedException(e);
		}
	}

	@Override
	public ReactionRule makeObject(Element e) throws LoadFailedException {
		rr = new ReactionRule();
		
		rr.setRedex(
			makeRedex(getNamedChildElement(e, RULE, "redex")));
		updateReactum(rr, getNamedChildElement(e, RULE, "changes"));
		
		return executeUndecorators(rr, e);
	}
	
	private Bigraph makeRedex(Element e) throws LoadFailedException {
		BigraphXMLLoader im =
				newLoader(BigraphXMLLoader.class).setFile(getFile());
		return im.makeObject(e);
	}
	
	private static Link.Identifier getLink(String name) {
		return new Link.Identifier(name);
	}
	
	private static Layoutable.Identifier getLayoutable(
			String type, String name) {
		if ("site".equals(type)) {
			return new Site.Identifier(name);
		} else if ("node".equals(type) || "root".equals(type) ||
				(type == null && name != null)) {
			return getContainer(type, name);
		} else if ("edge".equals(type) || "outername".equals(type) ||
				"link".equals(type)) {
			return getLink(name);
		} else if ("innername".equals(type)) {
			return new InnerName.Identifier(name);
		} else return null;
	}
	
	private static Container.Identifier getContainer(
			String type, String name) {
		if ("node".equals(type)) {
			return new Node.Identifier(name, null);
		} else if ("root".equals(type)) {
			return new Root.Identifier(name);
		} else if (type == null && name == null) {
			return new Bigraph.Identifier();
		} else return null;
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
				
				if (cdg.size() > 0)
					cd = cdg;
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
					cd = new Layoutable.ChangeRemoveDescriptor(l);
			} else if (el.getLocalName().equals("rename")) {
				String
					name = getAttributeNS(el, CHANGE, "name"),
					type = getAttributeNS(el, CHANGE, "type"),
					newName = getAttributeNS(el, CHANGE, "new-name");
				Layoutable.Identifier l = getLayoutable(type, name);
				
				if (l != null)
					cd = new Layoutable.ChangeNameDescriptor(l, newName);
			} else if (el.getLocalName().equals("connect")) {
				String
					name = getAttributeNS(el, CHANGE, "name"),
					link = getAttributeNS(el, CHANGE, "link"),
					node = getAttributeNS(el, CHANGE, "node");
				Point.Identifier p;
				if (node != null) {
					p = new Port.Identifier(name,
						new Node.Identifier(node, null));
				} else p = new InnerName.Identifier(name);
				
				Link.Identifier l = getLink(link);
				if (p != null && l != null)
					cd = new Point.ChangeConnectDescriptor(p, l);
			} else if (el.getLocalName().equals("disconnect")) {
				String
					name = getAttributeNS(el, CHANGE, "name"),
					node = getAttributeNS(el, CHANGE, "node");
				Point.Identifier p = null;
				if (node != null) {
					p = new Port.Identifier(name,
						new Node.Identifier(node, null));
				} else p = new InnerName.Identifier(name);
				
				if (p != null)
					cd = new Point.ChangeDisconnectDescriptor(p);
			} else if (el.getLocalName().equals("site-alias")) {
				String
					name = getAttributeNS(el, CHANGE, "name"),
					alias = getAttributeNS(el, CHANGE, "alias");
				Site.Identifier s = new Site.Identifier(name);
				
				if (s != null)
					cd = ExtendedDataUtilities.changeAliasDescriptor(s, alias);
			} else if (el.getLocalName().equals("node-parameter")) {
				String
					name = getAttributeNS(el, CHANGE, "name"),
					parameter = getAttributeNS(el, CHANGE, "parameter");
				Node.Identifier o =
						new Node.Identifier(name, null);
				
				if (o != null)
					cd = ExtendedDataUtilities.changeParameterDescriptor(
							o, parameter);
			}
		} else if (el.getNamespaceURI().equals(BIG_RED)) {
			if (el.getLocalName().equals("layout")) {
				String
					type =
						getAttributeNS(el, BIG_RED, "type"),
					name =
						getAttributeNS(el, BIG_RED, "name");
				Layoutable.Identifier l = getLayoutable(type, name);
				
				if (l != null)
					cd = ExtendedDataUtilities.changeLayoutDescriptor(l,
							RedXMLDecorator.getRectangle(el));
			} else if (el.getLocalName().equals("fill")) {
				String
					colour =
						getAttributeNS(el, BIG_RED, "colour"),
					type =
						getAttributeNS(el, BIG_RED, "type"),
					name =
						getAttributeNS(el, BIG_RED, "name");
				Layoutable.Identifier l = getLayoutable(type, name);
				
				if (l != null)
					cd = ExtendedDataUtilities.changeFillDescriptor(l,
							new Colour(colour));
			} else if (el.getLocalName().equals("outline")) {
				String
					colour =
						getAttributeNS(el, BIG_RED, "colour"),
					type =
						getAttributeNS(el, BIG_RED, "type"),
					name =
						getAttributeNS(el, BIG_RED, "name");
				Layoutable.Identifier l = getLayoutable(type, name);
				
				if (l != null)
					cd = ExtendedDataUtilities.changeOutlineDescriptor(l,
							new Colour(colour));
			} else if (el.getLocalName().equals("comment")) {
				String
					comment =
						getAttributeNS(el, BIG_RED, "comment"),
					type =
						getAttributeNS(el, BIG_RED, "type"),
					name =
						getAttributeNS(el, BIG_RED, "name");
				Layoutable.Identifier l = getLayoutable(type, name);
				
				if (l != null)
					cd = ExtendedDataUtilities.changeCommentDescriptor(
							l, comment);
			}
		}
		return cd;
	}
	
	private void updateReactum(ReactionRule rr, Element e) throws LoadFailedException {
		Bigraph reactum = rr.getReactum();
		NodeList nl = e.getChildNodes();
		ChangeDescriptorGroup cdg = rr.getChanges();
		for (int i = 0; i < nl.getLength(); i++) {
			IChangeDescriptor c = changeDescriptorFromElement(nl.item(i));
			if (c != null)
				cdg.add(c);
		}
		
		ChangeGroup cg = cdg.createChange(reactum, null);
		try {
			reactum.tryValidateChange(cg);
		} catch (ChangeRejectedException cre) {
			throw new LoadFailedException(cre);
			/* XXX
			Change ch = cre.getRejectedChange();
			if (ch instanceof ChangeExtendedData) {
				ChangeExtendedData cd = (ChangeExtendedData)ch;
				if (ExtendedDataUtilities.LAYOUT.equals(cd.key)) {
					addNotice(Notice.WARNING,
							"Layout data invalid.");
					cg.add(ExtendedDataUtilities.relayout(scratch, reactum));
				}
			}
			*/
		}
		
		try {
			reactum.tryApplyChange(cg);
		} catch (ChangeRejectedException cre) {
			throw new LoadFailedException(cre);
		}
	}
	
	@Override
	public ReactionRuleXMLLoader setFile(IFile f) {
		return (ReactionRuleXMLLoader)super.setFile(f);
	}
}
