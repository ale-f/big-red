package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.geometry.Dimension;

import dk.itu.big_red.model.assistants.BigraphIntegrityValidator;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.IChangeValidator;
import dk.itu.big_red.model.changes.IChangeable;
import dk.itu.big_red.model.import_export.BigraphXMLExport;
import dk.itu.big_red.model.interfaces.IBigraph;
import dk.itu.big_red.model.interfaces.IEdge;
import dk.itu.big_red.model.interfaces.IInnerName;
import dk.itu.big_red.model.interfaces.IOuterName;
import dk.itu.big_red.model.interfaces.IRoot;
import dk.itu.big_red.model.interfaces.ISignature;
import dk.itu.big_red.util.Colour;
import dk.itu.big_red.util.Utility;
import dk.itu.big_red.util.geometry.Rectangle;
import dk.itu.big_red.util.resources.IFileBackable;

/**
 * The Bigraph is the root of any agent, and contains {@link Root}s, {@link
 * InnerName}s, and {@link OuterName}s.
 * @author alec
 * @see IBigraph
 */
public class Bigraph extends Container implements IBigraph, IChangeable, IFileBackable {
	protected Signature signature = null;

	private HashMap<Object, Map<String, Layoutable>> names =
		new HashMap<Object, Map<String, Layoutable>>();
	
	/**
	 * Creates a <i>namespace</i>, a mapping from {@link String}s to {@link
	 * Layoutable}s.
	 * @return a new namespace
	 */
	public static Map<String, Layoutable> newNamespace() {
		return new HashMap<String, Layoutable>();
	}
	
	/**
	 * Creates a <i>namespace</i>, a mapping from {@link String}s to {@link
	 * Layoutable}s.
	 * @param m an existing namespace to copy mappings from
	 * @return a new namespace
	 */
	public static Map<String, Layoutable> newNamespace(Map<? extends String, ? extends Layoutable> m) {
		return new HashMap<String, Layoutable>(m);
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
		return object;
	}
	
	/**
	 * Gets the namespace with the given namespace identifier, creating it if
	 * necessary.
	 * @param nsi a value returned from a call to {@link #getNSI(Layoutable)},
	 * unless you're <i>very</i> sure you know what you're doing
	 * @return the specified namespace
	 */
	public Map<String, Layoutable> getNamespace(Object nsi) {
		Map<String, Layoutable> r = names.get(nsi);
		if (r == null)
			names.put(nsi, r = newNamespace());
		return r;
	}
	
	/**
	 * The characters one might want to use to represent a base-36 number.
	 */
	private final static String _IAS_ALPHA =
		"0123456789abcdefghijklmnopqrstuvwxyz";

	/**
	 * The first six powers of thirty-six, because premature optimisation is
	 * great.
	 */
	private final static int powersOf36[] = {
		1, /* 36 ^^ 0 */
		36, /* 36 ^^ 1 */
		1296, /* 36 ^^ 2 */
		46656, /* 36 ^^ 3 */
		1679616, /* 36 ^^ 4 */
		60466176, /* 36 ^^ 5 */
	};
	
	/**
	 * Returns <code>x</code> as a base-36 string.
	 * <p>This is used by {@link #getFirstUnusedName(Layoutable)}.
	 * @param x a number (which would ideally be less than 62,193,781)
	 * @return <code>x</code> in base-36
	 */
	private static String intAsString(int x) {
		String s = "";
		boolean nonZeroEncountered = false;
		for (int i = 5; i >= 0; i--) {
			int y = powersOf36[i];
			int z = x / y;

			if (z == 0 && !nonZeroEncountered && i != 0)
				continue;

			nonZeroEncountered = true;
			s += _IAS_ALPHA.charAt(z);

			x -= y * z;
		}
		return s;
	}
	
	/**
	 * Gets the first unused name in a namespace.
	 * @param ns the namespace to search
	 * @return a {@link String} suitable for a {@link BigraphChangeName}, or
	 * &mdash; in the highly unlikely event that there are 62,193,781 objects
	 * named by this function in the given namespace &mdash; the empty string
	 */
	public static String getFirstUnusedName(Map<String, Layoutable> ns) {
		int i = 0;
		String name = null;
		do {
			name = intAsString(i++);
		} while (ns.get(name) != null);
		return name;
	}
	
	/**
	 * Gets the first unused name suitable for the given {@link Layoutable}.
	 * @param l a {@link Layoutable}
	 * @return a {@link String} suitable for a {@link BigraphChangeName}, or
	 * &mdash; in the highly unlikely event that there are 62,193,781 objects
	 * of the same type as <code>l</code> in this {@link Bigraph} &mdash; the
	 * empty string
	 */
	public String getFirstUnusedName(Layoutable l) {
		return Bigraph.getFirstUnusedName(getNamespace(getNSI(l)));
	}
	
	/**
	 * The property name fired when a boundary has changed.
	 * (<code>oldValue</code> and <code>newValue</code> are both meaningless.)
	 */
	public static final String
		PROPERTY_BOUNDARY = "BigraphBoundary";
	
	protected int upperRootBoundary = Integer.MIN_VALUE,
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
		Bigraph b = (Bigraph)super.clone(m);
		
		for (Link i : Utility.only(getChildren(), Link.class)) {
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
				b.getNamespace(lClone).put(lClone.getName(), lClone);
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
		if (signature != null)
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
		return new Rectangle(0, 0, 1000000000, 1000000000);
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
		return getLayout();
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
		return Utility.only(children, IEdge.class);
	}

