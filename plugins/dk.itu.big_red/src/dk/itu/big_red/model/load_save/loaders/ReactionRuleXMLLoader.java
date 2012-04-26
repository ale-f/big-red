package dk.itu.big_red.model.load_save.loaders;

import org.eclipse.core.resources.IFile;
import org.w3c.dom.Document;
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
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.model.assistants.Colour;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.load_save.LoadFailedException;
import dk.itu.big_red.model.names.INamespace;
import dk.itu.big_red.model.names.NamespaceGroup;
import dk.itu.big_red.model.names.policies.PositiveIntegerNamePolicy;
import dk.itu.big_red.model.names.policies.StringNamePolicy;

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
			return makeObject(d.getDocumentElement()).setFile(getFile());
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
		
		return rr;
	}
	
	private Bigraph makeRedex(Element e) throws LoadFailedException {
		BigraphXMLLoader im = new BigraphXMLLoader().setFile(getFile());
		return im.makeObject(e).setFile(getFile());
	}
	
	private NamespaceGroup<Layoutable> nsg = new NamespaceGroup<Layoutable>();
	{
		nsg.createNamespace(Link.class).setPolicy(new StringNamePolicy());
		nsg.createNamespace(Node.class).setPolicy(new StringNamePolicy());
		nsg.createNamespace(InnerName.class).setPolicy(new StringNamePolicy());
		
		nsg.createNamespace(Root.class).setPolicy(new PositiveIntegerNamePolicy());
		nsg.createNamespace(Site.class).setPolicy(new PositiveIntegerNamePolicy());
	}
	
	private Layoutable getNamed(String type, String name) {
		Layoutable l = nsg.getNamespace(Bigraph.getNSI(type)).get(name);
		if (l == null)
			l = rr.getReactum().getNamespace(Bigraph.getNSI(type)).get(name);
		return l;
	}
	
	private Change changeFromElement(org.w3c.dom.Node n) {
		Bigraph reactum = rr.getReactum();
		Change c = null;
		if (!(n instanceof Element))
			return null;
		Element el = (Element)n;
		if (el.getNamespaceURI().equals(CHANGE)) {
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
					name = getAttributeNS(el, CHANGE, "name"),
					type = getAttributeNS(el, CHANGE, "type"),
					parentName = getAttributeNS(el, CHANGE, "parent"),
					parentType = getAttributeNS(el, CHANGE, "parent-type");
				Container parent = null;
				if (parentName == null && parentType == null) {
					parent = reactum;
				} else {
					parent = (Container)getNamed(parentType, parentName);
				}
				Layoutable child = null;
				
				if (type.equals("node")) {
					String control = getAttributeNS(el, CHANGE, "control");
					child = new Node(
							reactum.getSignature().getControl(control));
				} else child = (Layoutable)BigraphXMLLoader.getNewObject(type);
				c = parent.changeAddChild(child, name);
				nsg.getNamespace(Bigraph.getNSI(type)).put(name, child);
			} else if (el.getLocalName().equals("remove")) {
				String
					name = getAttributeNS(el, CHANGE, "name"),
					type = getAttributeNS(el, CHANGE, "type");
				c = getNamed(type, name).changeRemove();
				nsg.getNamespace(Bigraph.getNSI(type)).remove(name);
			} else if (el.getLocalName().equals("rename")) {
				String
					name = getAttributeNS(el, CHANGE, "name"),
					type = getAttributeNS(el, CHANGE, "type"),
					newName = getAttributeNS(el, CHANGE, "new-name");
				Layoutable l = getNamed(type, name);
				c = l.changeName(newName);
				INamespace<Layoutable> ns =
						nsg.getNamespace(Bigraph.getNSI(type));
				ns.remove(name);
				ns.put(newName, l);
			} else if (el.getLocalName().equals("connect")) {
				String
					name = getAttributeNS(el, CHANGE, "name"),
					link = getAttributeNS(el, CHANGE, "link"),
					node = getAttributeNS(el, CHANGE, "node");
				Point p;
				if (node != null) {
					p = ((Node)getNamed("node", node)).getPort(name);
				} else p = (InnerName)getNamed("innername", name);
				c = p.changeConnect((Link)getNamed("link", link));
			} else if (el.getLocalName().equals("disconnect")) {
				String
					name = getAttributeNS(el, CHANGE, "name"),
					node = getAttributeNS(el, CHANGE, "node");
				Point p;
				if (node != null) {
					p = ((Node)getNamed("node", node)).getPort(name);
				} else p = (InnerName)getNamed("innername", name);
				c = p.changeDisconnect();
			} else if (el.getLocalName().equals("site-alias")) {
				String
					name = getAttributeNS(el, CHANGE, "name"),
					alias = getAttributeNS(el, CHANGE, "alias");
				c = ((Site)getNamed("site", name)).
						changeAlias(alias);
			}
		} else if (el.getNamespaceURI().equals(BIG_RED)) {
			if (el.getLocalName().equals("layout")) {
				String
					type =
						getAttributeNS(el, BIG_RED, "type"),
					name =
						getAttributeNS(el, BIG_RED, "name");
				c = getNamed(type, name).changeLayout(
						AppearanceGenerator.elementToRectangle(el));
			} else if (el.getLocalName().equals("fill")) {
				String
					colour =
						getAttributeNS(el, BIG_RED, "colour"),
					type =
						getAttributeNS(el, BIG_RED, "type"),
					name =
						getAttributeNS(el, BIG_RED, "name");
				c = getNamed(type, name).changeFillColour(
						new Colour(colour));
			} else if (el.getLocalName().equals("outline")) {
				String
					colour =
						getAttributeNS(el, BIG_RED, "colour"),
					type =
						getAttributeNS(el, BIG_RED, "type"),
					name =
						getAttributeNS(el, BIG_RED, "name");
				c = getNamed(type, name).changeOutlineColour(
						new Colour(colour));
			} else if (el.getLocalName().equals("comment")) {
				String
					comment =
						getAttributeNS(el, BIG_RED, "comment"),
					type =
						getAttributeNS(el, BIG_RED, "type"),
					name =
						getAttributeNS(el, BIG_RED, "name");
				c = getNamed(type, name).changeComment(comment);
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
