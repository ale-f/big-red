package dk.itu.big_red.model.interfaces.pure;

public interface IParent extends IPlace {
	public Iterable<INode> getINodes();
	public Iterable<ISite> getISites();
	
	public Iterable<IChild> getIChildren();
}
