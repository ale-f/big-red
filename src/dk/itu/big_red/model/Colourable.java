package dk.itu.big_red.model;

import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.util.Colour;
import dk.itu.big_red.util.ReadonlyColour;

/**
 * {@link ModelObject}s which can have fill and outline colours are subclasses
 * of <strong>Colourable</strong>.
 * @author alec
 *
 */
public abstract class Colourable extends ModelObject {
	public class ChangeOutlineColour extends Change {
		public Colourable model;
		public Colour newColour;
		
		public ChangeOutlineColour(Colourable model, Colour newColour) {
			this.model = model;
			this.newColour = newColour;
		}

		private Colour oldColour;
		@Override
		public void beforeApply() {
			oldColour = model.getOutlineColour().getCopy();
		}
		
		@Override
		public Change inverse() {
			return new ChangeOutlineColour(model, oldColour);
		}
		
		@Override
		public boolean canInvert() {
			return (oldColour != null);
		}
		
		@Override
		public boolean isReady() {
			return (model != null && newColour != null);
		}
		
		@Override
		public String toString() {
			return "Change(set outline colour of " + model + " to " + newColour + ")";
		}
	}
	
	public class ChangeFillColour extends Change {
		public Colourable model;
		public Colour newColour;
		
		public ChangeFillColour(Colourable model, Colour newColour) {
			this.model = model;
			this.newColour = newColour;
		}

		private Colour oldColour;
		@Override
		public void beforeApply() {
			oldColour = model.getFillColour().getCopy();
		}
		
		@Override
		public Change inverse() {
			return new ChangeOutlineColour(model, oldColour);
		}
		
		@Override
		public boolean canInvert() {
			return (oldColour != null);
		}
		
		@Override
		public boolean isReady() {
			return (model != null && newColour != null);
		}
		
		@Override
		public String toString() {
			return "Change(set fill colour of " + model + " to " + newColour + ")";
		}
	}
	
	private Colour
		outlineColour = new Colour("black"),
		fillColour = new Colour("white");

	/**
	 * The property name fired when the outline colour changes. The property
	 * values are {@link Colour}s.
	 */
	public static final String PROPERTY_OUTLINE = "ColourableOutline";
	
	/**
	 * The property name fired when the fill colour changes. The property
	 * values are {@link Colour}s.
	 */
	public static final String PROPERTY_FILL = "ColourableFill";
	
	/**
	 * Gets the current outline colour used to render this object.
	 * @return the current outline colour
	 */
	public final ReadonlyColour getOutlineColour() {
		return outlineColour;
	}

	/**
	 * Sets the outline colour used to render this object.
	 * @param c the new outline colour (which will belong to this object)
	 */
	protected final void setOutlineColour(Colour c) {
		Colour old = outlineColour;
		outlineColour = c;
		firePropertyChange(PROPERTY_OUTLINE, old, c);
		
		old.invalidateSWTColor();
	}

	/**
	 * Gets the current fill colour used to render this object.
	 * @return the current fill colour
	 */
	public final ReadonlyColour getFillColour() {
		return fillColour;
	}

	/**
	 * Sets the fill colour used to render this object.
	 * @param c the new fill colour (which will belong to this object)
	 */
	protected final void setFillColour(Colour c) {
		Colour old = fillColour;
		fillColour = c;
		firePropertyChange(PROPERTY_FILL, old, c);
		
		old.invalidateSWTColor();
	}

	public Change changeOutlineColour(Colour c) {
		return new ChangeOutlineColour(this, c);
	}
	
	public Change changeFillColour(Colour c) {
		return new ChangeFillColour(this, c);
	}
	
	@Override
	public Object getProperty(String name) {
		if (name.equals(PROPERTY_FILL)) {
			return getFillColour();
		} else if (name.equals(PROPERTY_OUTLINE)) {
			return getOutlineColour();
		} else return super.getProperty(name);
	}
}
