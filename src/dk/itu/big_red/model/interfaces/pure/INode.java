package dk.itu.big_red.model.interfaces.pure;

public interface INode extends IPlace {
	public IPlace getIPlace();
	
	public Iterable<INode> getINodes();
	public Iterable<IPort> getIPorts();
	public Iterable<ISite> getISites();
}
