package dk.itu.big_red.model;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.model.assistants.Colour;
import dk.itu.big_red.model.changes.Change;

/**
 * {@link ModelObject}s which can have fill and outline colours are subclasses
 * of <strong>Colourable</strong>.
 * @author alec
 *
 */
public abstract class Colourable extends ModelObject {
	/**
	 * Gets the current outline colour used to render this object.
	 * @return the current outline colour
	 */
	public final Colour getOutlineColour() {
		return ExtendedDataUtilities.getOutline(this);
	}
	
	/**
	 * Gets the current fill colour used to render this object.
	 * @return the current fill colour
	 */
	public final Colour getFillColour() {
		return ExtendedDataUtilities.getFill(this);
	}

	public Change changeOutlineColour(Colour c) {
		return ExtendedDataUtilities.changeOutline(this, c);
	}
	
	public Change changeFillColour(Colour c) {
		return ExtendedDataUtilities.changeFill(this, c);
	}
}
