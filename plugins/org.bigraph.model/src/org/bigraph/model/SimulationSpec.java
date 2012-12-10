package org.bigraph.model;

import java.util.ArrayList;
import java.util.List;
import org.bigraph.model.ModelObject;
import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IChangeExecutor;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.changes.descriptors.experimental.DescriptorExecutorManager;

public class SimulationSpec extends ModelObject
		implements IChangeExecutor, Resolver {
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
	
	abstract class SimulationSpecChange extends ModelObjectChange {
		@Override
		public SimulationSpec getCreator() {
			return SimulationSpec.this;
		}
	}
	
	public final class ChangeSignature extends SimulationSpecChange {
		public final Signature signature;
		
		protected ChangeSignature(Signature signature) {
			this.signature = signature;
		}
		
		private Signature oldSignature;
		
		@Override
		public void beforeApply() {
			oldSignature = getCreator().getSignature();
		}
		
		@Override
		public ChangeSignature inverse() {
			return new ChangeSignature(oldSignature);
		}
		
		@Override
		public String toString() {
			return "Change(set signature of " + getCreator() +
					" to " + signature + ")";
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			context.setProperty(getCreator(),
					SimulationSpec.PROPERTY_SIGNATURE, signature);
		}
	}
	
	public final class ChangeAddRule extends SimulationSpecChange {
		public final ReactionRule rule;
		public final int position;
		
		protected ChangeAddRule(ReactionRule rule) {
			this(rule, -1);
		}
		
		protected ChangeAddRule(ReactionRule rule, int position) {
			this.rule = rule;
			this.position = position;
		}
		
		@Override
		public ChangeRemoveRule inverse() {
			return new ChangeRemoveRule(rule);
		}
		
		@Override
		public String toString() {
			return "Change(add reaction rule " + rule + " to " +
					getCreator() + ")";
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			List<ReactionRule> l =
					context.<ReactionRule>getModifiableList(
							getCreator(),
							SimulationSpec.PROPERTY_RULE, getRules());
			if (position == -1) {
				l.add(rule);
			} else l.add(position, rule);
		}
	}
	
	public final class ChangeRemoveRule extends SimulationSpecChange {
		public final ReactionRule rule;
		
		protected ChangeRemoveRule(ReactionRule rule) {
			this.rule = rule;
		}
		
		private int actualPosition = -1;
		@Override
		public void beforeApply() {
			actualPosition = getCreator().getRules().indexOf(rule);
		}
		
		@Override
		public boolean canInvert() {
			return (actualPosition != -1);
		}
		
		@Override
		public ChangeAddRule inverse() {
			return new ChangeAddRule(rule, actualPosition);
		}
		
		@Override
		public String toString() {
			return "Change(remove reaction rule " + rule + " from " +
					getCreator() + ")";
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			context.<ReactionRule>getModifiableList(getCreator(),
					SimulationSpec.PROPERTY_RULE, getRules()).remove(rule);
		}
	}
	
	public final class ChangeModel extends SimulationSpecChange {
		public final Bigraph model;
		
		protected ChangeModel(Bigraph model) {
			this.model = model;
		}
		
		private Bigraph oldModel;
		
		@Override
		public void beforeApply() {
			oldModel = getCreator().getModel();
		}
		
		@Override
		public ChangeModel inverse() {
			return new ChangeModel(oldModel);
		}
		
		@Override
		public String toString() {
			return "Change(set model of " + getCreator() +
					" to " + model + ")";
		}
		
		@Override
		public void simulate(PropertyScratchpad context) {
			context.setProperty(getCreator(),
					SimulationSpec.PROPERTY_MODEL, model);
		}
	}
	
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
	
	public IChange changeSignature(Signature signature) {
		return new ChangeSignature(signature);
	}
	
	public IChange changeAddRule(ReactionRule rule) {
		return new ChangeAddRule(rule);
	}
	
	public IChange changeRemoveRule(ReactionRule rule) {
		return new ChangeRemoveRule(rule);
	}

	public IChange changeModel(Bigraph model) {
		return new ChangeModel(model);
	}
	
	@Override
	public void tryValidateChange(IChange b) throws ChangeRejectedException {
		ExecutorManager.getInstance().tryValidateChange(b);
	}

	@Override
	public void tryApplyChange(IChange b) throws ChangeRejectedException {
		ExecutorManager.getInstance().tryApplyChange(b);
	}
	
	static {
		ExecutorManager.getInstance().addHandler(new SimulationSpecHandler());
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
			return NamedModelObject.Identifier.require(r.lookup(context, this),
					SimulationSpec.class);
		}
	}
	
	abstract static class SimulationSpecChangeDescriptor
			extends ModelObjectChangeDescriptor {
		static {
			DescriptorExecutorManager.getInstance().addHandler(
					new SimulationSpecDescriptorHandler());
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
		public IChange createChange(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			SimulationSpec ss = getTarget().lookup(context, r);
			if (ss == null)
				throw new ChangeCreationException(this,
						"" + getTarget() + ": lookup failed");
			return ss.changeModel(getNewModel());
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeSetModelDescriptor(
					getTarget(), getNewModel(), getOldModel());
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
		public IChange createChange(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			SimulationSpec ss = getTarget().lookup(context, r);
			if (ss == null)
				throw new ChangeCreationException(this,
						"" + getTarget() + ": lookup failed");
			return ss.changeSignature(getNewSignature());
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeSetSignatureDescriptor(
					getTarget(), getNewSignature(), getOldSignature());
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
		public IChange createChange(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			SimulationSpec ss = getTarget().lookup(context, r);
			if (ss == null)
				throw new ChangeCreationException(this,
						"" + getTarget() + ": lookup failed");
			return ss.changeAddRule(getRule());
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeRemoveRuleDescriptor(
					getTarget(), getPosition(), getRule());
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
		public IChange createChange(PropertyScratchpad context, Resolver r)
				throws ChangeCreationException {
			SimulationSpec ss = getTarget().lookup(context, r);
			if (ss == null)
				throw new ChangeCreationException(this,
						"" + getTarget() + ": lookup failed");
			return ss.changeRemoveRule(getRule());
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeAddRuleDescriptor(
					getTarget(), getPosition(), getRule());
		}
	}

	@Override
	public Object lookup(PropertyScratchpad context,
			ModelObject.Identifier identifier) {
		if (identifier instanceof SimulationSpec.Identifier) {
			return this;
		} else return null;
	}
}
