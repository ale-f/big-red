package dk.itu.big_red.model.changes;

import dk.itu.big_red.application.RedException;

public class ChangeRejectedException extends RedException {
	private static final long serialVersionUID = 7181613421769493596L;

	private IChangeable changeable;
	private Change rejectedChange;
	private Object rejector;
	private String rationale;
	
	public ChangeRejectedException(IChangeable changeable, Change rejectedChange, Object rejector, String rationale) {
		this.changeable = changeable;
		this.rejectedChange = rejectedChange;
		this.rejector = rejector;
		this.rationale = rationale;
	}
	
	public IChangeable getChangeable() {
		return changeable;
	}
	
	public Change getRejectedChange() {
		return rejectedChange;
	}
	
	public Object getRejector() {
		return rejector;
	}
	
	public String getRationale() {
		return rationale;
	}
	
	@Override
	public String getMessage() {
		return "The attempt to change " + changeable +
				" by applying change " + rejectedChange +
				" was rejected by " + rejector +
				", which gave the rationale \"" + rationale + "\".";
	}
}
