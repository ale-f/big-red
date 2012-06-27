package org.bigraph.model.interfaces;

public interface IParent extends IPlace {
	Iterable<? extends INode> getNodes();
	Iterable<? extends ISite> getSites();
	
	Iterable<? extends IChild> getIChildren();
}
