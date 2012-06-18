package dk.itu.big_red.model.load_save.loaders;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.InnerName;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.Node;
import org.bigraph.model.Point;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Site;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.names.Namespace;
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
	
	private PropertyScratchpad scratch = new PropertyScratchpad();
	
	private Layoutable getNamed(Bigraph b, String type, String name) {
		Namespace<Layoutable> ns = b.getNamespace(Bigraph.getNSI(type));
		Layoutable l;
		return ((l = ns.get(scratch, name)) != null ? l : ns.get(name));
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
					parent = (Container)getNamed(reactum, parentType, parentName);
				}
				Layoutable child = null;
				
				if (type.equals("node")) {
					String control = getAttributeNS(el, CHANGE, "control");
					child = new Node(
							reactum.getSignature().getControl(control));
				} else child = (Layoutable)BigraphXMLLoader.getNewObject(type);
				c = parent.changeAddChild(child, name);
				parent.addChild(scratch, child, name);
			} else if (el.getLocalName().equals("remove")) {
				String
					name = getAttributeNS(el, CHANGE, "name"),
					type = getAttributeNS(el, CHANGE, "type");
				Layoutable l = getNamed(reactum, type, name);
				c = l.changeRemove();
				l.getParent(scratch).removeChild(scratch, l);
			} else if (el.getLocalName().equals("rename")) {
				String
					name = getAttributeNS(el, CHANGE, "name"),
					type = getAttributeNS(el, CHANGE, "type"),
					newName = getAttributeNS(el, CHANGE, "new-name");
				Layoutable l = getNamed(reactum, type, name);
				c = l.changeName(newName);
				l.setName(scratch, newName);
			} else if (el.getLocalName().equals("connect")) {
				String
					name = getAttributeNS(el, CHANGE, "name"),
					link = getAttributeNS(el, CHANGE, "link"),
					node = getAttributeNS(el, CHANGE, "node");
				Point p;
				if (node != null) {
					p = ((Node)getNamed(reactum, "node", node)).getPort(name);
				} else p = (InnerName)getNamed(reactum, "innername", name);
				c = p.changeConnect((Link)getNamed(reactum, "link", link));
			} else if (el.getLocalName().equals("disconnect")) {
				String
					name = getAttributeNS(el, CHANGE, "name"),
					node = getAttributeNS(el, CHANGE, "node");
				Point p;
				if (node != null) {
					p = ((Node)getNamed(reactum, "node", node)).getPort(name);
				} else p = (InnerName)getNamed(reactum, "innername", name);
				c = p.changeDisconnect();
			} else if (el.getLocalName().equals("site-alias")) {
				String
					name = getAttributeNS(el, CHANGE, "name"),
					alias = getAttributeNS(el, CHANGE, "alias");
				c = ExtendedDataUtilities.changeAlias(
						(Site)getNamed(reactum, "site", name), alias);
			} else if (el.getLocalName().equals("node-parameter")) {
				String
					name = getAttributeNS(el, CHANGE, "name"),
					parameter = getAttributeNS(el, CHANGE, "parameter");
				c = ExtendedDataUtilities.changeParameter(
						(Node)getNamed(reactum, "node", name), parameter);
			}
		} else if (el.getNamespaceURI().equals(BIG_RED)) {
			if (el.getLocalName().equals("layout")) {
				String
					type =
						getAttributeNS(el, BIG_RED, "type"),
					name =
						getAttributeNS(el, BIG_RED, "name");
				c = ExtendedDataUtilities.changeLayout(
						getNamed(reactum, type, name),
						RedXMLDecorator.getRectangle(el));
			} else if (el.getLocalName().equals("fill")) {
				String
					colour =
						getAttributeNS(el, BIG_RED, "colour"),
					type =
						getAttributeNS(el, BIG_RED, "type"),
					name =
						getAttributeNS(el, BIG_RED, "name");
				c = ExtendedDataUtilities.changeFill(
						getNamed(reactum, type, name), new Colour(colour));
			} else if (el.getLocalName().equals("outline")) {
				String
					colour =
						getAttributeNS(el, BIG_RED, "colour"),
					type =
						getAttributeNS(el, BIG_RED, "type"),
					name =
						getAttributeNS(el, BIG_RED, "name");
				c = ExtendedDataUtilities.changeOutline(
						getNamed(reactum, type, name), new Colour(colour));
			} else if (el.getLocalName().equals("comment")) {
				String
					comment =
						getAttributeNS(el, BIG_RED, "comment"),
					type =
						getAttributeNS(el, BIG_RED, "type"),
					name =
						getAttributeNS(el, BIG_RED, "name");
				c = ExtendedDataUtilities.changeComment(
						getNamed(reactum, type, name), comment);
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
