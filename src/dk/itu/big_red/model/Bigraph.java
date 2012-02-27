package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.geometry.Dimension;

import dk.itu.big_red.model.assistants.BigraphIntegrityValidator;
import dk.itu.big_red.model.assistants.Colour;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.IChangeValidator;
import dk.itu.big_red.model.changes.IChangeable;
import dk.itu.big_red.model.interfaces.IBigraph;
import dk.itu.big_red.model.interfaces.IEdge;
import dk.itu.big_red.model.interfaces.IInnerName;
import dk.itu.big_red.model.interfaces.IOuterName;
import dk.itu.big_red.model.interfaces.IRoot;
import dk.itu.big_red.model.interfaces.ISignature;
import dk.itu.big_red.model.load_save.savers.BigraphXMLSaver;
import dk.itu.big_red.model.namespaces.INamespace;
import dk.itu.big_red.model.namespaces.NamespaceGroup;
import dk.itu.big_red.model.namespaces.PositiveIntegerNamePolicy;
import dk.itu.big_red.model.namespaces.StringNamePolicy;
import dk.itu.big_red.utilities.Lists;
import dk.itu.big_red.utilities.geometry.Rectangle;
import dk.itu.big_red.utilities.resources.IFileBackable;

/**
 * The Bigraph is the root of any agent, and contains {@link Root}s, {@link
 * InnerName}s, and {@link OuterName}s.
 * @author alec
 * @see IBigraph
 */
public class Bigraph extends Container implements IBigraph, IChangeable, IFileBackable {
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
	
	private ArrayList<IChangeValidator> validators =
			new ArrayList<IChangeValidator>();
	
	public Bigraph() {
		validators.add(new BigraphIntegrityValidator(this));
	}
	
	/**
	 * Gets the list of {@link IChangeValidator}s currently validating changes
	 * to this {@link Bigraph}.
	 * @return a {@link List} of {@link IChangeValidator}s
	 */
	public List<IChangeValidator> getValidators() {
		return validators;
	}
	
	/**
	 * Adds a new {@link IChangeValidator} to this {@link Bigraph}.
	 * @param cv an {@link IChangeValidator}
	 */
	public void addValidator(IChangeValidator cv) {
		validators.add(cv);
	}
	
	/**
	 * Removes an {@link IChangeValidator} from this {@link Bigraph}.
	 * @param cv an {@link IChangeValidator}
	 */
	public void removeValidator(IChangeValidator cv) {
		validators.remove(cv);
	}
	
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
		
		for (Link i : Lists.only(getChildren(), Link.class)) {
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
	public Bigraph getBigraph() {
		return this;
	}
	
	public void setSignature(Signature signature) {
		this.signature = signature;
	}
	
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
			int top = i.getLayout().getTop(),
				bottom = i.getLayout().getBottom();
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
	public Iterable<IEdge> getIEdges() {
		return Lists.only(children, IEdge.class);
	}

	@Override
	public Iterable<IRoot> getIRoots() {
		return Lists.only(children, IRoot.class);
	}

	@Override
	public Iterable<IInnerName> getIInnerNames() {
		return Lists.only(children, IInnerName.class);
	}
	
	@Override
	public Iterable<IOuterName> getIOuterNames() {
		return Lists.only(children, IOuterName.class);
	}

	@Override
	public ISignature getISignature() {
		return signature;
	}
	
	/**
	 * Creates {@link ContainerChange}s which will (<i>almost</i> sensibly) resize and
	 * reposition all of this {@link Bigraph}'s children.
	 * @param cg a {@link ChangeGroup} to which changes should be appended
	 * @return a {@link ChangeGroup} containing relayout changes
	 */
	public ChangeGroup relayout() {
		ChangeGroup cg = new ChangeGroup();
		
		HashMap<Layoutable, Dimension> sizes =
				new HashMap<Layoutable, Dimension>();
		
		boolean outerNameEncountered = false;
		int outerNameProgress = PADDING,
			innerNameProgress = PADDING,
			top = PADDING;
		
		for (Layoutable i :
			Lists.group(getChildren(), BigraphXMLSaver.SCHEMA_ORDER)) {
			Dimension size = i.relayout(cg);
			sizes.put(i, size);
			Rectangle r = new Rectangle().setSize(size);
			if (i instanceof OuterName) {
				r.setLocation(outerNameProgress, PADDING);
				outerNameProgress += size.width + PADDING;
				if (!outerNameEncountered) {
					top += size.height + PADDING;
					outerNameEncountered = true;
				}
			} else if (i instanceof Root) {
				r.setLocation(PADDING, top);
				top += size.height + PADDING;
			} else if (i instanceof InnerName) {
				r.setLocation(innerNameProgress, top);
				innerNameProgress += size.width + PADDING;
			}
			cg.add(i.changeLayout(r));
		}
		
		for (Link i : Lists.only(getChildren(), Link.class)) {
			if (i instanceof Edge)
				cg.add(((Edge)i).changeReposition());
			cg.add(i.changeOutlineColour(new Colour().randomise()));
		}
		
		return cg;
	}
	
	@Override
	public void tryValidateChange(Change b) throws ChangeRejectedException {
		for (IChangeValidator i : validators)
			i.tryValidateChange(b);
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
			c.link.removePoint(c.getCreator());
		} else if (b instanceof Container.ChangeAddChild) {
			Container.ChangeAddChild c = (Container.ChangeAddChild)b;
			getNamespace(getNSI(c.child)).put(c.name, c.child);
			c.child.setName(c.name);
			c.getCreator().addChild(c.child);
		} else if (b instanceof Container.ChangeRemoveChild) {
			Container.ChangeRemoveChild c = (Container.ChangeRemoveChild)b;
			c.getCreator().removeChild(c.child);
			getNamespace(getNSI(c.child)).remove(c.child.getName());
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
			c.getCreator().setName(c.newName);
			getNamespace(getNSI(c.getCreator())).put(c.newName, c.getCreator());
		} else if (b instanceof ModelObject.ChangeComment) {
			ModelObject.ChangeComment c = (ModelObject.ChangeComment)b;
			c.getCreator().setComment(c.comment);
		} else if (b instanceof Site.ChangeAlias) {
			Site.ChangeAlias c = (Site.ChangeAlias)b;
			c.getCreator().setAlias(c.alias);
		}
	}

	private IFile file = null;
	
	@Override
	public IFile getFile() {
		return file;
	}

	@Override
	public Bigraph setFile(IFile file) {
		this.file = file;
		return this;
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
}
