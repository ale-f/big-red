package org.bigraph.model.resources;

import org.bigraph.model.ModelObject;
import org.bigraph.model.loaders.LoadFailedException;

public interface IFileWrapper extends IResourceWrapper {
	public ModelObject load() throws LoadFailedException;
}
