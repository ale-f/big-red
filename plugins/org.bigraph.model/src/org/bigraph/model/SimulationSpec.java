package org.bigraph.model;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.ModelObject;
import org.bigraph.model.assistants.IObjectIdentifier;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

public class SimulationSpec extends ModelObject implements Resolver {
	/**
	 * The property name fired when a rule is added or removed.
	 */
	@RedProperty(fired = ReactionRule.class, retrieved = List.class)
	public static final String PROPERTY_RULE = "SimulationSpecRule";
	
	/**
	 * The property name fired when the model changes.
	 */
	@RedProperty(fired = Bigraph.class, retrieved = Bigraph.class)
	public static final String PROPERTY_MODEL = "SimulationSpecModel";
	
	/**
	 * The property name fired when the signature changes.
	 */
	@RedProperty(fired = Signature.class, retrieved = Signature.class)
	public static final String PROPERTY_SIGNATURE = "SimulationSpecSignature";
	
	private Signature signature;
	
	protected void setSignature(Signature signature) {
		Signature oldSignature = this.signature;
		this.signature = signature;
		firePropertyChange(PROPERTY_SIGNATURE, oldSignature, signature);
	}
	
	public Signature getSignature() {
		return signature;
	}
	
	public Signature getSignature(PropertyScratchpad context) {
		return getProperty(context, PROPERTY_SIGNATURE, Signature.class);
	}
	
	@Override
	public SimulationSpec clone() {
		SimulationSpec ss = (SimulationSpec)super.clone();
		
		ss.setSignature(getSignature().clone());
		for (ReactionRule r : getRules())
			ss.addRule(-1, r.clone());
		ss.setModel(getModel().clone());
		
		return ss;
	}
	
	private ArrayList<ReactionRule> rules = new ArrayList<ReactionRule>();
	
	protected void addRule(int position, ReactionRule r) {
		if (position == -1) {
			rules.add(r);
		} else rules.add(position, r);
		firePropertyChange(PROPERTY_RULE, null, r);
	}
	
	protected void removeRule(ReactionRule r) {
		rules.remove(r);
		firePropertyChange(PROPERTY_RULE, r, null);
	}
	
	public List<? extends ReactionRule> getRules() {
		return rules;
	}
	
	@SuppressWarnings("unchecked")
	public List<? extends ReactionRule> getRules(PropertyScratchpad context) {
		return getProperty(context, PROPERTY_RULE, List.class);
	}
	
	private Bigraph model;

	public static final String CONTENT_TYPE = "dk.itu.big_red.simulation_spec";
	
	protected void setModel(Bigraph model) {
		Bigraph oldModel = this.model;
		this.model = model;
		firePropertyChange(PROPERTY_MODEL, oldModel, model);
	}
	
	public Bigraph getModel() {
		return model;
	}
	
	public Bigraph getModel(PropertyScratchpad context) {
		return getProperty(context, PROPERTY_MODEL, Bigraph.class);
	}
	
	@Override
	public void dispose() {
		if (model != null) {
			model.dispose();
			model = null;
		}
		
		if (signature != null) {
			signature.dispose();
			signature = null;
		}
		
		if (rules != null) {
			for (ReactionRule r : rules)
				r.dispose();
			rules.clear();
			rules = null;
		}
		
		super.dispose();
	}
	
	/**
	 * {@inheritDoc}
	 * <p><strong>Special notes for {@link SimulationSpec}:</strong>
	 * <ul>
	 * <li>Passing {@link #PROPERTY_RULE} will return a {@link List}&lt;{@link
	 * ReactionRule}&gt;, <strong>not</strong> a {@link ReactionRule}.
	 * </ul>
	 */
	@Override
	protected Object getProperty(String name) {
		if (PROPERTY_SIGNATURE.equals(name)) {
			return getSignature();
		} else if (PROPERTY_MODEL.equals(name)) {
			return getModel();
		} else if (PROPERTY_RULE.equals(name)) {
			return getRules();
		} else return super.getProperty(name);
	}
	
