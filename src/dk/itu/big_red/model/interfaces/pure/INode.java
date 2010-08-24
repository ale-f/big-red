package dk.itu.big_red.model.interfaces.pure;

import java.util.Collection;

public interface INode extends IPlace {
	public IPlace getParent();
	
	public Collection<INode> getNodes();
	public Collection<IPort> getPorts();
	public Collection<ISite> getSites();
}
