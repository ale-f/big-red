package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.assistants.ResourceWrapper;
import dk.itu.big_red.model.interfaces.ILayoutable;

public class Bigraph extends Thing {
	protected ResourceWrapper<Signature> signature =
		new ResourceWrapper<Signature>();
	protected NamespaceManager namespaceManager = new NamespaceManager();
	
	/**
	 * The property name fired when the <i>lower outer name</i> boundary is
	 * changed.
	 */
	public static final String
		PROPERTY_BOUNDARY_LON = "BigraphBoundaryLowerOuterName";
	
	/**
	 * The property name fired when the <i>upper root</i> boundary is changed.
	 */
	public static final String
		PROPERTY_BOUNDARY_UR = "BigraphBoundaryUpperRoot";
	
	/**
	 * The property name fired when the <i>lower root</i> boundary is changed.
	 */
	public static final String
		PROPERTY_BOUNDARY_LR = "BigraphBoundaryLowerRoot";
	
	/**
	 * The property name fired when the <i>upper inner name</i> boundary is
	 * changed.
	 */
	public static final String
		PROPERTY_BOUNDARY_UIN = "BigraphBoundaryUpperInnerName";
	
	protected static int BOUNDARY_MARGIN = 20;
	protected int upperRootBoundary = Integer.MIN_VALUE + BOUNDARY_MARGIN,
	              lowerOuterNameBoundary = Integer.MAX_VALUE - BOUNDARY_MARGIN,
	              upperInnerNameBoundary = Integer.MIN_VALUE + BOUNDARY_MARGIN,
	              lowerRootBoundary = Integer.MAX_VALUE - BOUNDARY_MARGIN;
	
	public Thing clone() throws CloneNotSupportedException {
		return new Bigraph()._overwrite(this);
	}
	
	/**
	 * Gets the {@link NamespaceManager} for this bigraph.
	 * @return a NamespaceManager
	 */
	public NamespaceManager getNamespaceManager() {
		return namespaceManager;
	}
	
	@Override
	public boolean canContain(ILayoutable child) {
		Class<? extends ILayoutable> c = child.getClass();
		return (c == Root.class || c == InnerName.class || c == OuterName.class);
	}
	
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
	
	public void setParent(ILayoutable parent) {
		/* do nothing */
	}
	
	public ILayoutable getParent() {
		return null;
	}
	
	@Override
	public Rectangle getRootLayout() {
		return new Rectangle();
	}
	
	private ArrayList<ILayoutable> sortedChildren =
		new ArrayList<ILayoutable>();
	
	@Override
	public void addChild(ILayoutable child) {
		sortedChildren.clear();
		super.addChild(child);
		updateBoundaries();
	}
	
	@Override
	public void removeChild(ILayoutable child) {
		sortedChildren.clear();
		super.removeChild(child);
		updateBoundaries();
	}
	
	@Override
	public List<ILayoutable> getChildren() {
		if (sortedChildren.size() == 0) {
			for (ILayoutable i : children) {
				if (i.getClass() != Edge.class)
					sortedChildren.add(i);
			}
			for (ILayoutable i : children) {
				if (i.getClass() == Edge.class)
					sortedChildren.add(i);
			}
		}
		return sortedChildren;
	}
	
	/**
	 * Recalculates the boundaries that govern the placement of {@link
	 * OuterName}s, {@link Root}s, and {@link InnerName}s.
	 */
	public void updateBoundaries() {
		int oldLON = upperRootBoundary,
		    oldHR = lowerOuterNameBoundary,
		    oldLR = upperInnerNameBoundary,
		    oldHIN = lowerRootBoundary;
		upperRootBoundary = Integer.MIN_VALUE;
		lowerOuterNameBoundary = Integer.MAX_VALUE;
		upperInnerNameBoundary = Integer.MIN_VALUE;
		lowerRootBoundary = Integer.MAX_VALUE;
		
		for (ILayoutable i : children) {
			int top = i.getLayout().getTopLeft().y,
				bottom = i.getLayout().getBottomLeft().y;
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
		
		lowerOuterNameBoundary -= BOUNDARY_MARGIN;
		upperRootBoundary += BOUNDARY_MARGIN;
		lowerRootBoundary -= BOUNDARY_MARGIN;
		upperInnerNameBoundary += BOUNDARY_MARGIN;
		
		if (oldHR != lowerOuterNameBoundary)
			listeners.firePropertyChange(PROPERTY_BOUNDARY_LON, oldHR, lowerOuterNameBoundary);
		if (oldLON != upperRootBoundary)
			listeners.firePropertyChange(PROPERTY_BOUNDARY_UR, oldLON, upperRootBoundary);
		if (oldHIN != lowerRootBoundary)
			listeners.firePropertyChange(PROPERTY_BOUNDARY_LR, oldHIN, lowerRootBoundary);
		if (oldLR != upperInnerNameBoundary)
			listeners.firePropertyChange(PROPERTY_BOUNDARY_UIN, oldLR, upperInnerNameBoundary);
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
}
