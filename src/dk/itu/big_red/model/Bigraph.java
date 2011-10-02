package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.geometry.Dimension;

import dk.itu.big_red.model.assistants.BigraphIntegrityValidator;
import dk.itu.big_red.model.assistants.CloneMap;
import dk.itu.big_red.model.assistants.NamespaceManager;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.IChangeValidator;
import dk.itu.big_red.model.changes.IChangeable;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeAddChild;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeConnect;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeDisconnect;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeEdgeReposition;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeLayout;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeOutlineColour;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeRemoveChild;
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
import dk.itu.big_red.util.resources.ResourceWrapper;

/**
 * The Bigraph is the root of any agent, and contains {@link Root}s, {@link
 * InnerName}s, and {@link OuterName}s.
 * @author alec
 * @see IBigraph
 */
public class Bigraph extends Container implements IBigraph, IChangeable {
	protected ResourceWrapper<Signature> signature =
		new ResourceWrapper<Signature>();
	protected NamespaceManager namespaceManager = new NamespaceManager();
	
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
	 * Creates and returns a complete copy of this {@link Bigraph}, complete
	 * with {@link Point}s and {@link Link} connections.
	 * @param m a {@link CloneMap}; can be <code>null</code>
	 * @return an exact copy of this {@link Bigraph}
	 */
	@Override
	public Bigraph clone(CloneMap m) {
		if (m == null)
			m = new CloneMap();
		Bigraph b = (Bigraph)super.clone(m);
		
		for (Link i : Utility.only(getChildren(), Link.class)) {
			Link iD = m.getCloneOf(i);
			for (Point p : i.getPoints()) {
				System.out.println("");
				iD.addPoint(m.getCloneOf(p));
			}
		}
		
		return b;
	}
	
	/**
	 * Gets the {@link NamespaceManager} for this bigraph.
	 * @return a NamespaceManager
	 */
	public NamespaceManager getNamespaceManager() {
		return namespaceManager;
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
	
	public void setSignature(IFile file, Signature signature) {
		if (file != null && signature != null) {
			this.signature.setResource(file);
			this.signature.setModel(signature);
		}
	}
	
	public Signature getSignature() {
		return signature.getModel();
	}
	
	public IFile getSignatureFile() {
		return signature.getResource();
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
		return signature.getModel();
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
			cg.add(new BigraphChangeLayout(i, r));
		}
		
		for (Link i : Utility.only(getChildren(), Link.class)) {
			if (i instanceof Edge)
				cg.add(new BigraphChangeEdgeReposition((Edge)i));
			cg.add(new BigraphChangeOutlineColour(i, Colour.randomRGB()));
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
		} else if (b instanceof BigraphChangeConnect) {
			BigraphChangeConnect c = (BigraphChangeConnect)b;
			c.link.addPoint(c.point);
		} else if (b instanceof BigraphChangeDisconnect) {
			BigraphChangeDisconnect c = (BigraphChangeDisconnect)b;
			c.link.removePoint(c.point);
		} else if (b instanceof BigraphChangeAddChild) {
			BigraphChangeAddChild c = (BigraphChangeAddChild)b;
			c.parent.addChild(c.child);
			c.child.setLayout(c.newLayout);
		} else if (b instanceof BigraphChangeRemoveChild) {
			BigraphChangeRemoveChild c = (BigraphChangeRemoveChild)b;
			c.parent.removeChild(c.child);
		} else if (b instanceof BigraphChangeLayout) {
			BigraphChangeLayout c = (BigraphChangeLayout)b;
			c.model.setLayout(c.newLayout);
			if (c.model.getParent() instanceof Bigraph)
				((Bigraph)c.model.getParent()).updateBoundaries();
		} else if (b instanceof BigraphChangeEdgeReposition) {
			BigraphChangeEdgeReposition c = (BigraphChangeEdgeReposition)b;
			c.edge.averagePosition();
		} else if (b instanceof BigraphChangeOutlineColour) {
			BigraphChangeOutlineColour c = (BigraphChangeOutlineColour)b;
			c.model.setOutlineColour(c.newColour);
		}
		if (changes != null && !(b instanceof ChangeGroup))
			changes.add(b);
	}
	
	private ArrayList<Change> changes = null;
	
	/**
	 * Returns all the {@link Change}s applied to this bigraph since the last
	 * call to {@link #checkpoint()}.
	 * <p>{@link ChangeGroup}s will not be present in the return value, but
	 * the {@link Change}s they contained will be.
	 * @return an {@link ArrayList} of {@link Change}s, or <code>null</code>
	 * when called for the first time
	 */
	public ArrayList<Change> checkpoint() {
		ArrayList<Change> ch = changes;
		changes = new ArrayList<Change>();
		return ch;
	}
}
