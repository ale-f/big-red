package dk.itu.big_red.model;

import java.util.ArrayList;
import java.util.List;

import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.IChangeable;

public class SimulationSpec extends ModelObject implements IChangeable {
	private abstract class SimulationSpecChange extends ModelObjectChange {
		@Override
		public SimulationSpec getCreator() {
			return SimulationSpec.this;
		}
	}
	
	public class ChangeSignature extends SimulationSpecChange {
		public Signature signature;
		
		protected ChangeSignature(Signature signature) {
			this.signature = signature;
		}
		
		private Signature oldSignature;
		
		@Override
		public void beforeApply() {
			oldSignature = getCreator().getSignature();
		}
		
		@Override
		public Change inverse() {
			return getCreator().changeSignature(oldSignature);
		}
	}
	
	public class ChangeAddRule extends SimulationSpecChange {
		public ReactionRule rule;
		
		protected ChangeAddRule(ReactionRule rule) {
			this.rule = rule;
		}
		
		@Override
		public Change inverse() {
			return getCreator().changeRemoveRule(rule);
		}
	}
	
	public class ChangeRemoveRule extends SimulationSpecChange {
		public ReactionRule rule;
		
		protected ChangeRemoveRule(ReactionRule rule) {
			this.rule = rule;
		}
		
		@Override
		public Change inverse() {
			return getCreator().changeAddRule(rule);
		}
	}
	
	public class ChangeModel extends SimulationSpecChange {
		public Bigraph model;
		
		protected ChangeModel(Bigraph model) {
			this.model = model;
		}
		
		private Bigraph oldModel;
		
		@Override
		public void beforeApply() {
			oldModel = getCreator().getModel();
		}
		
		@Override
		public Change inverse() {
			return getCreator().changeModel(oldModel);
		}
	}
	
	private Signature signature;
	
	protected SimulationSpec setSignature(Signature signature) {
		this.signature = signature;
		return this;
	}
	
	public Signature getSignature() {
		return signature;
	}
	
	private ArrayList<ReactionRule> rules = new ArrayList<ReactionRule>();
	
	protected SimulationSpec clearRules() {
		getRules().clear();
		return this;
	}
	
	protected SimulationSpec addRule(ReactionRule r) {
		getRules().add(r);
		return this;
	}
	
	protected SimulationSpec removeRule(ReactionRule r) {
		getRules().remove(r);
		return this;
	}
	
	public List<ReactionRule> getRules() {
		return rules;
	}
	
	private Bigraph model;
	
	protected SimulationSpec setModel(Bigraph model) {
		this.model = model;
		return this;
	}
	
	public Bigraph getModel() {
		return model;
	}
	
	public ChangeSignature changeSignature(Signature signature) {
		return new ChangeSignature(signature);
	}
	
	public ChangeAddRule changeAddRule(ReactionRule rule) {
		return new ChangeAddRule(rule);
	}
	
	public ChangeRemoveRule changeRemoveRule(ReactionRule rule) {
		return new ChangeRemoveRule(rule);
	}

	public ChangeModel changeModel(Bigraph model) {
		return new ChangeModel(model);
	}
	
	@Override
	public void tryValidateChange(Change b) throws ChangeRejectedException {
		if (b instanceof ChangeGroup) {
			for (Change i : (ChangeGroup)b)
				tryValidateChange(i);
		} else if (b instanceof ChangeSignature ||
				b instanceof ChangeAddRule ||
				b instanceof ChangeRemoveRule ||
				b instanceof ChangeModel) {
			/* do nothing */
		} else {
			throw new ChangeRejectedException(this, b, this,
					"The Change was not recognised");
		}
	}

	@Override
	public void tryApplyChange(Change b) throws ChangeRejectedException {
		tryValidateChange(b);
		doChange(b);
	}
	
	private void doChange(Change b) {
		b.beforeApply();
		if (b instanceof ChangeGroup) {
			for (Change i : (ChangeGroup)b)
				doChange(i);
		} else if (b instanceof ChangeSignature) {
			setSignature(((ChangeSignature) b).signature);
		} else if (b instanceof ChangeAddRule) {
			addRule(((ChangeAddRule) b).rule);
		} else if (b instanceof ChangeRemoveRule) {
			removeRule(((ChangeRemoveRule) b).rule);
		} else if (b instanceof ChangeModel) {
			setModel(((ChangeModel) b).model);
		}
	}
}
