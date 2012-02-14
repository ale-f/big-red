package dk.itu.big_red.interaction_managers;

/**
 * Classes implementing <strong>IInteractionManagerFactory</strong> can create
 * a specific kind of {@link IInteractionManager}.
 * @author alec
 *
 */
public interface IInteractionManagerFactory {
	/**
	 * Returns the display name for this factory.
	 * @return this factory's name
	 */
	public String getName();
	
	/**
	 * Creates a new {@link IInteractionManager}.
	 * @return a new {@link IInteractionManager}
	 */
	public IInteractionManager createInteractionManager();
}
