package dk.itu.big_red.model.changes.bigraph;

import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.changes.Change;

public class BigraphChangeName extends Change {
	public Layoutable model;
	public String newName;
	
	public BigraphChangeName(Layoutable model, String newName) {
		this.model = model;
		this.newName = newName;
	}

	private boolean oldNameRecorded = false;
	private String oldName;
	@Override
	public void beforeApply() {
		oldName = model.getName();
		oldNameRecorded = true;
	}
	
	@Override
	public Change inverse() {
		return new BigraphChangeName(model, oldName);
	}
	
	@Override
	public boolean canInvert() {
		return oldNameRecorded;
	}
	
	@Override
	public boolean isReady() {
		return (model != null);
	}
	
	@Override
	public String toString() {
		return "Change(set name of " + model + " to " + newName + ")";
	}
}
