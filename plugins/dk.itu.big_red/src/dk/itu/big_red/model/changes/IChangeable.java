package dk.itu.big_red.model.changes;

import dk.itu.big_red.model.assistants.IPropertyProviderProxy;

/**
 * Classes implementing <strong>IChangeable</strong> define {@link Change}s
 * which can be used to modify them, and can indicate the {@link
 * IChangeExecutor} that should be used to apply those changes.
 * @author alec
 *
 */
public interface IChangeable {
	/**
	 * Returns the {@link IChangeExecutor} that can apply changes to this
	 * object.
	 * @return an {@link IChangeExecutor} (can be <code>null</code>)
	 */
	IChangeExecutor getChangeExecutor();
	
	/**
	 * Returns the {@link IChangeExecutor} that could, in the given context,
	 * apply changes to this object.
	 * @param context a context
	 * @return an {@link IChangeExecutor} (can be <code>null</code>)
	 */
	IChangeExecutor getChangeExecutor(IPropertyProviderProxy context);
}
