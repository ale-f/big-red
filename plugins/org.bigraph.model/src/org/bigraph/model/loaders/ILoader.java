package org.bigraph.model.loaders;

public interface ILoader {
	void addNotice(LoaderNotice notice);
	void addNotice(LoaderNotice.Type type, String message);
}
