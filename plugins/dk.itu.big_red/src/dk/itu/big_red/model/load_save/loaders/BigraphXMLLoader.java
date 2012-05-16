package dk.itu.big_red.model.load_save.loaders;

import java.util.HashMap;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.load_save.LoadFailedException;
import dk.itu.big_red.model.load_save.savers.BigraphXMLSaver;
import dk.itu.big_red.model.names.policies.INamePolicy;
import dk.itu.big_red.utilities.resources.Project;

import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.BIG_RED;
import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.BIGRAPH;

/**
 * XMLImport reads a XML document and produces a corresponding {@link Bigraph}.
 * @author alec
 * @see BigraphXMLSaver
 *
 */
public class BigraphXMLLoader extends XMLLoader {
	private enum Tristate {
		TRUE,
		FALSE,
		UNKNOWN;
		
		private static Tristate fromBoolean(boolean b) {
			return (b ? TRUE : FALSE);
		}
	}
	
	private boolean partialAppearanceWarning;
	private Tristate appearanceAllowed;
	private ChangeGroup cg = new ChangeGroup();
	
	@Override
	public Bigraph importObject() throws LoadFailedException {
		try {
			Document d =
					validate(parse(source), "resources/schema/bigraph.xsd");
			Bigraph b = makeObject(d.getDocumentElement());
			ExtendedDataUtilities.setFile(b, getFile());
			return b;
		} catch (LoadFailedException e) {
			throw e;
		} catch (Exception e) {
			throw new LoadFailedException(e);
		}
	}
	
	private Bigraph bigraph = null;
	
	@Override
	public Bigraph makeObject(Element e) throws LoadFailedException {
		if (e == null)
			throw new LoadFailedException("Element is null");
		
		bigraph = new Bigraph();
		
		partialAppearanceWarning = false;
		appearanceAllowed = Tristate.UNKNOWN;
		cg.clear();
		
		Element signatureElement =
			getNamedChildElement(e, BIGRAPH, "signature");
		
		String signaturePath;
		if (signatureElement != null) {
			signaturePath = getAttributeNS(signatureElement, BIGRAPH, "src");
		} else {
			signaturePath = getAttributeNS(e, BIGRAPH, "signature");
		}
		
		SignatureXMLLoader si = newLoader(SignatureXMLLoader.class);
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
					throw new LoadFailedException("The signature \"" + signaturePath + "\" does not exist.");
			}
			
