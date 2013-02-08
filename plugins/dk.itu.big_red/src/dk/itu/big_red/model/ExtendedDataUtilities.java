package dk.itu.big_red.model;

import org.bigraph.model.ModelObject;
import org.bigraph.model.Site;
import org.bigraph.model.assistants.ExtendedDataUtilities.ChangeExtendedDataDescriptor;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.names.policies.BoundedIntegerNamePolicy;
import org.bigraph.model.names.policies.INamePolicy;

import static org.bigraph.model.assistants.ExtendedDataUtilities.getProperty;

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
							COMMENT, cd.getNormalisedNewValue(null, resolver));
				} else return false;
				return true;
			}
		}
		
		public ChangeCommentDescriptor(ModelObject.Identifier target,
				String oldValue, String newValue) {
			super(COMMENT, target, oldValue, newValue);
		}
		
		@Override
		protected String getNormalisedNewValue(
				PropertyScratchpad context, Resolver r) {
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
	
	public static final class ChangeAliasDescriptor
			extends org.bigraph.model.assistants.ExtendedDataUtilities.ChangeExtendedDataDescriptor<
					Site.Identifier, String> {
		static {
			DescriptorExecutorManager.getInstance().addParticipant(
					new AliasHandler());
		}

		private static final INamePolicy NAME_POLICY =
				new BoundedIntegerNamePolicy(0);
		
		private static final class AliasHandler extends Handler {
			@Override
			public boolean tryValidateChange(Process context,
					IChangeDescriptor change) throws ChangeCreationException {
				final PropertyScratchpad scratch = context.getScratch();
				final Resolver resolver = context.getResolver();
				if (change instanceof ChangeAliasDescriptor) {
					ChangeAliasDescriptor cd =
							(ChangeAliasDescriptor)change;
					ModelObject mo = cd.getTarget().lookup(scratch, resolver);
					if (mo == null)
						throw new ChangeCreationException(cd,
								"" + cd.getTarget() + ": lookup failed");
					
					String nv = cd.getNewValue();
					if (nv != null) {
						if (cd.getNormalisedNewValue(
								scratch, resolver) == null)
							throw new ChangeCreationException(cd,
									"\"" + nv + "\" is not a valid alias" +
									" for " + cd.getTarget());
					}
				} else return false;
				return true;
			}

			@Override
			public boolean executeChange(Resolver resolver,
					IChangeDescriptor change) {
				if (change instanceof ChangeAliasDescriptor) {
					ChangeAliasDescriptor cd =
							(ChangeAliasDescriptor)change;
					cd.getTarget().lookup(null, resolver).setExtendedData(
							ALIAS, cd.getNormalisedNewValue(null, resolver));
				} else return false;
				return true;
			}
		}

		public ChangeAliasDescriptor(Site.Identifier target,
				String oldValue, String newValue) {
			super(ALIAS, target, oldValue, newValue);
		}

		@Override
		protected String getNormalisedNewValue(
				PropertyScratchpad context, Resolver r) {
			return NAME_POLICY.normalise(getNewValue());
		}

		@Override
		public IChangeDescriptor inverse() {
			return new ChangeAliasDescriptor(
					getTarget(), getNewValue(), getOldValue());
		}
	}
	
	public static final String ALIAS =
			"eD!+dk.itu.big_red.model.Site.alias";
	
	public static String getAlias(Site s) {
		return getAlias(null, s);
	}
	
	public static String getAlias(PropertyScratchpad context, Site s) {
		return getProperty(context, s, ALIAS, String.class);
	}
}
