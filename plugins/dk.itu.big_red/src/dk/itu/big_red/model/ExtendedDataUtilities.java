package dk.itu.big_red.model;

import org.bigraph.model.Layoutable;
import org.bigraph.model.ModelObject;
import org.bigraph.model.ModelObject.ExtendedDataNormaliser;
import org.bigraph.model.Site;
import org.bigraph.model.ModelObject.ChangeExtendedData;
import org.bigraph.model.ModelObject.ExtendedDataValidator;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.names.policies.BoundedIntegerNamePolicy;
import org.bigraph.model.names.policies.INamePolicy;

/**
 * The <strong>ExtendedDataUtilities</strong> class is a collection of static
 * methods and fields for manipulating some of the extended data used by Big
 * Red.
 * @author alec
 * @see ColourUtilities
 * @see LayoutUtilities
 */
public abstract class ExtendedDataUtilities {
	private ExtendedDataUtilities() {}
	
	static <T> T require(PropertyScratchpad context, ModelObject o,
			String name, Class<T> klass) {
		if (o != null && name != null) {
			try {
				return klass.cast(o.getExtendedData(context, name));
			} catch (ClassCastException ex) {
				return null;
			}
		} else return null;
	}
	
	static void set(PropertyScratchpad context, ModelObject o, String name,
			Object value) {
		if (o == null || name == null)
			return;
		if (context != null) {
			context.setProperty(o, name, value);
		} else o.setExtendedData(name, value);
	}
	
	private static final ExtendedDataNormaliser commentNormaliser =
			new ExtendedDataNormaliser() {
		@Override
		public Object normalise(ChangeExtendedData c, Object rawValue) {
			if (rawValue instanceof String) {
				String s = ((String)rawValue).trim();
				if (s.length() > 0)
					return s;
			}
			return null;
		}
	};
	
	@RedProperty(fired = String.class, retrieved = String.class)
	public static final String COMMENT =
			"eD!+dk.itu.big_red.model.ModelObject.comment";
	
	public static String getComment(ModelObject m) {
		return getComment(null, m);
	}
	
	public static String getComment(
			PropertyScratchpad context, ModelObject m) {
		return require(context, m, COMMENT, String.class);
	}
	
	public static void setComment(ModelObject m, String s) {
		m.setExtendedData(COMMENT, s);
	}
	
	public static IChange changeComment(ModelObject m, String s) {
		return m.changeExtendedData(COMMENT, s, null, null, commentNormaliser);
	}
	
	public static IChangeDescriptor changeCommentDescriptor(
			ModelObject.Identifier l, String oldC, String newC) {
		return new ModelObject.ChangeExtendedDataDescriptor(
				l, COMMENT, oldC, newC, null, null, commentNormaliser);
	}
	
	private static final ExtendedDataValidator aliasValidator =
			new ExtendedDataValidator() {
		@Override
		public void validate(ChangeExtendedData c, PropertyScratchpad context)
				throws ChangeRejectedException {
			if (c.newValue != null) {
				if (!(c.newValue instanceof String))
					throw new ChangeRejectedException(c,
							"Aliases must be strings");
				INamePolicy np = new BoundedIntegerNamePolicy(0);
				if (np.normalise((String)c.newValue) == null)
					throw new ChangeRejectedException(c,
							"\"" + c.newValue + "\" is not a valid alias" +
							" for " + c.getCreator());
			}
		}
	};
	
	public static final String ALIAS =
			"eD!+dk.itu.big_red.model.Site.alias";
	
	public static String getAlias(Site s) {
		return getAlias(null, s);
	}
	
	public static String getAlias(PropertyScratchpad context, Site s) {
		return require(context, s, ALIAS, String.class);
	}
	
	public static void setAlias(Site s, String a) {
		setAlias(null, s, a);
	}
	
	public static void setAlias(PropertyScratchpad context, Site s, String a) {
		set(context, s, ALIAS, a);
	}
	
	public static IChange changeAlias(Site s, String a) {
		return s.changeExtendedData(ALIAS, a, aliasValidator);
	}
	
	public static IChangeDescriptor changeAliasDescriptor(
			Site.Identifier s, String oldA, String newA) {
		return new Layoutable.ChangeExtendedDataDescriptor(
				s, ALIAS, oldA, newA, aliasValidator, null, null);
	}
}
