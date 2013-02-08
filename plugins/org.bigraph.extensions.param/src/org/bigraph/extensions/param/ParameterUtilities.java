package org.bigraph.extensions.param;

import org.bigraph.model.Control;
import org.bigraph.model.Node;
import org.bigraph.model.assistants.ExtendedDataUtilities;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.names.policies.INamePolicy;

import static org.bigraph.model.assistants.ExtendedDataUtilities.getProperty;
import static org.bigraph.model.assistants.ExtendedDataUtilities.setProperty;

public abstract class ParameterUtilities {
	private ParameterUtilities() {}
	
	@RedProperty(fired = String.class, retrieved = String.class)
	public static final String PARAMETER =
			"eD!+dk.itu.big_red.model.Node.parameter";
	
	public static final class ChangeParameterDescriptor
			extends ExtendedDataUtilities.ChangeExtendedDataDescriptor<
					Node.Identifier, String> {
		static {
			DescriptorExecutorManager.getInstance().addParticipant(
					new ParameterHandler());
		}
		
		private static final class ParameterHandler extends Handler {
			@Override
			public boolean tryValidateChange(Process context,
					IChangeDescriptor change) throws ChangeCreationException {
				final PropertyScratchpad scratch = context.getScratch();
				final Resolver resolver = context.getResolver();
				if (change instanceof ChangeParameterDescriptor) {
					ChangeParameterDescriptor cd =
							(ChangeParameterDescriptor)change;
					Node n = cd.getTarget().lookup(scratch, resolver);
					if (n == null)
						throw new ChangeCreationException(cd,
								"" + cd.getTarget() + ": lookup failed");
					
					Control control = n.getControl();
					INamePolicy policy = getParameterPolicy(scratch, control);
					if (policy == null)
						throw new ChangeCreationException(cd,
								"The control " + control.getName() +
								" does not define a parameter");
					
					if (cd.getNormalisedNewValue(scratch, resolver) == null)
						throw new ChangeCreationException(cd,
								"\"" + cd.getNewValue() + "\" is not a " +
								"valid value for the parameter of " +
								control.getName());
				} else return false;
				return true;
			}
			
			@Override
			public boolean executeChange(Resolver resolver,
					IChangeDescriptor change) {
				if (change instanceof ChangeParameterDescriptor) {
					ChangeParameterDescriptor cd =
							(ChangeParameterDescriptor)change;
					cd.getTarget().lookup(null, resolver).setExtendedData(
							PARAMETER,
							cd.getNormalisedNewValue(null, resolver));
				} else return false;
				return true;
			}
		}
		
		public ChangeParameterDescriptor(Node.Identifier identifier,
				String oldValue, String newValue) {
			super(PARAMETER, identifier, oldValue, newValue);
		}
		
		@Override
		protected String getNormalisedNewValue(PropertyScratchpad context,
				Resolver r) {
			INamePolicy parameterPolicy = getParameterPolicy(
					getTarget().getControl().lookup(context, r));
			return (parameterPolicy != null ?
					parameterPolicy.normalise(getNewValue()) : null);
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeParameterDescriptor(
					getTarget(), getNewValue(), getOldValue());
		}
	}

	public static final class ChangeParameterPolicyDescriptor
			extends ExtendedDataUtilities.ChangeExtendedDataDescriptor<
					Control.Identifier, INamePolicy> {
		static {
			DescriptorExecutorManager.getInstance().addParticipant(
					new ParameterPolicyHandler());
		}
		
		private static final class ParameterPolicyHandler extends Handler {
			@Override
			public boolean tryValidateChange(Process context,
					IChangeDescriptor change) throws ChangeCreationException {
				final PropertyScratchpad scratch = context.getScratch();
				final Resolver resolver = context.getResolver();
				if (change instanceof ChangeParameterPolicyDescriptor) {
					ChangeParameterPolicyDescriptor cd =
							(ChangeParameterPolicyDescriptor)change;
					Control c = cd.getTarget().lookup(scratch, resolver);
					if (c == null)
						throw new ChangeCreationException(cd,
								"" + cd.getTarget() + ": lookup failed");
				} else return false;
				return true;
			}
			
			@Override
			public boolean executeChange(Resolver resolver,
					IChangeDescriptor change) {
				if (change instanceof ChangeParameterPolicyDescriptor) {
					ChangeParameterPolicyDescriptor cd =
							(ChangeParameterPolicyDescriptor)change;
					cd.getTarget().lookup(null, resolver).setExtendedData(
							PARAMETER_POLICY,
							cd.getNormalisedNewValue(null, resolver));
				} else return false;
				return true;
			}
		}
		
		public ChangeParameterPolicyDescriptor(Control.Identifier identifier,
				INamePolicy oldValue, INamePolicy newValue) {
			super(PARAMETER_POLICY, identifier, oldValue, newValue);
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeParameterPolicyDescriptor(
					getTarget(), getNewValue(), getOldValue());
		}
	}
	
	@RedProperty(fired = INamePolicy.class, retrieved = INamePolicy.class)
	public static final String PARAMETER_POLICY =
			"eD!+dk.itu.big_red.model.Control.parameter-policy";

	public static INamePolicy getParameterPolicy(Control c) {
		return getParameterPolicy(null, c);
	}

	public static INamePolicy getParameterPolicy(
			PropertyScratchpad context, Control c) {
		return getProperty(context, c, PARAMETER_POLICY, INamePolicy.class);
	}

	public static IChangeDescriptor changeParameterPolicyDescriptor(
			Control.Identifier c, INamePolicy oldP, INamePolicy newP) {
		return new ChangeParameterPolicyDescriptor(c, oldP, newP);
	}

	public static String getParameter(Node n) {
		return getParameter(null, n);
	}

	public static String getParameter(
			PropertyScratchpad context, Node n) {
		INamePolicy p = getParameterPolicy(context, n.getControl());
		String s = getProperty(context, n, PARAMETER, String.class),
				t = null;
		if (p != null) {
			t = p.normalise(s);
			if (t == null)
				t = p.get(0);
		}
		if (s != null ? !s.equals(t) : s != t)
			setProperty(context, n, PARAMETER, t);
		return t;
	}
	
	public static IChangeDescriptor changeParameterDescriptor(
			Node.Identifier n, String oldP, String newP) {
		return new ChangeParameterDescriptor(n, oldP, newP);
	}
}
