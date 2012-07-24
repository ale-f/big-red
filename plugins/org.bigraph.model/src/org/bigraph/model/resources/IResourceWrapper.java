package org.bigraph.model.resources;

public interface IResourceWrapper {
	public String getPath();
	public String getRelativePath(String relativeTo);
	public IContainerWrapper getParent();
}
