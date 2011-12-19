package dk.itu.big_red.model.import_export;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dk.itu.big_red.import_export.Import;
import dk.itu.big_red.import_export.ImportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.model.assistants.ModelFactory;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.util.DOM;

public class ReactionRuleXMLImport extends Import<ReactionRule> {
	private ReactionRule rr = null;
	
	@Override
	public ReactionRule importObject() throws ImportFailedException {
		try {
			Document d = DOM.parse(source);
			source.close();
			return makeRule(d.getDocumentElement());
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
		BigraphXMLImport im = new BigraphXMLImport();
		return im.makeBigraph(e);
	}
	
	private static String chattr(Element e, String name) {
		return DOM.getAttributeNS(e, XMLNS.CHANGE, name);
	}
	
	private void updateReactum(ReactionRule rr, Element e) throws ImportFailedException {
		Bigraph reactum = rr.getReactum();
		for (Node i : DOM.iterableChildren(e)) {
			Change c = null;
			if (i instanceof Element &&
					i.getNamespaceURI().equals(XMLNS.CHANGE)) {
				Element el = (Element)i;
				System.out.println(el.getLocalName());
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
						Map<String, Layoutable> ns =
							reactum.getNamespace(Bigraph.getNSI(parentType));
						parent = ((Container)ns.get(parentName));
					}
					Layoutable child = null;
					
					if (type.equals("node")) {
						String control = chattr(el, "control");
						child =
							new dk.itu.big_red.model.Node(
								reactum.getSignature().getControl(control));
					} else child = (Layoutable)ModelFactory.getNewObject(type);
					
					c = parent.changeAddChild(child, name);
				} else if (el.getLocalName().equals("rename")) {
					String
						name = chattr(el, "name"),
						type = chattr(el, "type"),
						newName = chattr(el, "new-name");
					Map<String, Layoutable> ns =
						reactum.getNamespace(Bigraph.getNSI(type));
					
					c = ns.get(name).changeName(newName);
				} else if (el.getLocalName().equals("connect")) {
					String
						name = chattr(el, "name"),
						link = chattr(el, "link"),
						node = chattr(el, "node");
					Map<String, Layoutable> nsl =
						reactum.getNamespace(Bigraph.getNSI("link"));
					Link l = (Link)nsl.get(link);
					if (node != null) {
						Map<String, Layoutable> ns =
							reactum.getNamespace(Bigraph.getNSI("node"));
						Port p =
							((dk.itu.big_red.model.Node)ns.get(node)).getPort(name);
						if (p != null) {
							c = p.changeConnect(l);
						} else throw new ImportFailedException("Port failed");
					}
				} else if (el.getLocalName().equals("disconnect")) {
					String
						name = chattr(el, "name"),
						node = chattr(el, "node");
					Map<String, Layoutable> ns = null;
					Point p;
					if (node != null) {
						ns = reactum.getNamespace(Bigraph.getNSI("node"));
						p = ((dk.itu.big_red.model.Node)ns.get(node)).getPort(name);
					} else {
						ns = reactum.getNamespace(Bigraph.getNSI("innername"));
						p = (Point)ns.get(name);
					}
					if (p != null) {
						c = p.changeDisconnect(p.getLink());
					} else throw new ImportFailedException("Can't disconnect");
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
					Map<String, Layoutable> ns =
							reactum.getNamespace(Bigraph.getNSI(type));
					c = ns.get(name).changeLayout(
							AppearanceGenerator.elementToRectangle(el));
				}
			}
			if (c != null) {
				try {
					System.out.print(c + "...");
					reactum.tryApplyChange(c);
					rr.getChanges().add(c);
					System.out.println("success!");
				} catch (ChangeRejectedException cre) {
					throw new ImportFailedException(cre);
				}
			}
		}
	}
	
	public static ReactionRule importFile(IFile file) throws ImportFailedException {
		ReactionRuleXMLImport b = new ReactionRuleXMLImport();
		try {
			b.setInputStream(file.getContents());
		} catch (CoreException e) {
			throw new ImportFailedException(e);
		}
		return b.importObject().setFile(file);
	}
}
