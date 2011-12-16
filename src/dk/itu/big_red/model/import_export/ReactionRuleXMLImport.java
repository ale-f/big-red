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
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
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
				if (el.getLocalName().equals("add-root")) {
					c = reactum.changeAddChild(new Root(),
							chattr(el, "name"));
				} else if (el.getLocalName().equals("add-edge")) {
					c = reactum.changeAddChild(new Edge(),
							chattr(el, "name"));
				} else if (el.getLocalName().equals("add-inner-name")) {
					c = reactum.changeAddChild(new InnerName(),
							chattr(el, "name"));
				} else if (el.getLocalName().equals("add-outer-name")) {
					c = reactum.changeAddChild(new OuterName(),
							chattr(el, "name"));
				} else if (el.getLocalName().equals("add-node-to-root")) {
					c = ((Root)reactum.getNamespace(Root.class).
							get(chattr(el, "parent"))).
							changeAddChild(
									new dk.itu.big_red.model.Node(
											reactum.getSignature().
											getControl(chattr(el, "control"))),
											chattr(el, "name"));
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
