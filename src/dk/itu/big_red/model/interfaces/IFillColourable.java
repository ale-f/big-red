package dk.itu.big_red.model.interfaces;

import org.eclipse.swt.graphics.RGB;

public interface IFillColourable extends IPropertyChangeNotifier {

	/**
	 * The property name fired when the fill colour changes.
	 */
	public static final String PROPERTY_FILL_COLOUR = "IFillColourableColour";

	/**
	 * Returns the current fill colour used when rendering this object.
	 * @return the current fill colour
	 */
	public abstract RGB getFillColour();

	/**
	 * Changes the fill colour used to render this object.
	 * @param fillColour the new fill colour
	 */
	public abstract void setFillColour(RGB fillColour);

}