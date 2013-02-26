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
	
	private final Bigraph bigraph = new Bigraph();
	
	@Override
	public Bigraph getResolver() {
		return bigraph;
	}
	
	@Override
	public Bigraph makeObject(Element e) throws LoadFailedException {
		cycleCheck();
		String replacement = getAttributeNS(e, BIGRAPH, "src");
		if (replacement != null)
			return loadRelative(replacement, Bigraph.class,
					new BigraphXMLLoader(this));
		
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
		executeChanges();
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
					addChange(new Point.ChangeConnectDescriptor(
							model.getIdentifier(getScratch()),
							link.getIdentifier(getScratch())));
			}
		} else {
			addNotice(LoaderNotice.Type.WARNING,
					"Invalid point referenced; skipping.");
		}
	}
	
	private void addChild(Container context, Element e) throws LoadFailedException {
		ModelObject.Identifier modelID = null;
		boolean port = false;
		String name = getAttributeNS(e, BIGRAPH, "name");
		if (e.getLocalName().equals("node")) {
			String
				controlName = getAttributeNS(e, BIGRAPH, "control");
			modelID = new Node.Identifier(name,
					new Control.Identifier(controlName));
		} else if (e.getLocalName().equals("port") && context instanceof Node) {
			/*
			 * <port /> tags shouldn't actually create anything, so let the
			 * special handling commence!
			 */
			port = true;
		} else modelID = BigraphXMLLoader.getNewObject(e.getLocalName(), name);

		ModelObject model = null;
		if (modelID instanceof Layoutable.Identifier) {
			addChange(new Container.ChangeAddChildDescriptor(
					context.getIdentifier(getScratch()),
					(Layoutable.Identifier)modelID));
			
			/* Get the temporary object from the scratchpad */
			model = modelID.lookup(getScratch(), getResolver());
		}
		
		if (model instanceof Container) {
			processContainer(e, (Container)model);
		} else if (port) {
			processPoint(e, ((Node)context).getPort(name));
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

	static ModelObject.Identifier getNewObject(String typeName, String name) {
		typeName = typeName.toLowerCase(Locale.ENGLISH);
		if (typeName.equals("bigraph"))
			return new Bigraph.Identifier();
		else if (typeName.equals("root"))
			return new Root.Identifier(name);
		else if (typeName.equals("site"))
			return new Site.Identifier(name);
		else if (typeName.equals("innername"))
			return new InnerName.Identifier(name);
		else if (typeName.equals("outername"))
			return new OuterName.Identifier(name);
		else if (typeName.equals("signature"))
			return new Signature.Identifier();
		else if (typeName.equals("control"))
			return new Control.Identifier(name);
		else if (typeName.equals("edge"))
			return new Edge.Identifier(name);
		else return null;
	}
}
