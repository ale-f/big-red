package dk.itu.big_red.model.assistants;

import org.eclipse.core.resources.IFile;

/**
 * ResourceWrappers associate a model object with an {@link IFile}, typically
 * the one that backs them in the project.
 * @author alec
 */
public class ResourceWrapper<T> {
	private T model = null;
	private IFile resource = null;
	
	public ResourceWrapper() {
	}
	
	public ResourceWrapper(T model, IFile resource) {
		setModel(model);
		setResource(resource);
	}
	
	/**
	 * Sets the model object.
	 * @param model a model object
	 */
	public void setModel(T model) {
		if (model != null)
			this.model = model;
	}
	
	/**
	 * Gets the model object.
	 * @return the current model object
	 */
	public T getModel() {
		return model;
	}
	
	/**
	 * Sets the {@link IFile} associated with the model object.
	 * @param resource an IFile
	 */
	public void setResource(IFile resource) {
		if (resource != null)
			this.resource = resource;
	}
	
	/**
	 * Gets the {@link IFile} associated with the model object.
	 * @return an IFile
	 */
	public IFile getResource() {
		return resource;
	}
}
