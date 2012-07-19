package dk.itu.big_red.model.load_save.savers;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.Point;
import org.bigraph.model.Port;
import org.bigraph.model.Root;
import org.bigraph.model.Site;
import org.w3c.dom.Element;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.model.load_save.loaders.BigraphXMLLoader;

import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.BIGRAPH;
import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.BIG_RED;

/**
 * XMLSaver writes a {@link Bigraph} out as an XML document.
 * @author alec
 * @see BigraphXMLLoader
 */
public class BigraphXMLSaver extends XMLSaver {
	public BigraphXMLSaver() {
		setDefaultNamespace(BIGRAPH);
	}
	
	@Override
	public Bigraph getModel() {
		return (Bigraph)super.getModel();
	}
	
	@Override
	public BigraphXMLSaver setModel(ModelObject model) {
		if (model == null || model instanceof Bigraph)
			super.setModel(model);
		return this;
	}
	
	private boolean exportAppearance = true;
	
	public static final String
		OPTION_APPEARANCE = "BigraphXMLSaverAppearance";
	
	{
		addOption(OPTION_APPEARANCE, "Export appearance data",
			"Include Big Red-specific appearance information in the output.");
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
		setDocument(createDocument(BIGRAPH, "bigraph:bigraph"));
		processObject(getDocumentElement(), getModel());
		finish();
	}
	
	@Override
	public Element processObject(Element e, Object obj_) throws SaveFailedException {
		if (!(obj_ instanceof Bigraph))
			throw new SaveFailedException(obj_ + " isn't a Bigraph");
		Bigraph obj = (Bigraph)obj_;
		
		if (exportAppearance)
			applyAttributes(getDocumentElement(), "xmlns:big-red", BIG_RED);
		appendChildIfNotNull(e,
			processOrReference(
				newElement(BIGRAPH, "bigraph:signature"),
				obj.getSignature(), SignatureXMLSaver.class));
		
		ArrayList<Element>
			edges = new ArrayList<Element>(),
			outernames = new ArrayList<Element>(),
			roots = new ArrayList<Element>(),
			innernames = new ArrayList<Element>();
		
		for (Layoutable i : obj.getChildren()) {
			Element f = null;
			if (i instanceof Edge) {
				edges.add(f = newElement(BIGRAPH, "bigraph:edge"));
			} else if (i instanceof OuterName) {
				outernames.add(f = newElement(BIGRAPH, "bigraph:outername"));
			} else if (i instanceof Root) {
				roots.add(f = processContents(
						newElement(BIGRAPH, "bigraph:root"), (Root)i));
			} else if (i instanceof InnerName) {
				innernames.add(f = processPoint(
						newElement(BIGRAPH, "bigraph:innername"), (Point)i));
			}
			applyCommonProperties(f, i);
		}
		
		appendChildren(e, edges);
		appendChildren(e, outernames);
		appendChildren(e, roots);
		appendChildren(e, innernames);
		
		return executeDecorators(obj, e);
	}
	
	private static final void
	appendChildren(Element e, List<Element> children) {
		for (Element i : children)
			if (i != null)
				e.appendChild(i);
		children.clear();
	}
	
	private Element processContents(Element e, Container c)
			throws SaveFailedException {
		ArrayList<Element>
			nodes = new ArrayList<Element>(),
			sites = new ArrayList<Element>();
		
		for (Layoutable i : c.getChildren()) {
			Element f = null;
			if (i instanceof Node) {
				nodes.add(f =
					processNode(newElement(BIGRAPH, "bigraph:node"), (Node)i));
			} else if (i instanceof Site) {
				sites.add(f =
					processSite(newElement(BIGRAPH, "bigraph:site"), (Site)i));
			}
			applyCommonProperties(f, i);
		}
		appendChildren(e, nodes);
		appendChildren(e, sites);
		return e;
	}
	
	private Element processSite(Element e, Site s) throws SaveFailedException {
		String alias = ExtendedDataUtilities.getAlias(s);
		if (alias != null)
			applyAttributes(e, "alias", alias);
		return e;
	}
	
	private Element processNode(Element e, Node n) throws SaveFailedException {
		applyAttributes(e,
			"control", n.getControl().getName(),
			"name", n.getName());
		String parameter = ExtendedDataUtilities.getParameter(n);
		if (parameter != null)
			applyAttributes(e, "parameter", parameter);
		
		for (Port p : n.getPorts())
			appendChildIfNotNull(e, processPoint(
					newElement(BIGRAPH, "bigraph:port"), p));
		
		return processContents(e, n);
	}
	
	private Element processPoint(Element e, Point p) throws SaveFailedException {
		Link link = p.getLink();
		applyAttributes(e, "name", p.getName());
		if (link != null)
			applyAttributes(e, "link", link.getName());
		return e;
	}
		
	private Element applyCommonProperties(Element e, Layoutable l) {
		if (e == null || l == null)
			return e;
		if (!(l instanceof Bigraph))
			applyAttributes(e, "name", l.getName());
		return executeDecorators(l, e);
	}
}
