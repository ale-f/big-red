package dk.itu.big_red.model.interfaces.pure;

public interface IRoot extends IPlace {
	public Iterable<INode> getNodes();
	public Iterable<ISite> getSites();
}
