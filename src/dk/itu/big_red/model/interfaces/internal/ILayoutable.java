package dk.itu.big_red.model.interfaces.internal;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;

/**
 * Objects implementing ILayoutable are model objects which have a graphical
 * representation:
 * 
 * <ul>
 * <li>they have a <i>layout</i> (a {@link Rectangle}) which defines their
 * bounding box and which can change under some circumstances; and
 * <li>they have a <i>parent</i> (another {@link ILayoutable}), which contains
 * them.
 * </ul>
 * 
 * <p>As they also implement {@link IPropertyChangeNotifier}, they can also
 * notify interested parties when these properties change.
 * @author alec
 *
 */
public interface ILayoutable extends IPropertyChangeNotifier {
	/**
	 * The property name fired when the ILayoutable's layout changes (i.e.,
	 * it's resized or moved).
	 */
	public static final String PROPERTY_LAYOUT = "ILayoutableLayout";
	
	/**
	 * Gets the current layout of this object.
	 * @return the current layout
	 */
	public Rectangle getLayout();
	
	/**
	 * Gets a copy of the layout of this object relative to the top-left of the
	 * root {@link Bigraph} rather than the immediate parent.
	 * @return the current layout relative to the root
	 */
	public Rectangle getRootLayout();
	
	/**
	 * Sets the layout of this object.
	 * @param layout the new layout (which will belong to this object)
	 */
	public void setLayout(Rectangle layout);
	
	/**
	 * Returns the {@link Bigraph} that ultimately contains this object.
	 * @return a Bigraph
	 */
	public Bigraph getBigraph();
	
	/**
	 * Returns the parent of this object.
	 * @return an {@link Container}
	 */
	public Container getParent();
}
