package dk.itu.big_red.model;

import org.bigraph.model.Layoutable;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Site;
import org.bigraph.model.ModelObject.ChangeExtendedData;
import org.bigraph.model.ModelObject.ExtendedDataValidator;
import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.BoundDescriptor;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.changes.descriptors.IDescriptorStepExecutor;
import org.bigraph.model.changes.descriptors.IDescriptorStepValidator;
import org.bigraph.model.names.policies.BoundedIntegerNamePolicy;
import org.bigraph.model.names.policies.INamePolicy;
import org.bigraph.model.process.IParticipantHost;

import static org.bigraph.model.assistants.ExtendedDataUtilities.getProperty;
import static org.bigraph.model.assistants.ExtendedDataUtilities.setProperty;

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
	
	protected static abstract class ChangeExtendedDataDescriptor<
			T extends ModelObject.Identifier, V>
			extends ModelObject.ModelObjectChangeDescriptor {
		protected static abstract class Handler
				implements IDescriptorStepExecutor, IDescriptorStepValidator {
			@Override
			public void setHost(IParticipantHost host) {
				/* do nothing */
			}
		}
		
		private final String key;
		private final T target;
		private final V oldValue, newValue;
		
		protected ChangeExtendedDataDescriptor(
				String key, T target, V oldValue, V newValue) {
			this.key = key;
			this.target = target;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}
		
		protected String getKey() {
			return key;
		}
		
		public T getTarget() {
			return target;
		}
		
		public V getOldValue() {
			return oldValue;
		}
		
		public V getNewValue() {
			return newValue;
		}
		
		protected V getNormalisedNewValue() {
			return getNewValue();
		}
		
		@Override
		public IChange createChange(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			return new BoundDescriptor(r, this);
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			ModelObject mo = getTarget().lookup(context, r);
			mo.setExtendedData(context, getKey(), getNormalisedNewValue());
		}
	}
	
	public static final class ChangeCommentDescriptor
			extends ChangeExtendedDataDescriptor<
					ModelObject.Identifier, String> {
		static {
			DescriptorExecutorManager.getInstance().addParticipant(
					new CommentHandler());
		}
		
		private static final class CommentHandler extends Handler {
			@Override
			public boolean tryValidateChange(Process context,
					IChangeDescriptor change) throws ChangeCreationException {
				final PropertyScratchpad scratch = context.getScratch();
				final Resolver resolver = context.getResolver();
				if (change instanceof ChangeCommentDescriptor) {
					ChangeCommentDescriptor cd =
							(ChangeCommentDescriptor)change;
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
				if (change instanceof ChangeCommentDescriptor) {
					ChangeCommentDescriptor cd =
							(ChangeCommentDescriptor)change;
					cd.getTarget().lookup(null, resolver).setExtendedData(
							COMMENT, cd.getNormalisedNewValue());
				} else return false;
				return true;
			}
		}
		
		public ChangeCommentDescriptor(ModelObject.Identifier target,
				String oldValue, String newValue) {
			super(COMMENT, target, oldValue, newValue);
		}
		
		@Override
		protected String getNormalisedNewValue() {
			String s = getNewValue();
			if (s != null) {
				s = s.trim();
				return (s.length() > 0 ? s : null);
			} else return null;
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeCommentDescriptor(
					getTarget(), getNewValue(), getOldValue());
		}
	}
	
	@RedProperty(fired = String.class, retrieved = String.class)
	public static final String COMMENT =
			"eD!+dk.itu.big_red.model.ModelObject.comment";
	
	public static String getComment(ModelObject m) {
		return getComment(null, m);
	}
	
	public static String getComment(
			PropertyScratchpad context, ModelObject m) {
		return getProperty(context, m, COMMENT, String.class);
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
		return getProperty(context, s, ALIAS, String.class);
	}
	
	public static void setAlias(Site s, String a) {
		setAlias(null, s, a);
	}
	
	public static void setAlias(PropertyScratchpad context, Site s, String a) {
		setProperty(context, s, ALIAS, a);
	}
	
	public static IChange changeAlias(Site s, String a) {
		return s.changeExtendedData(ALIAS, a, aliasValidator);
	}
	
	public static IChangeDescriptor changeAliasDescriptor(
			Site.Identifier s, String oldA, String newA) {
		return new Layoutable.ChangeExtendedDataDescriptor(
				s, ALIAS, oldA, newA, aliasValidator, null);
	}
}
