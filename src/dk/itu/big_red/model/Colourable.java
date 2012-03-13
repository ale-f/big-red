package dk.itu.big_red.model;

import java.util.Map;

import dk.itu.big_red.model.assistants.Colour;
import dk.itu.big_red.model.assistants.IPropertyProviderProxy;
import dk.itu.big_red.model.assistants.ReadonlyColour;

/**
 * {@link ModelObject}s which can have fill and outline colours are subclasses
 * of <strong>Colourable</strong>.
 * @author alec
 *
 */
public abstract class Colourable extends ModelObject {
	abstract class ColourableChange extends ModelObjectChange {
		public Colour newColour;
		
		private ColourableChange(Colour newColour) {
			this.newColour = newColour;
		}

		protected Colour oldColour;
		
		@Override
		public boolean canInvert() {
			return (oldColour != null);
		}
		
		@Override
		public boolean isReady() {
			return (newColour != null);
		}
		
		@Override
		public Colourable getCreator() {
			return Colourable.this;
		}
	}
	
	public class ChangeOutlineColour extends ColourableChange {
		protected ChangeOutlineColour(Colour newColour) {
			super(newColour);
		}

		@Override
		public void beforeApply() {
			oldColour = getCreator().getOutlineColour().getCopy();
		}
		
		@Override
		public ColourableChange inverse() {
			return getCreator().changeOutlineColour(oldColour);
		}
		
		@Override
		public String toString() {
			return "Change(set outline colour of " + getCreator() + " to " + newColour + ")";
		}
	}
	
	public class ChangeFillColour extends ColourableChange {
		protected ChangeFillColour(Colour newColour) {
			super(newColour);
		}

		@Override
		public void beforeApply() {
			oldColour = getCreator().getFillColour().getCopy();
		}
		
		@Override
		public ColourableChange inverse() {
			return getCreator().changeFillColour(oldColour);
		}
		
		@Override
		public String toString() {
			return "Change(set fill colour of " + getCreator() + " to " + newColour + ")";
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
	
	public ReadonlyColour getOutlineColour(IPropertyProviderProxy context) {
		return (ReadonlyColour)getProperty(context, PROPERTY_OUTLINE);
	}
	
	/**
	 * Gets the current fill colour used to render this object.
	 * @return the current fill colour
	 */
	public final ReadonlyColour getFillColour() {
		return fillColour;
	}

	public ReadonlyColour getFillColour(IPropertyProviderProxy context) {
		return (ReadonlyColour)getProperty(context, PROPERTY_FILL);
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

	public ColourableChange changeOutlineColour(Colour c) {
		return new ChangeOutlineColour(c);
	}
	
	public ColourableChange changeFillColour(Colour c) {
		return new ChangeFillColour(c);
	}
	
	@Override
	public Object getProperty(String name) {
		if (name.equals(PROPERTY_FILL)) {
			return getFillColour();
		} else if (name.equals(PROPERTY_OUTLINE)) {
			return getOutlineColour();
		} else return super.getProperty(name);
	}
	
	@Override
	public Colourable clone(Map<ModelObject, ModelObject> m) {
		Colourable c = (Colourable)super.clone(m);
		c.setFillColour(getFillColour().getCopy());
		c.setOutlineColour(getOutlineColour().getCopy());
		return c;
	}
}
