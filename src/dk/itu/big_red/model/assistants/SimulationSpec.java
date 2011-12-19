package dk.itu.big_red.model.assistants;

import java.util.ArrayList;
import java.util.List;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Signature;

public class SimulationSpec {
	private Signature signature;
	
	public SimulationSpec setSignature(Signature signature) {
		this.signature = signature;
		return this;
	}
	
	public Signature getSignature() {
		return signature;
	}
	
	private ArrayList<ReactionRule> rules = new ArrayList<ReactionRule>();
	
	public SimulationSpec clearRules() {
		getRules().clear();
		return this;
	}
	
	public SimulationSpec addRule(ReactionRule r) {
		getRules().add(r);
		return this;
	}
	
	public List<ReactionRule> getRules() {
		return rules;
	}
	
	private Bigraph model;
	
	public SimulationSpec setModel(Bigraph model) {
		this.model = model;
		return this;
	}
	
	public Bigraph getModel() {
		return model;
	}
}
