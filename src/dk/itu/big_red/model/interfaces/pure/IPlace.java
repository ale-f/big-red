package dk.itu.big_red.model.interfaces.pure;

public interface IPlace extends IEntity {
	public Iterable<INode> getINodes();
	public Iterable<ISite> getISites();
}
