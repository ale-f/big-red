package org.bigraph.model.loaders;

import org.bigraph.model.resources.IFileWrapper;

public interface ILoader {
	void addNotice(LoaderNotice notice);
	void addNotice(LoaderNotice.Type type, String message);
	
	IFileWrapper getFile();
}
