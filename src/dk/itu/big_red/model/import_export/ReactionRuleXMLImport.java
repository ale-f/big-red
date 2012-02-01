package dk.itu.big_red.model.import_export;

import org.eclipse.core.resources.IFile;
import org.w3c.dom.Element;

import dk.itu.big_red.import_export.Import;
import dk.itu.big_red.import_export.ImportFailedException;
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
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.utilities.Colour;
import dk.itu.big_red.utilities.DOM;
import dk.itu.big_red.utilities.resources.IFileBackable;

public class ReactionRuleXMLImport extends Import<ReactionRule> implements IFileBackable {
	private ReactionRule rr = null;
	
	@Override
	public ReactionRule importObject() throws ImportFailedException {
		try {
			return makeRule(DOM.parse(source).getDocumentElement()).
					setFile(getFile());
		} catch (Exception e) {
			if (e instanceof ImportFailedException) {
				throw (ImportFailedException)e;
			} else throw new ImportFailedException(e);
		}
	}

	public ReactionRule makeRule(Element e) throws ImportFailedException {
		rr = new ReactionRule();
		
		rr.setRedex(
			makeRedex(DOM.getNamedChildElement(e, XMLNS.RULE, "redex")));
		updateReactum(rr, DOM.getNamedChildElement(e, XMLNS.RULE, "changes"));
		
		return rr;
	}
	
	private Bigraph makeRedex(Element e) throws ImportFailedException {
		BigraphXMLImport im = new BigraphXMLImport().setFile(getFile());
		return im.makeBigraph(e).setFile(getFile());
	}
	
	private static String chattr(Element e, String name) {
		return DOM.getAttributeNS(e, XMLNS.CHANGE, name);
	}
	
	private static Layoutable getNamed(Bigraph b, String type, String name) {
		return b.getNamespace(Bigraph.getNSI(type)).get(name);
	}
	
	private void updateReactum(ReactionRule rr, Element e) throws ImportFailedException {
		Bigraph reactum = rr.getReactum();
		for (org.w3c.dom.Node i : DOM.iterableChildren(e)) {
			Change c = null;
			if (i instanceof Element &&
					i.getNamespaceURI().equals(XMLNS.CHANGE)) {
				Element el = (Element)i;
				if (el.getLocalName().equals("add")) {
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
			} else if (i instanceof Element &&
					i.getNamespaceURI().equals(XMLNS.BIG_RED)) {
				Element el = (Element)i;
				
				if (el.getLocalName().equals("layout")) {
					String
						type =
							DOM.getAttributeNS(el, XMLNS.BIG_RED, "type"),
						name =
							DOM.getAttributeNS(el, XMLNS.BIG_RED, "name");
					c = getNamed(reactum, type, name).changeLayout(
							AppearanceGenerator.elementToRectangle(el));
				} else if (el.getLocalName().equals("fill")) {
					String
						colour =
							DOM.getAttributeNS(el, XMLNS.BIG_RED, "colour"),
						type =
							DOM.getAttributeNS(el, XMLNS.BIG_RED, "type"),
						name =
							DOM.getAttributeNS(el, XMLNS.BIG_RED, "name");
					c = getNamed(reactum, type, name).changeFillColour(
							new Colour(colour));
				} else if (el.getLocalName().equals("outline")) {
					String
						colour =
							DOM.getAttributeNS(el, XMLNS.BIG_RED, "colour"),
						type =
							DOM.getAttributeNS(el, XMLNS.BIG_RED, "type"),
						name =
							DOM.getAttributeNS(el, XMLNS.BIG_RED, "name");
					c = getNamed(reactum, type, name).changeOutlineColour(
							new Colour(colour));
				}
			}
			if (c != null) {
				try {
					reactum.tryApplyChange(c);
					rr.getChanges().add(c);
				} catch (ChangeRejectedException cre) {
					throw new ImportFailedException(cre);
				}
			}
		}
	}
	
	private IFile file;
	
	@Override
	public IFile getFile() {
		return file;
	}

	@Override
	public ReactionRuleXMLImport setFile(IFile file) {
		this.file = file;
		return this;
	}
}
