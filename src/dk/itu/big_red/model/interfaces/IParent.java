package dk.itu.big_red.model.interfaces;

public interface IParent extends IPlace {
	public Iterable<? extends INode> getINodes();
	public Iterable<? extends ISite> getISites();
	
	public Iterable<? extends IChild> getIChildren();
}
