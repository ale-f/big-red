package dk.itu.big_red.model;

import org.eclipse.swt.graphics.RGB;

import dk.itu.big_red.util.Colour;

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
	public static final String PROPERTY_OUTLINE_COLOUR = "IOutlineColourableColour";
	
	/**
	 * The property name fired when the fill colour changes.
	 */
	public static final String PROPERTY_FILL_COLOUR = "IFillColourableColour";
	
	/**
	 * Returns the current outline colour used when rendering this object.
	 * @return the current outline colour
	 */
	public final RGB getOutlineColour() {
		return outlineColour.getRGB();
	}

	/**
	 * Changes the outline colour used to render this object.
	 * @param outlineColour the new outline colour
	 */
	public final void setOutlineColour(RGB outlineColour) {
		Colour c = new Colour(outlineColour),
				old = this.outlineColour;
		this.outlineColour = c;
		firePropertyChange(PROPERTY_OUTLINE_COLOUR, old, c);
	}

	/**
	 * Returns the current fill colour used when rendering this object.
	 * @return the current fill colour
	 */
	public final RGB getFillColour() {
		return fillColour.getRGB();
	}

	/**
	 * Changes the fill colour used to render this object.
	 * @param fillColour the new fill colour
	 */
	public final void setFillColour(RGB fillColour) {
		Colour c = new Colour(fillColour),
				old = this.fillColour;
		this.fillColour = c;
		firePropertyChange(PROPERTY_FILL_COLOUR, old, c);
	}

}
