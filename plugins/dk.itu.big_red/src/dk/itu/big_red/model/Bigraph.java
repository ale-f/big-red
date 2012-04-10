package dk.itu.big_red.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.assistants.BigraphIntegrityValidator;
import dk.itu.big_red.model.assistants.Colour;
import dk.itu.big_red.model.assistants.IPropertyProviderProxy;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.IChangeValidator;
import dk.itu.big_red.model.changes.IChangeExecutor;
import dk.itu.big_red.model.interfaces.IBigraph;
import dk.itu.big_red.model.names.INamespace;
import dk.itu.big_red.model.names.NamespaceGroup;
import dk.itu.big_red.model.names.PositiveIntegerNamePolicy;
import dk.itu.big_red.model.names.StringNamePolicy;

/**
 * The Bigraph is the root of any agent, and contains {@link Root}s, {@link
 * InnerName}s, and {@link OuterName}s.
 * @author alec
 * @see IBigraph
 */
public class Bigraph extends Container implements IBigraph, IChangeExecutor {
	private Signature signature = null;

	private NamespaceGroup<Layoutable> nsg = new NamespaceGroup<Layoutable>();
	
	{
		nsg.createNamespace(Link.class).setPolicy(new StringNamePolicy());
		nsg.createNamespace(Node.class).setPolicy(new StringNamePolicy());
		nsg.createNamespace(InnerName.class).setPolicy(new StringNamePolicy());
		
		nsg.createNamespace(Root.class).setPolicy(new PositiveIntegerNamePolicy());
		nsg.createNamespace(Site.class).setPolicy(new PositiveIntegerNamePolicy());
	}
	
	/**
	 * Returns the <i>namespace identifier</i> for the given {@link
	 * Layoutable}, which identifies the scope in which its name must be
	 * unique.
	 * <p>{@link InnerName}s, {@link Root}s, {@link Site}s and {@link Node}s
	 * all have separate namespaces, all {@link Link}s occupy the same
	 * namespace, and all other objects have no restrictions on their names.
	 * @param object a {@link Layoutable}
	 * @return the scope within which <code>object</code>'s name must be
	 * unique, or <code>object</code> if there are no restrictions on its name
	 */
	public static Object getNSI(Layoutable object) {
		if (object instanceof Link) {
			return Link.class;
		} else if (object instanceof InnerName ||
				object instanceof Root ||
				object instanceof Site ||
				object instanceof Node) {
			return object.getClass();
		}
		return null;
	}
	
	/**
	 * @see #getNSI(Layoutable)
	 */
	public static Object getNSI(String objectType) {
		if (objectType.equals("edge") || objectType.equals("outername") || objectType.equals("link")) {
			return Link.class;
		} else if (objectType.equals("innername")) {
			return InnerName.class;
		} else if (objectType.equals("root")) {
			return Root.class;
		} else if (objectType.equals("site")) {
			return Site.class;
		} else if (objectType.equals("node")) {
			return Node.class;
		} else return null;
	}
	
	/**
	 * Gets the namespace with the given namespace identifier.
	 * @param nsi a value returned from a call to {@link #getNSI(Layoutable)},
	 * unless you're <i>very</i> sure you know what you're doing
	 * @return the specified namespace
	 */
	public INamespace<Layoutable> getNamespace(Object nsi) {
		return nsg.getNamespace(nsi);
	}
	
	/**
	 * Gets the first unused name suitable for the given {@link Layoutable}.
	 * @param l a {@link Layoutable}
	 * @return a {@link String} suitable for a {@link BigraphChangeName}
	 */
	public String getFirstUnusedName(Layoutable l) {
		return getNamespace(getNSI(l)).getNextName();
	}
	
	/**
	 * The property name fired when a boundary has changed.
	 * (<code>oldValue</code> and <code>newValue</code> are both meaningless.)
	 */
	public static final String
		PROPERTY_BOUNDARY = "BigraphBoundary";
	
