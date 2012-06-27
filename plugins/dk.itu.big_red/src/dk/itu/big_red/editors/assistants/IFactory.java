package dk.itu.big_red.editors.assistants;

public interface IFactory<T> {
	String getName();
	T newInstance();
}
