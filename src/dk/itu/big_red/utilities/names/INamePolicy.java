package dk.itu.big_red.utilities.names;

public interface INamePolicy {
	public boolean validate(String name);
	public String getName(int value);
}
