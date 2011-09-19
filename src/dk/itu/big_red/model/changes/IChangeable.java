package dk.itu.big_red.model.changes;

public interface IChangeable {
	public boolean applyChange(Change c);
	
	public boolean validateChange(Change c);
}
