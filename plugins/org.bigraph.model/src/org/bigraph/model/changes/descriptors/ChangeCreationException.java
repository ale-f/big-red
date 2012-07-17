package org.bigraph.model.changes.descriptors;

public class ChangeCreationException extends Exception {
	private static final long serialVersionUID = 7744148841156557485L;
	
	private final IChangeDescriptor descriptor;
	private final String rationale;
	
	public ChangeCreationException() {
		this(null, null);
	}
	
	public ChangeCreationException(
			IChangeDescriptor descriptor, String rationale) {
		this.descriptor = descriptor;
		this.rationale = rationale;
	}
	
	public IChangeDescriptor getChangeDescriptor() {
		return descriptor;
	}
	
	public String getRationale() {
		return rationale;
	}
	
	@Override
	public String getMessage() {
		return "The change descriptor " + descriptor +
				" couldn't create a change: " + rationale;
	}
}
