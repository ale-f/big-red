package dk.itu.big_red.model;

import org.bigraph.model.Control;
import org.bigraph.model.Link;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Node;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.assistants.ExtendedDataUtilities.ChangeExtendedDataDescriptor;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

import static org.bigraph.model.assistants.ExtendedDataUtilities.getProperty;

/**
 * The <strong>ColourUtilities</strong> class is a collection of static
 * methods and fields for manipulating objects' outline and fill colours.
 * @author alec
 * @see ExtendedDataUtilities
 * @see LayoutUtilities
 */
public abstract class ColourUtilities {
	private ColourUtilities() {}

	protected static abstract class ChangeColourDescriptor
			extends ChangeExtendedDataDescriptor<
					ModelObject.Identifier, Colour> {
		static {
			DescriptorExecutorManager.getInstance().addParticipant(
					new ColourHandler());
		}
		
		private static final class ColourHandler extends Handler {
			@Override
			public boolean tryValidateChange(Process context,
					IChangeDescriptor change) throws ChangeCreationException {
				final PropertyScratchpad scratch = context.getScratch();
				final Resolver resolver = context.getResolver();
				if (change instanceof ChangeColourDescriptor) {
					ChangeColourDescriptor cd =
							(ChangeColourDescriptor)change;
					ModelObject mo = cd.getTarget().lookup(scratch, resolver);
					if (mo == null)
						throw new ChangeCreationException(cd,
								"" + cd.getTarget() + ": lookup failed");
				} else return false;
				return true;
			}
			
			@Override
			public boolean executeChange(Resolver resolver,
					IChangeDescriptor change) {
				if (change instanceof ChangeColourDescriptor) {
					ChangeColourDescriptor cd =
							(ChangeColourDescriptor)change;
					cd.getTarget().lookup(null, resolver).setExtendedData(
							cd.getKey(),
							cd.getNormalisedNewValue(null, resolver));
				} else return false;
				return true;
			}
		}
		
		protected ChangeColourDescriptor(String key,
				ModelObject.Identifier target,
				Colour oldValue, Colour newValue) {
			super(key, target, oldValue, newValue);
		}
		
		@Override
		public String getKey() {
			return super.getKey();
		}
	}
	
	public static final class ChangeOutlineDescriptor
			extends ChangeColourDescriptor {
		public ChangeOutlineDescriptor(ModelObject.Identifier identifier,
				Colour oldValue, Colour newValue) {
			super(OUTLINE, identifier, oldValue, newValue);
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeOutlineDescriptor(
					getTarget(), getNewValue(), getOldValue());
		}
	}
	
	public static final class ChangeFillDescriptor
			extends ChangeColourDescriptor {
		public ChangeFillDescriptor(ModelObject.Identifier identifier,
				Colour oldValue, Colour newValue) {
			super(FILL, identifier, oldValue, newValue);
		}

		@Override
		public IChangeDescriptor inverse() {
			return new ChangeFillDescriptor(
					getTarget(), getNewValue(), getOldValue());
		}
	}
	
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
		return getProperty(context, m, FILL, Colour.class);
	}

	@RedProperty(fired = Colour.class, retrieved = Colour.class)
	public static final String OUTLINE =
			"eD!+dk.itu.big_red.model.Colourable.outline";
	
	public static Colour getOutline(ModelObject m) {
		return getOutline(null, m);
	}

	public static Colour getOutline(
			PropertyScratchpad context, ModelObject m) {
		Colour c = getProperty(context, m, OUTLINE, Colour.class);
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
		return getProperty(context, m, OUTLINE, Colour.class);
	}
}