			try {
				si.setFile(sigFile).setInputStream(sigFile.getContents());
			} catch (CoreException ex) {
				throw new LoadFailedException(ex);
			}
			bigraph.setSignature(si.importObject());
		} else if (signatureElement != null) {
			bigraph.setSignature(
					si.setFile(getFile()).makeObject(signatureElement));
		} else {
			throw new LoadFailedException("The bigraph does not define or reference a signature.");
		}
		
		processContainer(e, bigraph);
		
		try {
			if (cg.size() != 0)
				bigraph.tryApplyChange(cg);
			if (appearanceAllowed == Tristate.FALSE)
				bigraph.tryApplyChange(bigraph.relayout());
		} catch (ChangeRejectedException f) {
			throw new LoadFailedException(f);
		}
		
		return executeUndecorators(bigraph, e);
	}
	
	private void processContainer(Element e, Container model) throws LoadFailedException {
		for (Element i : getChildElements(e))
			addChild(model, i);
	}
	
	private HashMap<String, Link> links =
			new HashMap<String, Link>();
	
	private void processLink(Element e, Link model) throws LoadFailedException {
		links.put(getAttributeNS(e, BIGRAPH, "name"), model);
	}
	
	private void processPoint(Element e, Point model) throws LoadFailedException {
		String link = getAttributeNS(e, BIGRAPH, "link");
		if (link != null)
			cg.add(model.changeConnect(links.get(link)));
	}
	
	private void processSite(Element e, Site model) throws LoadFailedException {
		String alias = getAttributeNS(e, BIGRAPH, "alias");
		if (alias != null)
			cg.add(model.changeAlias(alias));
	}
	
	private void processNode(Element e, Node model) throws LoadFailedException {
		String parameter = getAttributeNS(e, BIGRAPH, "parameter");
		INamePolicy policy = ExtendedDataUtilities.getParameterPolicy(model.getControl());
		
		if (parameter != null && policy == null) {
			addNotice(new Status(IStatus.WARNING, RedPlugin.PLUGIN_ID,
				"Spurious parameter value ignored.")); /* FIXME - details */
		} else if (parameter == null && policy != null) {
			addNotice(new Status(IStatus.WARNING, RedPlugin.PLUGIN_ID,
				"Default parameter value assigned.")); /* FIXME - details */
			cg.add(model.changeParameter(policy.get(0)));
		} else if (parameter != null && policy != null) {
			cg.add(model.changeParameter(parameter));
		}
		
		processContainer(e, model);
	}
	
	private void addChild(Container context, Element e) throws LoadFailedException {
		ModelObject model = null;
		boolean port = false;
		if (e.getLocalName().equals("node")) {
			String controlName = getAttributeNS(e, BIGRAPH, "control");
			Control c = bigraph.getSignature().getControl(controlName);
			if (c == null)
				throw new LoadFailedException(
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
			model = BigraphXMLLoader.getNewObject(e.getLocalName());
		}

		if (model instanceof Layoutable) {
			Layoutable l = (Layoutable)model;
			cg.add(context.changeAddChild(l,
					getAttributeNS(e, BIGRAPH, "name")));
			
			Element appearance = getNamedChildElement(e, BIG_RED, "appearance");
			if (appearanceAllowed == Tristate.UNKNOWN) {
				appearanceAllowed = Tristate.fromBoolean(appearance != null);
			} else if (!partialAppearanceWarning &&
					    (appearanceAllowed == Tristate.FALSE &&
					     appearance != null) ||
					    (appearanceAllowed == Tristate.TRUE &&
					     appearance == null)) {
				addNotice(new Status(IStatus.WARNING, RedPlugin.PLUGIN_ID,
					"The layout data for this bigraph is incomplete and so " +
					"has been ignored."));
				appearanceAllowed = Tristate.FALSE;
				partialAppearanceWarning = true;
			}
			
			if (appearance != null && appearanceAllowed == Tristate.TRUE)
				elementToAppearance(appearance, model, cg);
		}
		
		if (model instanceof Node) {
			processNode(e, (Node)model);
		} else if (model instanceof Container) {
			processContainer(e, (Container)model);
		} else if (port) {
			Node n = (Node)context;
			processPoint(e,
				n.getPort(getAttributeNS(e, BIGRAPH, "name")));
		} else if (model instanceof Link) {
			processLink(e, (Link)model);
		} else if (model instanceof InnerName) {
			processPoint(e, (InnerName)model);
		} else if (model instanceof Site) {
			processSite(e, (Site)model);
		}
		
		if (model != null)
			executeUndecorators(model, e);
	}

	@Override
	public BigraphXMLLoader setFile(IFile f) {
		return (BigraphXMLLoader)super.setFile(f);
	}

	/**
	 * Creates a new object of the named type.
	 * @param typeName a type name (not case sensitive)
	 * @return a new object of the appropriate type, or <code>null</code> if
	 *          the type name was unrecognised
	 * @see ModelObject#getType()
	 */
	static ModelObject getNewObject(String typeName) {
		typeName = typeName.toLowerCase(Locale.ENGLISH);
		if (typeName.equals("bigraph"))
			return new Bigraph();
		else if (typeName.equals("root"))
			return new Root();
		else if (typeName.equals("site"))
			return new Site();
		else if (typeName.equals("innername"))
			return new InnerName();
		else if (typeName.equals("outername"))
			return new OuterName();
		else if (typeName.equals("signature"))
			return new Signature();
		else if (typeName.equals("control"))
			return new Control();
		else if (typeName.equals("edge"))
			return new Edge();
		else return null;
	}

	protected static void elementToAppearance(
			Element e, ModelObject o, ChangeGroup cg) {
		if (!(e.getNamespaceURI().equals(BIG_RED) &&
				e.getLocalName().equals("appearance")))
			return;
		
		if (o instanceof Layoutable) {
			Layoutable l = (Layoutable)o;
			Rectangle r = AppearanceGenerator.elementToRectangle(e);
			cg.add(ExtendedDataUtilities.changeLayout(l, r));
		}
	}
}
