package dk.itu.big_red.model.interfaces;

import org.eclipse.swt.graphics.RGB;

/**
 * Objects implementing IColourable have a <i>fill colour</i> and <i>outline
 * colour</i> that can be manipulated by the user.
 * @author alec
 *
 */
public interface IColourable extends IPropertyChangeNotifier {
	/**
	 * The property name fired when the fill colour changes.
	 */
	public static final String PROPERTY_FILL_COLOUR = "IColourableFillColour";
	
	/**
	 * The property name fired when the outline colour changes.
	 */
	public static final String PROPERTY_OUTLINE_COLOUR = "IColourableOutlineColour";
	
	/**
	 * Returns the current fill colour used when rendering this object.
	 * @return the current fill colour
	 */
	public RGB getFillColour();
	
	/**
	 * Changes the fill colour used to render this object.
	 * @param fillColour the new fill colour
	 */
	public void setFillColour(RGB fillColour);

	/**
	 * Returns the current outline colour used when rendering this object.
	 * @return the current outline colour
	 */
	public RGB getOutlineColour();
	
	/**
	 * Changes the outline colour used to render this object.
	 * @param outlineColour the new outline colour
	 */
	public void setOutlineColour(RGB outlineColour);
}