	private int upperRootBoundary = Integer.MIN_VALUE,
	            lowerOuterNameBoundary = Integer.MAX_VALUE,
	            upperInnerNameBoundary = Integer.MIN_VALUE,
	            lowerRootBoundary = Integer.MAX_VALUE;
	
	private IChangeValidator validator = new BigraphIntegrityValidator(this);

	public static final String CONTENT_TYPE = "dk.itu.big_red.bigraph";
	
	/**
	 * Creates and returns a complete copy of this {@link Bigraph}, complete
	 * with {@link Point}s and {@link Link} connections.
	 * @param m a {@link CloneMap}; can be <code>null</code>
	 * @return an exact copy of this {@link Bigraph}
	 */
	@Override
	public Bigraph clone(Map<ModelObject, ModelObject> m) {
		if (m == null)
			m = new HashMap<ModelObject, ModelObject>();
		Bigraph b = (Bigraph)newInstance();
		
		b.setFile(getFile());
		b.setSignature(getSignature().clone(m));
		
		/* ModelObject.clone */
		m.put(this, b);
		b.setComment(getComment());
		
		/* Colourable.clone */
		b.setFillColour(getFillColour().getCopy());
		b.setOutlineColour(getOutlineColour().getCopy());
		
		/* Layoutable.clone */
		b.setLayout(null);
		
		/* Container.clone */
		for (Layoutable child : getChildren())
			b.addChild(child.clone(m));
		
		for (Link i : only(null, Link.class)) {
			Link iClone = (Link)m.get(i);
			for (Point p : i.getPoints())
				iClone.addPoint((Point)m.get(p));
		}
		
		for (Entry<ModelObject, ModelObject> e : m.entrySet()) {
			ModelObject o = e.getKey();
			if (o instanceof Layoutable) {
				Layoutable l = (Layoutable)o,
						lClone = (Layoutable)e.getValue();
				lClone.setName(l.getName());
				INamespace<Layoutable> ns = b.getNamespace(getNSI(lClone));
				if (ns != null)
					ns.put(lClone.getName(), lClone);
			}
		}
		
		return b;
	}
	
	@Override
	public boolean canContain(Layoutable child) {
		Class<? extends Layoutable> c = child.getClass();
		return (c == Root.class || c == InnerName.class || c == OuterName.class || c == Edge.class);
	}
	
	@Override
	public Bigraph getBigraph(IPropertyProviderProxy context) {
		return this;
	}
	
	public void setSignature(Signature signature) {
		this.signature = signature;
	}
	
	@Override
	public Signature getSignature() {
		return signature;
	}
	
	@Override
	protected void setParent(Container parent) {
		/* do nothing */
	}
	
	@Override
	public Rectangle getLayout() {
		return null;
	}
	
	@Override
	protected void setLayout(Rectangle newLayout) {
		/* do nothing */
	}
	
	@Override
	public Container getParent() {
		return null;
	}
	
	@Override
	public Rectangle getRootLayout() {
		return new Rectangle();
	}
	
	@Override
	public Rectangle getRootLayout(IPropertyProviderProxy context) {
		return getRootLayout();
	}
	
	@Override
	protected void addChild(Layoutable child) {
		super.addChild(child);
		updateBoundaries();
	}
	
	@Override
	protected void removeChild(Layoutable child) {
		super.removeChild(child);
		updateBoundaries();
	}
	
