package dk.itu.big_red.model.interfaces;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.Bigraph;

/**
 * Objects implementing ILayoutable are model objects which represent an actual
 * graphical item on the bigraph - they have a <i>layout</i> (a {@link
 * Rectangle}) which defines their bounding box and which can change under some
 * circumstances.
 * @author alec
 *
 */
public interface ILayoutable {
	public static final String PROPERTY_LAYOUT = "ILayoutableLayout";
	
	/**
	 * Gets a copy of the current layout of this object.
	 * @return the current layout
	 */
	public Rectangle getLayout();
	
	/**
	 * Sets the layout of this object.
	 * 
	 * <p>Implementers are required not to store a reference to the {@link
	 * Rectangle} provided - its values should instead be copied into another
	 * structure.
	 * @param layout the new layout
	 */
	public void setLayout(Rectangle layout);
	
	/**
	 * Returns the {@link Bigraph} that contains this object.
	 * @return a Bigraph
	 */
	public Bigraph getBigraph();
	
	/**
	 * Sets the {@link Bigraph} that contains this object.
	 * @param bigraph a Bigraph
	 */
	public void setBigraph(Bigraph bigraph);
}
