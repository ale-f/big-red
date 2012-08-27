package org.bigraph.model.loaders;

import java.util.Locale;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Control;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.Point;
import org.bigraph.model.Root;
import org.bigraph.model.Signature;
import org.bigraph.model.Site;
import org.bigraph.model.assistants.FileData;
import org.bigraph.model.resources.IFileWrapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static org.bigraph.model.loaders.RedNamespaceConstants.BIGRAPH;
import static org.bigraph.model.loaders.RedNamespaceConstants.SIGNATURE;

/**
 * XMLImport reads a XML document and produces a corresponding {@link Bigraph}.
 * @author alec
 */
public class BigraphXMLLoader extends XMLLoader {
	public BigraphXMLLoader() {
	}
	
	public BigraphXMLLoader(Loader parent) {
		super(parent);
	}
	
	@Override
	public Bigraph importObject() throws LoadFailedException {
		try {
			Document d = validate(parse(getInputStream()),
					Schemas.getBigraphSchema());
			Bigraph b = makeObject(d.getDocumentElement());
			FileData.setFile(b, getFile());
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
		cycleCheck();
		bigraph = loadRelative(
				getAttributeNS(e, BIGRAPH, "src"), Bigraph.class, this);
		if (bigraph != null) {
			return bigraph;
		} else bigraph = new Bigraph();
		
		Signature s = loadSub(
				selectFirst(
					getNamedChildElement(e, SIGNATURE, "signature"),
					getNamedChildElement(e, BIGRAPH, "signature")),
				BIGRAPH, Signature.class, new SignatureXMLLoader(this));
		if (s != null) {
			bigraph.setSignature(s);
		} else throw new LoadFailedException(
				"The bigraph does not define or reference a signature.");
		
		processContainer(e, bigraph);
		
		executeUndecorators(bigraph, e);
		executeChanges(bigraph);
		return bigraph;
	}
	
	private void processContainer(Element e, Container model) throws LoadFailedException {
		for (Element i : getChildElements(e))
			addChild(model, i);
	}
	
	private void processPoint(Element e, Point model) throws LoadFailedException {
		if (model != null) {
			String linkName = getAttributeNS(e, BIGRAPH, "link");
			if (linkName != null) {
				Link link = (Link)bigraph.getNamespace(Link.class).get(
						getScratch(), linkName);
				if (link != null)
					addChange(model.changeConnect(link));
			}
		} else {
			addNotice(LoaderNotice.Type.WARNING,
					"Invalid point referenced; skipping.");
		}
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

		if (model instanceof Layoutable)
			addChange(context.changeAddChild(
					(Layoutable)model, getAttributeNS(e, BIGRAPH, "name")));
		
		if (model instanceof Container) {
			processContainer(e, (Container)model);
		} else if (port) {
			Node n = (Node)context;
			processPoint(e,
				n.getPort(getAttributeNS(e, BIGRAPH, "name")));
		} else if (model instanceof InnerName) {
			processPoint(e, (InnerName)model);
		}
		
		if (model != null)
			executeUndecorators(model, e);
	}

	@Override
	public BigraphXMLLoader setFile(IFileWrapper f) {
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
}
