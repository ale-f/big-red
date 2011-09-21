package dk.itu.big_red.model.changes;

import dk.itu.big_red.util.RedException;

public class ChangeRejectedException extends RedException {
	private static final long serialVersionUID = 7181613421769493596L;

	private IChangeable changeable;
	private Change rejectedChange;
	private IChangeValidator rejector;
	private String rationale;
	
	public ChangeRejectedException(IChangeable changeable, Change rejectedChange, IChangeValidator rejector, String rationale) {
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
