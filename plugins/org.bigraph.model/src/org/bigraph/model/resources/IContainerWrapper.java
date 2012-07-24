package org.bigraph.model.resources;

public interface IContainerWrapper extends IResourceWrapper {
	public IResourceWrapper getResource(String path);
}
