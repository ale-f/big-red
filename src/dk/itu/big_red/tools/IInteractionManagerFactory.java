package dk.itu.big_red.tools;

public interface IInteractionManagerFactory {
	public String getName();
	
	public IInteractionManager createInteractionManager();
}
