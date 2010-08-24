package dk.itu.big_red.model.interfaces.pure;

public interface IRoot extends IPlace {
	public Iterable<INode> getINodes();
	public Iterable<ISite> getISites();
}