	@Override
	public Iterable<IRoot> getIRoots() {
		return Utility.only(children, IRoot.class);
	}

	@Override
	public Iterable<IInnerName> getIInnerNames() {
		return Utility.only(children, IInnerName.class);
	}
	
	@Override
	public Iterable<IOuterName> getIOuterNames() {
		return Utility.only(children, IOuterName.class);
	}

	@Override
	public ISignature getISignature() {
		return signature;
	}
	
	/**
	 * Creates {@link Change}s which will (<i>almost</i> sensibly) resize and
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
			Utility.groupListByClass(getChildren(), BigraphXMLExport.SCHEMA_ORDER)) {
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
		
		for (Link i : Utility.only(getChildren(), Link.class)) {
			if (i instanceof Edge)
				cg.add(((Edge)i).changeReposition());
			cg.add(i.changeOutlineColour(new Colour().randomise()));
		}
		
		return cg;
	}
	
	private ChangeRejectedException lastRejection = null;
	
	@Override
	public void applyChange(Change b) {
		try {
			tryApplyChange(b);
		} catch (ChangeRejectedException e) {
			return;
		}
	}
	
	@Override
	public boolean validateChange(Change b) {
		try {
			tryValidateChange(b);
		} catch (ChangeRejectedException e) {
			lastRejection = e;
			return false;
		}
		return true;
	}
	
	@Override
	public ChangeRejectedException getLastRejection() {
		return lastRejection;
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
			c.link.addPoint(c.point);
		} else if (b instanceof Point.ChangeDisconnect) {
			Point.ChangeDisconnect c = (Point.ChangeDisconnect)b;
			c.link.removePoint(c.point);
		} else if (b instanceof Container.ChangeAddChild) {
			Container.ChangeAddChild c = (Container.ChangeAddChild)b;
			c.parent.addChild(c.child);
		} else if (b instanceof Container.ChangeRemoveChild) {
			Container.ChangeRemoveChild c = (Container.ChangeRemoveChild)b;
			c.parent.removeChild(c.child);
		} else if (b instanceof Layoutable.ChangeLayout) {
			Layoutable.ChangeLayout c = (Layoutable.ChangeLayout)b;
			c.model.setLayout(c.newLayout);
			if (c.model.getParent() instanceof Bigraph)
				((Bigraph)c.model.getParent()).updateBoundaries();
		} else if (b instanceof Edge.ChangeReposition) {
			Edge.ChangeReposition c = (Edge.ChangeReposition)b;
			c.edge.averagePosition();
		} else if (b instanceof Colourable.ChangeOutlineColour) {
			Colourable.ChangeOutlineColour c = (Colourable.ChangeOutlineColour)b;
			c.model.setOutlineColour(c.newColour);
		} else if (b instanceof Colourable.ChangeFillColour) {
			Colourable.ChangeFillColour c = (Colourable.ChangeFillColour)b;
			c.model.setFillColour(c.newColour);
		} else if (b instanceof Layoutable.ChangeName) {
			Layoutable.ChangeName c = (Layoutable.ChangeName)b;
			if (c.model.getName() != null)
				getNamespace(getNSI(c.model)).remove(c.model.getName());
			c.model.setName(c.newName);
			if (c.newName != null)
				getNamespace(getNSI(c.model)).put(c.newName, c.model);
		}
		if (checkpointChanges != null && !(b instanceof ChangeGroup))
			checkpointChanges.add(b);
	}
	
	private Bigraph checkpointBigraph = null;
	private Map<ModelObject, ModelObject> checkpointBigraphCloneMap = null;
	private ArrayList<Change> checkpointChanges = null;
	
	/**
	 * Checkpoints the {@link Bigraph}.
	 * @see #getCheckpointBigraph()
	 * @see #getCheckpointBigraphCloneMap()
	 * @see #getCheckpointChanges()
	 */
	public void checkpoint() {
		checkpointBigraphCloneMap = new HashMap<ModelObject, ModelObject>();
		checkpointBigraph = this.clone(checkpointBigraphCloneMap);
		checkpointChanges = new ArrayList<Change>();
	}
	
	/**
	 * Returns the state of the {@link Bigraph} at the time of the last call to
	 * {@link #checkpoint()}.
	 * @return a clone of an earlier state of this {@link Bigraph}, or
	 *         <code>null</code> if {@link #checkpoint()} has not yet been
	 *         called
	 * @see #getCheckpointBigraphCloneMap()
	 */
	public Bigraph getCheckpointBigraph() {
		return checkpointBigraph;
	}
	
	/**
	 * Returns the {@link CloneMap} mapping objects in this {@link Bigraph} to
	 * their clones in the result of {@link #getCheckpointBigraph()}.
	 * @return a {@link CloneMap}, or <code>null</code> if
	 *         {@link #checkpoint()} has not yet been called
	 * @see #getCheckpointBigraph()
	 */
	public Map<ModelObject, ModelObject> getCheckpointBigraphCloneMap() {
		return checkpointBigraphCloneMap;
	}
	
	/**
	 * Returns the list of {@link Change}s made to this {@link Bigraph} since
	 * the last call to {@link #checkpoint()}.
	 * @return an {@link ArrayList} of {@link Change}s, or <code>null</code> if
	 *         {@link #checkpoint()} has not yet been called
	 */
	public ArrayList<Change> getCheckpointChanges() {
		return checkpointChanges;
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
}
