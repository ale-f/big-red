package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;

import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.part.BigraphPart;

public class Bigraph extends Thing {
	protected Signature signature = new Signature();
	protected HashMap<String, Thing> idRegistry = new HashMap<String, Thing>();
	protected ArrayList<Edge> edges = new ArrayList<Edge>();
	
	public Thing clone() throws CloneNotSupportedException {
		return new Bigraph()._overwrite(this);
	}
	
	@Override
	public boolean canContain(ILayoutable child) {
		Class<? extends ILayoutable> c = child.getClass();
		return (c == Root.class || c == InnerName.class);
	}
	
	public Bigraph getBigraph() {
		return this;
	}
	
	public Signature getSignature() {
		return signature;
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
	
	private ArrayList<ILayoutable> nhtlo = new ArrayList<ILayoutable>();
	
	/**
	 * Adds a <i>non-hierarchical top-level object</i> to this Bigraph.
	 * @param o an {@link ILayoutable}
	 * @see Bigraph#getNHTLOs()
	 */
	public void addNHTLO(ILayoutable o) {
		if (!nhtlo.contains(o)) {
			nhtlo.add(o);
			listeners.firePropertyChange(PROPERTY_CHILD, null, o);
		}
	}
	
	/**
	 * Removes a <i>non-hierarchical top-level object</i> from this Bigraph.
	 * @param o an {@link ILayoutable}
	 * @see Bigraph#getNHTLOs()
	 */
	public void removeNHTLO(ILayoutable o) {
		if (nhtlo.contains(o)) {
			nhtlo.remove(o);
			listeners.firePropertyChange(PROPERTY_CHILD, o, null);
		}
	}
	
	/**
	 * Returns the array of <i>non-hierarchical top-level objects</i> for this
	 * Bigraph.
	 * 
	 * <p>A <i>non-hierarchical top-level object</i> is an object whose {@link
	 * EditPart} must always appear as a top-level child of {@link
	 * BigraphPart}; they include {@link Port}s and {@link Edge}s. An
	 * object is a good candidate for being a NHTLO if it doesn't really make
	 * sense to think of it as being a child of a particular {@link Node}, or
	 * if it needs to be able to escape the bounding box of its parent.
	 * @return an array of {@link ILayoutable} objects
	 */
	public ArrayList<ILayoutable> getNHTLOs() {
		return nhtlo;
	}
}
