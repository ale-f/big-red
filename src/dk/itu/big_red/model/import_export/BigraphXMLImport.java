package dk.itu.big_red.model.import_export;

import java.util.HashMap;

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
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.model.assistants.ModelFactory;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeConnect;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeName;
import dk.itu.big_red.util.DOM;
import dk.itu.big_red.util.UI;
import dk.itu.big_red.util.resources.Project;

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
	private ChangeGroup cg = new ChangeGroup();
	
	@Override
	public Bigraph importObject() throws ImportFailedException {
		try {
			Document d =
				DOM.validate(DOM.parse(source), RedPlugin.getResource("schema/bigraph.xsd"));
			source.close();
			return makeBigraph(d.getDocumentElement());
		} catch (Exception e) {
			if (e instanceof ImportFailedException) {
				throw (ImportFailedException)e;
			} else throw new ImportFailedException(e);
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
			cg.add(bigraph.relayout());
		
		try {
			bigraph.tryApplyChange(cg);
		} catch (ChangeRejectedException f) {
			throw new ImportFailedException(f);
		}
		
		return bigraph;
	}
	
	private Container processContainer(Element e, Container model) throws ImportFailedException {
		for (Element i : DOM.getChildElements(e))
			addChild(model, i);
		return model;
	}
	
	private HashMap<String, Link> links =
			new HashMap<String, Link>();
	
	private Link processLink(Element e, Link model) throws ImportFailedException {
		String name = DOM.getAttributeNS(e, XMLNS.BIGRAPH, "name");
		links.put(name, model);
		
		return model;
	}
	
	private Point processPoint(Element e, Point model) throws ImportFailedException {
		String link = DOM.getAttributeNS(e, XMLNS.BIGRAPH, "link");
		cg.add(new BigraphChangeConnect(model, links.get(link)));
		return model;
	}
	
	private void addChild(Container context, Element e) throws ImportFailedException {
		Object model = null;
		boolean port = false;
		if (e.getNodeName().equals("node")) {
			String controlName =
					DOM.getAttributeNS(e, XMLNS.BIGRAPH, "control");
			Control c = bigraph.getSignature().getControl(controlName);
			if (c == null)
				throw new ImportFailedException(
					"The control \"" + controlName + "\" isn't defined by " +
							"this bigraph's signature.");
			model = new Node(c);
		} else if (e.getNodeName().equals("port") && context instanceof Node) {
			/*
			 * <port /> tags shouldn't actually create anything, so let the
			 * special handling commence!
			 */
			port = true;
		} else {
			model = ModelFactory.getNewObject(e.getNodeName());
		}

		
		if (model instanceof Layoutable) {
			cg.add(context.changeAddChild((Layoutable)model));
			
			if (!(model instanceof Bigraph)) {
				String name = DOM.getAttributeNS(e, XMLNS.BIGRAPH, "name");
				cg.add(new BigraphChangeName((Layoutable)model, name));
			}
			
			boolean warn = false;
			Element appearance =
					DOM.removeNamedChildElement(e, XMLNS.BIG_RED, "appearance");
			switch (as) {
			case FORBIDDEN:
				warn = (appearance != null);
				break;
			case MANDATORY:
				warn = (appearance == null && !(model instanceof Port));
				break;
			case NOTHING_YET:
				as = (appearance != null ? AppearanceStatus.MANDATORY :
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
			
			if (appearance != null && as == AppearanceStatus.MANDATORY) {
				if (as == AppearanceStatus.NOTHING_YET)
					as = AppearanceStatus.MANDATORY;
				AppearanceGenerator.setAppearance(appearance, model, cg);
			}
		}
		
		if (model instanceof Container) {
			processContainer(e, (Container)model);
		} else if (port) {
			Node n = (Node)context;
			for (Port p : n.getPorts()) {
				if (p.getName().equals(e.getAttribute("name"))) {
					processPoint(e, p);
					break;
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