	/**
	 * Recalculates the boundaries that govern the placement of {@link
	 * OuterName}s, {@link Root}s, and {@link InnerName}s.
	 */
	public void updateBoundaries() {
		int oldUR = upperRootBoundary,
		    oldLON = lowerOuterNameBoundary,
		    oldUIN = upperInnerNameBoundary,
		    oldLR = lowerRootBoundary;
		upperRootBoundary = Integer.MIN_VALUE;
		lowerOuterNameBoundary = Integer.MAX_VALUE;
		upperInnerNameBoundary = Integer.MIN_VALUE;
		lowerRootBoundary = Integer.MAX_VALUE;
		
		for (Layoutable i : children) {
			Rectangle r = i.getLayout();
			int top = i.getLayout().y(),
				bottom = r.y() + r.height();
			if (i instanceof OuterName) {
				if (bottom > upperRootBoundary)
					upperRootBoundary = bottom;
			} else if (i instanceof Root) {
				if (top < lowerOuterNameBoundary)
					lowerOuterNameBoundary = top;
				if (bottom > upperInnerNameBoundary)
					upperInnerNameBoundary = bottom;
			} else if (i instanceof InnerName) {
				if (top < lowerRootBoundary)
					lowerRootBoundary = top;
			}
		}
		
		if (oldUR != upperRootBoundary ||
			oldLON != lowerOuterNameBoundary ||
			oldUIN != upperInnerNameBoundary ||
			oldLR != lowerRootBoundary)
			firePropertyChange(PROPERTY_BOUNDARY, null, null);
	}
	
	public int getLowerOuterNameBoundary() {
		return lowerOuterNameBoundary;
	}
	
	public int getUpperRootBoundary() {
		return upperRootBoundary;
	}
	
	public int getLowerRootBoundary() {
		return lowerRootBoundary;
	}
	
	public int getUpperInnerNameBoundary() {
		return upperInnerNameBoundary;
	}

	@Override
	public List<Edge> getEdges() {
		return only(null, Edge.class);
	}

	@Override
	public List<Root> getRoots() {
		return only(null, Root.class);
	}

	@Override
	public List<InnerName> getInnerNames() {
		return only(null, InnerName.class);
	}
	
	@Override
	public List<Site> getSites() {
		ArrayList<Site> sites = new ArrayList<Site>();
		ArrayDeque<Container> queue = new ArrayDeque<Container>();
		queue.add(this);
		
		while (!queue.isEmpty()) {
			for (Layoutable l : queue.remove().getChildren()) {
				if (l instanceof Site) {
					sites.add((Site)l);
				} else if (l instanceof Container) {
					queue.add((Container)l);
				}
			}
		}
		
		return sites;
	}
	
	@Override
	public List<OuterName> getOuterNames() {
		return only(null, OuterName.class);
	}
	
	/**
	 * Creates {@link ContainerChange}s which will (<i>almost</i> sensibly) resize and
	 * reposition all of this {@link Bigraph}'s children.
	 * @param cg a {@link ChangeGroup} to which changes should be appended
	 * @return a {@link ChangeGroup} containing relayout changes
	 */
	public ChangeGroup relayout() {
		return relayout(null);
	}
	
	public ChangeGroup relayout(IPropertyProviderProxy context) {
		ChangeGroup cg = new ChangeGroup();
		int progress = PADDING, top = PADDING;
		Rectangle r = new Rectangle();
		Dimension size;
		
		for (Layoutable i : getOuterNames()) {
			r.setSize(size = i.relayout(context, cg));
		
			r.setLocation(progress, PADDING);
			progress += size.width + PADDING;
			if (top == PADDING)
				top += size.height + PADDING;
			
			cg.add(i.changeLayout(r.getCopy()));
		}
		
		for (Layoutable i : getRoots()) {
			r.setSize(size = i.relayout(context, cg));

			r.setLocation(PADDING, top);
			top += size.height + PADDING;
			
			cg.add(i.changeLayout(r.getCopy()));
		}
		
		progress = PADDING;
		
		for (Layoutable i : getInnerNames()) {
			r.setSize(size = i.relayout(context, cg));

			r.setLocation(progress, top);
			progress += size.width + PADDING;
			
			cg.add(i.changeLayout(r.getCopy()));
		}
		
		for (Link i : only(context, Link.class)) {
			if (i instanceof Edge)
				cg.add(((Edge)i).changeReposition());
			cg.add(i.changeOutlineColour(new Colour().randomise()));
		}
		
		return cg;
	}
	
	@Override
	public void tryValidateChange(Change b) throws ChangeRejectedException {
		validator.tryValidateChange(b);
	}
	
