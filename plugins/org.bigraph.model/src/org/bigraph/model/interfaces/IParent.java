package org.bigraph.model.interfaces;

import java.util.Collection;

public interface IParent extends IPlace {
	Collection<? extends INode> getNodes();
	Collection<? extends ISite> getSites();
	
	Collection<? extends IChild> getIChildren();
}
