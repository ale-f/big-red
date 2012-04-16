package dk.itu.big_red.editors.assistants;

public interface IFactory<T> {
	public String getName();
	public T newInstance();
}
