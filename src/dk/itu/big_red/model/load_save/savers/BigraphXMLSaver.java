package dk.itu.big_red.model.load_save.savers;

import org.w3c.dom.Element;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.assistants.AppearanceGenerator;
import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.model.load_save.XMLSaver;
import dk.itu.big_red.model.load_save.XMLNS;
import dk.itu.big_red.model.load_save.loaders.BigraphXMLLoader;
import dk.itu.big_red.utilities.DOM;
import dk.itu.big_red.utilities.Lists;

/**
 * XMLSaver writes a {@link Bigraph} out as an XML document.
 * @author alec
 * @see BigraphXMLLoader
 *
 */
public class BigraphXMLSaver extends XMLSaver {
	@Override
	public Bigraph getModel() {
		return (Bigraph)super.getModel();
	}
	
	@Override
	public BigraphXMLSaver setModel(ModelObject model) {
		if (model instanceof Bigraph)
			super.setModel(model);
		return this;
	}
	
	/**
	 * An array of model {@link Class}es in the appropriate order for the
	 * <code>&lt;bigraph&gt;</code> XML schema, suitable for giving as the
	 * last argument to {@link Lists#group(java.util.List,
	 * Object...)}.
	 */
	public static final Object SCHEMA_ORDER[] = {
		Edge.class, OuterName.class, Root.class, InnerName.class,
		Port.class, Node.class, Site.class
	};
	
	private boolean exportAppearance = true,
			exportPersistentID = true;
	
	public static final String
		OPTION_APPEARANCE = "BigraphXMLExportAppearance";
	
	{
		addOption(OPTION_APPEARANCE, "Saver Big Red-specific appearance data");
	}
	
	@Override
	public Object getOption(String id) {
		if (id.equals(OPTION_APPEARANCE)) {
			return exportAppearance;
		} else return super.getOption(id);
	}
	
	@Override
	public void setOption(String id, Object value) {
		if (id.equals(OPTION_APPEARANCE)) {
			exportAppearance = (Boolean)value;
		} else super.setOption(id, value);
	}
	
	@Override
	public void exportObject() throws SaveFailedException {
		setDocument(XMLSaver.createDocument(XMLNS.BIGRAPH, "bigraph:bigraph"));
		processObject(getDocumentElement(), getModel());
		finish();
	}
	
	@Override
	public Element processObject(Element e, Object obj_) throws SaveFailedException {
		if (!(obj_ instanceof Bigraph))
			throw new SaveFailedException(obj_ + " isn't a Bigraph");
		Bigraph obj = (Bigraph)obj_;
		
		if (exportAppearance || exportPersistentID)
			DOM.applyAttributes(getDocumentElement(), "xmlns:big-red", XMLNS.BIG_RED);
		appendChildIfNotNull(e,
			processOrReference(
				newElement(XMLNS.BIGRAPH, "bigraph:signature"),
				getModel().getFile(),
				obj.getSignature(), SignatureXMLSaver.class));
		
		for (Layoutable i :
			Lists.group(obj.getChildren(),
					BigraphXMLSaver.SCHEMA_ORDER)) {
			Element f = null;
			if (i instanceof Edge) {
				f = newElement(XMLNS.BIGRAPH, "bigraph:edge");
			} else if (i instanceof OuterName) {
				f = newElement(XMLNS.BIGRAPH, "bigraph:outername");
			} else if (i instanceof Root) {
				f = processRoot(
						newElement(XMLNS.BIGRAPH, "bigraph:root"), (Root)i);
			} else if (i instanceof InnerName) {
				f = processPoint(
						newElement(XMLNS.BIGRAPH, "bigraph:innername"), (Point)i);
			}
			appendChildIfNotNull(e, applyCommonProperties(f, i));
		}
		
		return e;
	}
	
	private Element processRoot(Element e, Root r) throws SaveFailedException {
		for (Layoutable i :
			Lists.group(r.getChildren(),
					BigraphXMLSaver.SCHEMA_ORDER)) {
			Element f = null;
			if (i instanceof Node) {
				f = processNode(
						newElement(XMLNS.BIGRAPH, "bigraph:node"), (Node)i);
			} else if (i instanceof Site) {
				f = processSite(
						newElement(XMLNS.BIGRAPH, "bigraph:site"), (Site)i);
			}
			appendChildIfNotNull(e, applyCommonProperties(f, i));
		}
		return e;
	}
	
	private Element processSite(Element e, Site s) throws SaveFailedException {
		String alias = s.getAlias();
		if (alias != null)
			DOM.applyAttributes(e, "alias", alias);
		return e;
	}
	
	private Element processNode(Element e, Node n) throws SaveFailedException {
		DOM.applyAttributes(e,
			"control", n.getControl().getName(),
			"name", n.getName());
		
		for (Port p : n.getPorts()) 
			appendChildIfNotNull(e, processPoint(
					newElement(XMLNS.BIGRAPH, "bigraph:port"), p));
		
		for (Layoutable l :
			Lists.group(n.getChildren(),
					BigraphXMLSaver.SCHEMA_ORDER)) {
			Element f = null;
			if (l instanceof Node) {
				f = processNode(
					newElement(XMLNS.BIGRAPH, "bigraph:node"), (Node)l);
			} else if (l instanceof Site) {
				f = processSite(
					newElement(XMLNS.BIGRAPH, "bigraph:site"), (Site)l);
			}
			appendChildIfNotNull(e, applyCommonProperties(f, l));
		}
				
		return e;
	}
	
	private Element processPoint(Element e, Point p) throws SaveFailedException {
		Link link = p.getLink();
		DOM.applyAttributes(e,
			"name", p.getName());
		if (link != null) {
			DOM.applyAttributes(
				e,
				"link", link.getName());
		}
		return e;
	}
		
	private Element applyCommonProperties(Element e, Layoutable l) {
		if (e == null || l == null)
			return e;
		if (!(l instanceof Bigraph))
			DOM.applyAttributes(e, "name", l.getName());
		if (exportAppearance)
			appendChildIfNotNull(e, AppearanceGenerator.getAppearance(getDocument(), l));
		return e;
	}
}
