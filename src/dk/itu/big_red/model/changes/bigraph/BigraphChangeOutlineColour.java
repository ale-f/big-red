package dk.itu.big_red.model.changes.bigraph;

import dk.itu.big_red.model.Colourable;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.util.Colour;

public class BigraphChangeOutlineColour extends Change {
	public Colourable model;
	public Colour newColour;
	
	public BigraphChangeOutlineColour(Colourable model, Colour newColour) {
		this.model = model;
		this.newColour = newColour;
	}

	private Colour oldColour;
	@Override
	public void beforeApply() {
		oldColour = model.getOutlineColour().getCopy();
	}
	
	@Override
	public Change inverse() {
		return new BigraphChangeOutlineColour(model, oldColour);
	}
	
	@Override
	public boolean canInvert() {
		return (oldColour != null);
	}
	
	@Override
	public boolean isReady() {
		return (model != null && newColour != null);
	}
	
	@Override
	public String toString() {
		return "Change(set outline colour of " + model + " to " + newColour + ")";
	}
}