	public static class Identifier implements ModelObject.Identifier {
		@Override
		public SimulationSpec lookup(PropertyScratchpad context, Resolver r) {
			return require(r.lookup(context, this), SimulationSpec.class);
		}
	}
	
	abstract static class SimulationSpecChangeDescriptor
			extends ModelObjectChangeDescriptor {
		static {
			DescriptorExecutorManager.getInstance().addParticipant(new SimulationSpecDescriptorHandler());
		}
	}
	
	public static final class ChangeSetModelDescriptor
			extends SimulationSpecChangeDescriptor {
		private final Identifier target;
		private final Bigraph oldModel;
		private final Bigraph newModel;
		
		public ChangeSetModelDescriptor(
				Identifier target, Bigraph oldModel, Bigraph newModel) {
			this.target = target;
			this.oldModel = oldModel;
			this.newModel = newModel;
		}
		
		public Identifier getTarget() {
			return target;
		}
		
		public Bigraph getOldModel() {
			return oldModel;
		}
		
		public Bigraph getNewModel() {
			return newModel;
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeSetModelDescriptor(
					getTarget(), getNewModel(), getOldModel());
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r) {
			SimulationSpec self = getTarget().lookup(context, r);
			context.setProperty(self, PROPERTY_MODEL, getNewModel());
		}
	}
	
	public static final class ChangeSetSignatureDescriptor
			extends SimulationSpecChangeDescriptor {
		private final Identifier target;
		private final Signature oldSignature;
		private final Signature newSignature;
		
		public ChangeSetSignatureDescriptor(Identifier target,
				Signature oldSignature, Signature newSignature) {
			this.target = target;
			this.oldSignature = oldSignature;
			this.newSignature = newSignature;
		}
		
		public Identifier getTarget() {
			return target;
		}
		
		public Signature getOldSignature() {
			return oldSignature;
		}
		
		public Signature getNewSignature() {
			return newSignature;
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeSetSignatureDescriptor(
					getTarget(), getNewSignature(), getOldSignature());
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r) {
			SimulationSpec self = getTarget().lookup(context, r);
			context.setProperty(self, PROPERTY_SIGNATURE, getNewSignature());
		}
	}
	
	public static final class ChangeAddRuleDescriptor
			extends SimulationSpecChangeDescriptor {
		private final Identifier target;
		private final int position;
		private final ReactionRule rule;
		
		public ChangeAddRuleDescriptor(
				Identifier target, int position, ReactionRule rule) {
			this.target = target;
			this.position = position;
			this.rule = rule;
		}
		
		public Identifier getTarget() {
			return target;
		}
		
		public int getPosition() {
			return position;
		}
		
		public ReactionRule getRule() {
			return rule;
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeRemoveRuleDescriptor(
					getTarget(), getPosition(), getRule());
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r) {
			SimulationSpec self = getTarget().lookup(context, r);
			List<ReactionRule> l =
					context.<ReactionRule>getModifiableList(
							self, PROPERTY_RULE, self.getRules());
			if (getPosition() == -1) {
				l.add(getRule());
			} else l.add(getPosition(), getRule());
		}
	}
	
	public static final class ChangeRemoveRuleDescriptor
			extends SimulationSpecChangeDescriptor {
		private final Identifier target;
		private final int position;
		private final ReactionRule rule;
		
		public ChangeRemoveRuleDescriptor(
				Identifier target, int position, ReactionRule rule) {
			this.target = target;
			this.position = position;
			this.rule = rule;
		}
		
		public Identifier getTarget() {
			return target;
		}
		
		public int getPosition() {
			return position;
		}
		
		public ReactionRule getRule() {
			return rule;
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeAddRuleDescriptor(
					getTarget(), getPosition(), getRule());
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver r) {
			SimulationSpec self = getTarget().lookup(context, r);
			context.<ReactionRule>getModifiableList(
					self, PROPERTY_RULE, self.getRules()).remove(getRule());
		}
	}

	@Override
	public Object lookup(PropertyScratchpad context,
			IObjectIdentifier identifier) {
		if (identifier instanceof SimulationSpec.Identifier) {
			return this;
		} else return null;
	}
}
