package dk.itu.big_red.model.changes.bigraph;

import org.eclipse.swt.graphics.RGB;

import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.interfaces.internal.IOutlineColourable;

public class BigraphChangeOutlineColour extends Change {
	public IOutlineColourable model;
	public RGB newColour;
	
	public BigraphChangeOutlineColour(IOutlineColourable model, RGB newColour) {
		this.model = model;
		this.newColour = newColour;
	}

	private RGB oldColour;
	@Override
	public void beforeApply() {
		oldColour = model.getOutlineColour();
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
