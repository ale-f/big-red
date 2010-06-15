package dk.itu.big_red.model.interfaces;

import org.eclipse.draw2d.geometry.Rectangle;

/**
 * Objects implementing IHierarchical are model objects with a place in the
 * bigraph hierarchy, and they have a <i>parent</i> that contains them within
 * it.
 * @author alec
 *
 */
public interface IHierarchical extends ILayoutable {
	/**
	 * Returns the parent of this object.
	 * @return an {@link IHierarchical}
	 */
	public IHierarchical getParent();
	
	/**
	 * Changes the parent of this object.
	 * @param p the new parent {@link IHierarchical}
	 */
	public void setParent(IHierarchical p);
	
	/**
	 * Gets the layout of this object relative to the top-left of the
	 * <i>root</i> rather than the immediate parent. (Like {@link
	 * ILayoutable#getLayout}, the object returned is newly created, and so can
	 * be safely modified.)
	 * @return the current layout relative to the root
	 */
	public Rectangle getRootLayout();
}