	@Override
	public void tryApplyChange(Change b) throws ChangeRejectedException {
		tryValidateChange(b);
		doChange(b);
	}
	
	private void doChange(Change b) {
		b.beforeApply();
		if (b instanceof ChangeGroup) {
			for (Change c : (ChangeGroup)b)
				doChange(c);
		} else if (b instanceof Point.ChangeConnect) {
			Point.ChangeConnect c = (Point.ChangeConnect)b;
			c.link.addPoint(c.getCreator());
		} else if (b instanceof Point.ChangeDisconnect) {
			Point.ChangeDisconnect c = (Point.ChangeDisconnect)b;
			c.getCreator().getLink().removePoint(c.getCreator());
		} else if (b instanceof Container.ChangeAddChild) {
			Container.ChangeAddChild c = (Container.ChangeAddChild)b;
			c.child.setName(
					getNamespace(getNSI(c.child)).put(c.name, c.child));
			c.getCreator().addChild(c.child);
		} else if (b instanceof Layoutable.ChangeRemove) {
			Layoutable.ChangeRemove c = (Layoutable.ChangeRemove)b;
			Layoutable ch = c.getCreator();
			ch.getParent().removeChild(ch);
			getNamespace(getNSI(ch)).remove(ch.getName());
		} else if (b instanceof Layoutable.ChangeLayout) {
			Layoutable.ChangeLayout c = (Layoutable.ChangeLayout)b;
			c.getCreator().setLayout(c.newLayout);
			if (c.getCreator().getParent() instanceof Bigraph)
				((Bigraph)c.getCreator().getParent()).updateBoundaries();
		} else if (b instanceof Edge.ChangeReposition) {
			Edge.ChangeReposition c = (Edge.ChangeReposition)b;
			c.getCreator().averagePosition();
		} else if (b instanceof Colourable.ChangeOutlineColour) {
			Colourable.ChangeOutlineColour c = (Colourable.ChangeOutlineColour)b;
			c.getCreator().setOutlineColour(c.newColour);
		} else if (b instanceof Colourable.ChangeFillColour) {
			Colourable.ChangeFillColour c = (Colourable.ChangeFillColour)b;
			c.getCreator().setFillColour(c.newColour);
		} else if (b instanceof Layoutable.ChangeName) {
			Layoutable.ChangeName c = (Layoutable.ChangeName)b;
			getNamespace(getNSI(c.getCreator())).remove(c.getCreator().getName());
			c.getCreator().setName(
					getNamespace(getNSI(c.getCreator())).put(
							c.newName, c.getCreator()));
		} else if (b instanceof ModelObject.ChangeComment) {
			ModelObject.ChangeComment c = (ModelObject.ChangeComment)b;
			c.getCreator().setComment(c.comment);
		} else if (b instanceof Site.ChangeAlias) {
			Site.ChangeAlias c = (Site.ChangeAlias)b;
			c.getCreator().setAlias(c.alias);
		} else if (b instanceof Node.ChangeParameter) {
			Node.ChangeParameter c = (Node.ChangeParameter)b;
			c.getCreator().setParameter(
				c.getCreator().getControl().getParameterPolicy().
					normalise(c.parameter));
		}
	}
	
	/**
	 * XXX: This is <i>probably</i> not a safe operation to perform on open
	 * Bigraphs...
	 */
	private void recursiveNodeUpdate(Signature newSignature, Container c) {
		for (Layoutable l : c.getChildren()) {
			if (l instanceof Node) {
				Node n = (Node)l;
				Control oldControl = n.getControl(),
					newControl = newSignature.getControl(oldControl.getName());
				n.setControl(newControl);
			}
			if (l instanceof Container)
				recursiveNodeUpdate(newSignature, (Container)l);
		}
	}
	
	@Override
	public void dispose() {
		signature.dispose();
		signature = null;
		
		super.dispose();
	}
	
	@Override
	public Bigraph setFile(IFile file) {
		return (Bigraph)super.setFile(file);
	}
}
