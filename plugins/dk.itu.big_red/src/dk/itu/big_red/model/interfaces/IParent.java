package dk.itu.big_red.model.interfaces;

public interface IParent extends IPlace {
	public Iterable<? extends INode> getNodes();
	public Iterable<? extends ISite> getSites();
	
	public Iterable<? extends IChild> getIChildren();
}
