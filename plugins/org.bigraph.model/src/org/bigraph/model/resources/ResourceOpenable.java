package org.bigraph.model.resources;

import java.io.InputStream;

public class ResourceOpenable implements IOpenable {
	private final String path;
	private final Class<?> klass;
	
	public ResourceOpenable(String path) {
		this(null, path);
	}
	
	public ResourceOpenable(Class<?> klass, String path) {
		this.path = path;
		this.klass = (klass != null ? klass : ResourceOpenable.class);
	}
	
	@Override
	public InputStream open() {
		return klass.getResourceAsStream(path);
	}
}
