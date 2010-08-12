package dk.itu.big_red.model.interfaces;

import org.eclipse.swt.graphics.RGB;

public interface IOutlineColourable extends IPropertyChangeNotifier {

	/**
	 * The property name fired when the outline colour changes.
	 */
	public static final String PROPERTY_OUTLINE_COLOUR = "IOutlineColourableColour";

	/**
	 * Returns the current outline colour used when rendering this object.
	 * @return the current outline colour
	 */
	public abstract RGB getOutlineColour();

	/**
	 * Changes the outline colour used to render this object.
	 * @param outlineColour the new outline colour
	 */
	public abstract void setOutlineColour(RGB outlineColour);

}