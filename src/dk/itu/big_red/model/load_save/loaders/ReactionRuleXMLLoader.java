package dk.itu.big_red.model.load_save.loaders;

import org.eclipse.core.resources.IFile;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.model.assistants.ModelFactory;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.load_save.LoadFailedException;
import dk.itu.big_red.model.load_save.IRedNamespaceConstants;
import dk.itu.big_red.utilities.Colour;

public class ReactionRuleXMLLoader extends XMLLoader {
	private ReactionRule rr = null;
	
	@Override
	public ReactionRule importObject() throws LoadFailedException {
		try {
			return makeObject(parse(source).getDocumentElement()).
					setFile(getFile());
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
			makeRedex(getNamedChildElement(e, IRedNamespaceConstants.RULE, "redex")));
		updateReactum(rr, getNamedChildElement(e, IRedNamespaceConstants.RULE, "changes"));
		
		return rr;
	}
	
	private Bigraph makeRedex(Element e) throws LoadFailedException {
		BigraphXMLLoader im = new BigraphXMLLoader().setFile(getFile());
		return im.makeObject(e).setFile(getFile());
	}
	
	private static String chattr(Element e, String name) {
		return getAttributeNS(e, IRedNamespaceConstants.CHANGE, name);
	}
	
	private static Layoutable getNamed(Bigraph b, String type, String name) {
		return b.getNamespace(Bigraph.getNSI(type)).get(name);
	}
	
	private Change changeFromElement(org.w3c.dom.Node n) {
		Bigraph reactum = rr.getReactum();
		Change c = null;
		if (!(n instanceof Element))
			return null;
		Element el = (Element)n;
		if (el.getNamespaceURI().equals(IRedNamespaceConstants.CHANGE)) {
			if (el.getLocalName().equals("group")) {
				ChangeGroup cg = new ChangeGroup();
				NodeList nl = el.getChildNodes();
				for (int i = 0; i < nl.getLength(); i++) {
					Change cp = changeFromElement(nl.item(i));
					if (cp != null)
						cg.add(cp);
				}
				
				c = cg;
			} else if (el.getLocalName().equals("add")) {
				String
					name = chattr(el, "name"),
					type = chattr(el, "type"),
					parentName = chattr(el, "parent"),
					parentType = chattr(el, "parent-type");
				Container parent = null;
				if (parentName == null && parentType == null) {
					parent = reactum;
				} else {
					parent = (Container)getNamed(reactum, parentType, parentName);
				}
				Layoutable child = null;
				
				if (type.equals("node")) {
					String control = chattr(el, "control");
					child = new Node(
							reactum.getSignature().getControl(control));
				} else child = (Layoutable)ModelFactory.getNewObject(type);
				c = parent.changeAddChild(child, name);
			} else if (el.getLocalName().equals("remove")) {
				String
					name = chattr(el, "name"),
					type = chattr(el, "type");
				Layoutable child =
					getNamed(reactum, type, name);
				Container parent = child.getParent();
				c = parent.changeRemoveChild(child);
			} else if (el.getLocalName().equals("rename")) {
				String
					name = chattr(el, "name"),
					type = chattr(el, "type"),
					newName = chattr(el, "new-name");
				c = getNamed(reactum, type, name).changeName(newName);
			} else if (el.getLocalName().equals("connect")) {
				String
					name = chattr(el, "name"),
					link = chattr(el, "link"),
					node = chattr(el, "node");
				Point p;
				if (node != null) {
					p = ((Node)getNamed(reactum, "node", node)).getPort(name);
				} else p = (InnerName)getNamed(reactum, "innername", name);
				c = p.changeConnect((Link)getNamed(reactum, "link", link));
			} else if (el.getLocalName().equals("disconnect")) {
				String
					name = chattr(el, "name"),
					node = chattr(el, "node");
				Point p;
				if (node != null) {
					p = ((Node)getNamed(reactum, "node", node)).getPort(name);
				} else p = (InnerName)getNamed(reactum, "innername", name);
				c = p.changeDisconnect(p.getLink());
			} else if (el.getLocalName().equals("site-alias")) {
				String
					name = chattr(el, "name"),
					alias = chattr(el, "alias");
				c = ((Site)getNamed(reactum, "site", name)).
						changeAlias(alias);
			}
		} else if (el.getNamespaceURI().equals(IRedNamespaceConstants.BIG_RED)) {
			if (el.getLocalName().equals("layout")) {
				String
					type =
						getAttributeNS(el, IRedNamespaceConstants.BIG_RED, "type"),
					name =
						getAttributeNS(el, IRedNamespaceConstants.BIG_RED, "name");
				c = getNamed(reactum, type, name).changeLayout(
						AppearanceGenerator.elementToRectangle(el));
			} else if (el.getLocalName().equals("fill")) {
				String
					colour =
						getAttributeNS(el, IRedNamespaceConstants.BIG_RED, "colour"),
					type =
						getAttributeNS(el, IRedNamespaceConstants.BIG_RED, "type"),
					name =
						getAttributeNS(el, IRedNamespaceConstants.BIG_RED, "name");
				c = getNamed(reactum, type, name).changeFillColour(
						new Colour(colour));
			} else if (el.getLocalName().equals("outline")) {
				String
					colour =
						getAttributeNS(el, IRedNamespaceConstants.BIG_RED, "colour"),
					type =
						getAttributeNS(el, IRedNamespaceConstants.BIG_RED, "type"),
					name =
						getAttributeNS(el, IRedNamespaceConstants.BIG_RED, "name");
				c = getNamed(reactum, type, name).changeOutlineColour(
						new Colour(colour));
			}
		}
		return c;
	}
	
	private void updateReactum(ReactionRule rr, Element e) throws LoadFailedException {
		Bigraph reactum = rr.getReactum();
		NodeList nl = e.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Change c = changeFromElement(nl.item(i));
			if (c != null) {
				try {
					reactum.tryApplyChange(c);
					rr.getChanges().add(c);
				} catch (ChangeRejectedException cre) {
					throw new LoadFailedException(cre);
				}
			}
		}
	}
	
	@Override
	public ReactionRuleXMLLoader setFile(IFile f) {
		return (ReactionRuleXMLLoader)super.setFile(f);
	}
}
