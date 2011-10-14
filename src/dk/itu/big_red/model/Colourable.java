package dk.itu.big_red.model;

import dk.itu.big_red.util.Colour;
import dk.itu.big_red.util.ReadonlyColour;

/**
 * {@link ModelObject}s which can have fill and outline colours are subclasses
 * of <strong>Colourable</strong>.
 * @author alec
 *
 */
public abstract class Colourable extends ModelObject {
	private Colour
		outlineColour = new Colour("black"),
		fillColour = new Colour("white");

	/**
	 * The property name fired when the outline colour changes.
	 */
	public static final String PROPERTY_OUTLINE_COLOUR = "ColourableOutline";
	
	/**
	 * The property name fired when the fill colour changes.
	 */
	public static final String PROPERTY_FILL_COLOUR = "ColourableFill";
	
	/**
	 * Returns the current outline colour used when rendering this object.
	 * @return the current outline colour
	 */
	public final ReadonlyColour getOutlineColour() {
		return outlineColour;
	}

	/**
	 * Changes the outline colour used to render this object.
	 * @param outlineColour the new outline colour
	 */
	public final void setOutlineColour(Colour c) {
		Colour old = outlineColour;
		outlineColour = c;
		firePropertyChange(PROPERTY_OUTLINE_COLOUR, old, c);
		
		old.invalidateSWTColor();
	}

	/**
	 * Returns the current fill colour used when rendering this object.
	 * @return the current fill colour
	 */
	public final ReadonlyColour getFillColour() {
		return fillColour;
	}

	/**
	 * Changes the fill colour used to render this object.
	 * @param fillColour the new fill colour
	 */
	public final void setFillColour(Colour c) {
		Colour old = fillColour;
		fillColour = c;
		firePropertyChange(PROPERTY_FILL_COLOUR, old, c);
		
		old.invalidateSWTColor();
	}

}
