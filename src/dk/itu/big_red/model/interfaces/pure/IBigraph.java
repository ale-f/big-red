package dk.itu.big_red.model.interfaces.pure;

public interface IBigraph {
	public Iterable<IEdge> getEdges();
	public Iterable<IRoot> getRoots();
	public Iterable<IOuterName> getOuterNames();
}
