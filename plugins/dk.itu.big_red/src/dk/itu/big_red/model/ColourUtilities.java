package dk.itu.big_red.model;

import org.bigraph.model.Control;
import org.bigraph.model.Link;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Node;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

import static dk.itu.big_red.model.ExtendedDataUtilities.require;
import static dk.itu.big_red.model.ExtendedDataUtilities.set;

/**
 * The <strong>ColourUtilities</strong> class is a collection of static
 * methods and fields for manipulating objects' outline and fill colours.
 * @author alec
 * @see ExtendedDataUtilities
 * @see LayoutUtilities
 */
public abstract class ColourUtilities {
	private ColourUtilities() {}

	@RedProperty(fired = Colour.class, retrieved = Colour.class)
	public static final String FILL =
			"eD!+dk.itu.big_red.model.Colourable.fill";
	
	public static Colour getFill(ModelObject m) {
		return getFill(null, m);
	}
	
	public static Colour getFill(
			PropertyScratchpad context, ModelObject m) {
		Colour c = getFillRaw(context, m);
		if (c == null)
			c = getDefaultFill(context, m);
		return c;
	}

	public static Colour getDefaultFill(ModelObject m) {
		return getDefaultFill(null, m);
	}
	
	public static Colour getDefaultFill(
			PropertyScratchpad context, ModelObject m) {
		if (m instanceof Node) {
			return getFill(context, ((Node)m).getControl());
		} else if (m instanceof Control) {
			return new Colour("white");
		} else return null;
	}
	
	public static Colour getFillRaw(ModelObject m) {
		return getFillRaw(null, m);
	}
	
	public static Colour getFillRaw(
			PropertyScratchpad context, ModelObject m) {
		return require(context, m, FILL, Colour.class);
	}
	
	public static void setFill(ModelObject m, Colour c) {
		setFill(null, m, c);
	}

	public static void setFill(
			PropertyScratchpad context, ModelObject m, Colour c) {
		set(context, m, FILL, c);
	}

	public static IChange changeFill(ModelObject m, Colour c) {
		return m.changeExtendedData(FILL, c);
	}
	
	public static IChangeDescriptor changeFillDescriptor(
			ModelObject.Identifier l, Colour oldC, Colour newC) {
		return new ModelObject.ChangeExtendedDataDescriptor(
				l, FILL, oldC, newC, null, null, null);
	}

	@RedProperty(fired = Colour.class, retrieved = Colour.class)
	public static final String OUTLINE =
			"eD!+dk.itu.big_red.model.Colourable.outline";
	
	public static Colour getOutline(ModelObject m) {
		return getOutline(null, m);
	}

	public static Colour getOutline(
			PropertyScratchpad context, ModelObject m) {
		Colour c = require(context, m, OUTLINE, Colour.class);
		if (c == null)
			c = getDefaultOutline(context, m);
		return c;
	}

	public static Colour getDefaultOutline(ModelObject m) {
		return getDefaultOutline(null, m);
	}
	
	public static Colour getDefaultOutline(
			PropertyScratchpad context, ModelObject m) {
		if (m instanceof Node) {
			return getOutline(context, ((Node)m).getControl());
		} else if (m instanceof Control) {
			return new Colour("black");
		} else if (m instanceof Link) {
			return new Colour("green");
		} else return null;
	}
	
	public static Colour getOutlineRaw(ModelObject m) {
		return getOutlineRaw(null, m);
	}
	
	public static Colour getOutlineRaw(
			PropertyScratchpad context, ModelObject m) {
		return require(context, m, OUTLINE, Colour.class);
	}
	
	public static void setOutline(ModelObject m, Colour c) {
		setOutline(null, m, c);
	}

	public static void setOutline(
			PropertyScratchpad context, ModelObject m, Colour c) {
		set(context, m, OUTLINE, c);
	}

	public static IChange changeOutline(ModelObject m, Colour c) {
		return m.changeExtendedData(OUTLINE, c);
	}
	
	public static IChangeDescriptor changeOutlineDescriptor(
			ModelObject.Identifier l, Colour oldC, Colour newC) {
		return new ModelObject.ChangeExtendedDataDescriptor(
				l, OUTLINE, oldC, newC, null, null, null);
	}
}
