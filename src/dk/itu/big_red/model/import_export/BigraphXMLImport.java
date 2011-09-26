package dk.itu.big_red.model.import_export;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.import_export.Import;
import dk.itu.big_red.import_export.ImportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.model.assistants.ModelFactory;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeConnect;
import dk.itu.big_red.model.interfaces.internal.INameable;
import dk.itu.big_red.util.DOM;
import dk.itu.big_red.util.Project;

/**
 * XMLImport reads a XML document and produces a corresponding {@link Bigraph}.
 * @author alec
 * @see BigraphXMLExport
 *
 */
public class BigraphXMLImport extends Import<Bigraph> {
	private enum AppearanceStatus {
		NOTHING_YET,
		APPEARANCE_MANDATORY,
		APPEARANCE_FORBIDDEN
	}
	
	private AppearanceStatus as = AppearanceStatus.NOTHING_YET;
	
	@Override
	public Bigraph importObject() throws ImportFailedException {
		try {
			Document d =
				DOM.validate(DOM.parse(source), RedPlugin.getPluginResource("schema/bigraph.xsd"));
			source.close();
			return makeBigraph(d.getDocumentElement());
		} catch (Exception e) {
			throw new ImportFailedException(e);
		}
	}
	
	private void applyChange(Change c) throws ImportFailedException {
		try {
			bigraph.tryApplyChange(c);
		} catch (ChangeRejectedException e) {
			throw new ImportFailedException(e);
		}
	}
	
	private Bigraph bigraph = null;
	
	private Bigraph makeBigraph(Element e) throws ImportFailedException {
		bigraph = new Bigraph();
		
		String signaturePath = e.getAttribute("signature");
		
		IFile sigFile =
			Project.findFileByPath(null, new Path(signaturePath));
		SignatureXMLImport si = new SignatureXMLImport();
		try {
			si.setInputStream(sigFile.getContents());
			bigraph.setSignature(sigFile, si.importObject());
		} catch (Exception ex) {
			throw new ImportFailedException(ex);
		}
		
		processContainer(e, bigraph);
		
		if (as == AppearanceStatus.APPEARANCE_FORBIDDEN)
			applyChange(bigraph.relayout());
		
		return bigraph;
	}
	
	private Container processContainer(Element e, Container model) throws ImportFailedException {
		if (model instanceof Node)
			((Node)model).setControl(bigraph.getSignature().getControl(DOM.getAttributeNS(e, XMLNS.BIGRAPH, "control")));
		
		if (model instanceof INameable)
			((INameable)model).setName(DOM.getAttributeNS(e, XMLNS.BIGRAPH, "name"));
		
		for (Element i : DOM.getChildElements(e))
			addChild(model, i);
		return model;
	}
	
	private Link processLink(Element e, Link model) throws ImportFailedException {
		model.setName(DOM.getAttributeNS(e, XMLNS.BIGRAPH, "name"));
		return model;
	}
	
	private Point processPoint(Element e, Point model) throws ImportFailedException {
		String name = DOM.getAttributeNS(e, XMLNS.BIGRAPH, "name"),
			   link = DOM.getAttributeNS(e, XMLNS.BIGRAPH, "link");
		model.setName(name);
		
		applyChange(
			new BigraphChangeConnect(model,
				(Link)bigraph.getNamespaceManager().getObject(Link.class, link)));
		return model;
	}
	
	private void addChild(Container context, Element e) throws ImportFailedException {
		Object model = ModelFactory.getNewObject(e.getNodeName());
		
		Element el = DOM.removeNamedChildElement(e, XMLNS.BIG_RED, "appearance");
		if (el != null && as != AppearanceStatus.APPEARANCE_FORBIDDEN) {
			if (as == AppearanceStatus.NOTHING_YET)
				as = AppearanceStatus.APPEARANCE_MANDATORY;
			AppearanceGenerator.setAppearance(el, model);
		} else if (!(model instanceof Port)) {
			if (as == AppearanceStatus.NOTHING_YET) {
				as = AppearanceStatus.APPEARANCE_FORBIDDEN;
			} else if (as == AppearanceStatus.APPEARANCE_MANDATORY) {
				/* Report an error? */
				as = AppearanceStatus.APPEARANCE_FORBIDDEN;
			}
		}
		
		if (model instanceof Layoutable && context != null &&
			!(model instanceof Port))
			context.addChild((Layoutable)model);
		
		if (model instanceof Container) {
			processContainer(e, (Container)model);
		} else if (model instanceof Port) {
			if (context instanceof Node) {
				Node n = (Node)context;
				for (Port p : n.getPorts()) {
					if (p.getName().equals(e.getAttribute("name"))) {
						processPoint(e, p);
						break;
					}
				}
			}
		} else if (model instanceof Link) {
			processLink(e, (Link)model);
		} else if (model instanceof InnerName) {
			processPoint(e, (InnerName)model);
		} else {
			/* fail in some other way? */;
		}
	}
}
