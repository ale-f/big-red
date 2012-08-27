package org.bigraph.model.resources;

import java.io.File;

public class JavaContainerWrapper extends JavaResourceWrapper implements
		IContainerWrapper {
	public JavaContainerWrapper(File file) {
		super(file);
	}

	@Override
	public IResourceWrapper getResource(String path) {
		File f = new File(getResource(), path);
		return (f.isDirectory() ? new JavaContainerWrapper(f) :
			f.isFile() ? new JavaFileWrapper(f) : null);
	}
}
