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
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.model.assistants.ModelFactory;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.utilities.DOM;
import dk.itu.big_red.utilities.Tristate;
import dk.itu.big_red.utilities.resources.IFileBackable;
import dk.itu.big_red.utilities.resources.Project;
import dk.itu.big_red.utilities.ui.UI;

/**
 * XMLImport reads a XML document and produces a corresponding {@link Bigraph}.
 * @author alec
 * @see BigraphXMLExport
 *
 */
public class BigraphXMLImport extends Import<Bigraph> implements IFileBackable {
	private boolean partialAppearanceWarning;
	private Tristate appearanceAllowed;
	private ChangeGroup cg = new ChangeGroup();
	
	@Override
	public Bigraph importObject() throws ImportFailedException {
		try {
			Document d =
				DOM.validate(DOM.parse(source), RedPlugin.getResource("resources/schema/bigraph.xsd"));
			return makeBigraph(d.getDocumentElement()).setFile(getFile());
		} catch (Exception e) {
			if (e instanceof ImportFailedException) {
				throw (ImportFailedException)e;
			} else throw new ImportFailedException(e);
		}
	}
	
	private Bigraph bigraph = null;
	
	public Bigraph makeBigraph(Element e) throws ImportFailedException {
		if (e == null)
			throw new ImportFailedException("Element is null");
		
		bigraph = new Bigraph();
		
		partialAppearanceWarning = false;
		appearanceAllowed = Tristate.UNKNOWN;
		cg.clear();
		
		Element signatureElement =
			DOM.removeNamedChildElement(e, XMLNS.BIGRAPH, "signature");
		
		String signaturePath;
		if (signatureElement != null) {
			signaturePath =
				DOM.getAttributeNS(signatureElement, XMLNS.BIGRAPH, "src");
		} else {
			signaturePath = DOM.getAttributeNS(e, XMLNS.BIGRAPH, "signature");
		}
		
		if (signaturePath != null) {
			IFile sigFile = null;
			if (getFile() != null)
				sigFile =
					Project.findFileByPath(getFile().getParent(),
							new Path(signaturePath));
			
			if (sigFile == null) { /* backwards compatibility */
				sigFile = 
					Project.findFileByPath(null, new Path(signaturePath));
				if (sigFile == null)
					throw new ImportFailedException("The signature \"" + signaturePath + "\" does not exist.");
			}
				
			Signature sig = (Signature)Import.fromFile(sigFile);
			bigraph.setSignature(sig);
		} else if (signatureElement != null) {
			SignatureXMLImport si = new SignatureXMLImport();
			bigraph.setSignature(si.makeSignature(signatureElement));
		} else {
			throw new ImportFailedException("The bigraph does not define or reference a signature.");
		}
		
		processContainer(e, bigraph);
		
		try {
			if (cg.size() != 0)
				bigraph.tryApplyChange(cg);
			if (appearanceAllowed == Tristate.FALSE)
				bigraph.tryApplyChange(bigraph.relayout());
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
		if (link != null)
			cg.add(model.changeConnect(links.get(link)));
		return model;
	}
	
	private Site processSite(Element e, Site model) throws ImportFailedException {
		String alias = DOM.getAttributeNS(e, XMLNS.BIGRAPH, "alias");
		if (alias != null)
			cg.add(model.changeAlias(alias));
		return model;
	}
	
	private void addChild(Container context, Element e) throws ImportFailedException {
		ModelObject model = null;
		boolean port = false;
		if (e.getLocalName().equals("node")) {
			String controlName =
					DOM.getAttributeNS(e, XMLNS.BIGRAPH, "control");
			Control c = bigraph.getSignature().getControl(controlName);
			if (c == null)
				throw new ImportFailedException(
					"The control \"" + controlName + "\" isn't defined by " +
							"this bigraph's signature.");
			model = new Node(c);
		} else if (e.getLocalName().equals("port") && context instanceof Node) {
			/*
			 * <port /> tags shouldn't actually create anything, so let the
			 * special handling commence!
			 */
			port = true;
		} else {
			model = ModelFactory.getNewObject(e.getLocalName());
		}

		if (model instanceof Layoutable) {
			Layoutable l = (Layoutable)model;
			cg.add(context.changeAddChild(l,
					DOM.getAttributeNS(e, XMLNS.BIGRAPH, "name")));
			
			Element appearance =
				DOM.removeNamedChildElement(e, XMLNS.BIG_RED, "appearance");
			if (appearanceAllowed == Tristate.UNKNOWN) {
				appearanceAllowed = Tristate.fromBoolean(appearance != null);
			} else if (!partialAppearanceWarning &&
					    (appearanceAllowed == Tristate.FALSE &&
					     appearance != null) ||
					    (appearanceAllowed == Tristate.TRUE &&
					     appearance == null)) {
				UI.showMessageBox(SWT.ICON_WARNING, "All or nothing!",
					"Some objects in this bigraph have layout data, and some don't. " +
					"Big Red ignores layout data unless all objects have it.");
				appearanceAllowed = Tristate.FALSE;
				partialAppearanceWarning = true;
			}
			
			if (appearance != null && appearanceAllowed == Tristate.TRUE)
				AppearanceGenerator.setAppearance(appearance, model, cg);
		}
		
		if (model instanceof Container) {
			processContainer(e, (Container)model);
		} else if (port) {
			Node n = (Node)context;
			processPoint(e,
				n.getPort(DOM.getAttributeNS(e, XMLNS.BIGRAPH, "name")));
		} else if (model instanceof Link) {
			processLink(e, (Link)model);
		} else if (model instanceof InnerName) {
			processPoint(e, (InnerName)model);
		} else if (model instanceof Site) {
			processSite(e, (Site)model);
		}
	}

	private IFile file;
	
	@Override
	public IFile getFile() {
		return file;
	}

	@Override
	public BigraphXMLImport setFile(IFile file) {
		this.file = file;
		return this;
	}
}
