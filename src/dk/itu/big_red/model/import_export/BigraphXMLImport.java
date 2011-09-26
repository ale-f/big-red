package dk.itu.big_red.model.import_export;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
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
import dk.itu.big_red.util.UI;

/**
 * XMLImport reads a XML document and produces a corresponding {@link Bigraph}.
 * @author alec
 * @see BigraphXMLExport
 *
 */
public class BigraphXMLImport extends Import<Bigraph> {
	private enum AppearanceStatus {
		NOTHING_YET,
		MANDATORY,
		FORBIDDEN
	}
	
	boolean warnedAboutLayouts = false;
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
		
		if (as == AppearanceStatus.FORBIDDEN)
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
		
		boolean warn = false;
		
		Element el = DOM.removeNamedChildElement(e, XMLNS.BIG_RED, "appearance");
		switch (as) {
		case FORBIDDEN:
			warn = (el != null);
			break;
		case MANDATORY:
			warn = (el == null && !(model instanceof Port));
			break;
		case NOTHING_YET:
			as = (el != null ? AppearanceStatus.MANDATORY :
				AppearanceStatus.FORBIDDEN);
			break;
		}

		if (warn && !warnedAboutLayouts) {
			UI.showMessageBox(SWT.ICON_WARNING, "All or nothing!",
				"Some objects in this bigraph have layout data, and some don't. " +
				"Big Red ignores layout data unless all objects have it.");
			as = AppearanceStatus.FORBIDDEN;
			warnedAboutLayouts = true;
		}
		
		if (el != null && as == AppearanceStatus.MANDATORY) {
			if (as == AppearanceStatus.NOTHING_YET)
				as = AppearanceStatus.MANDATORY;
			AppearanceGenerator.setAppearance(el, model);
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
