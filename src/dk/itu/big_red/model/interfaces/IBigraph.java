package dk.itu.big_red.model.interfaces;

public interface IBigraph {
	public ISignature getISignature();
	
	public Iterable<IEdge> getIEdges();
	public Iterable<IRoot> getIRoots();
	public Iterable<IInnerName> getIInnerNames();
	public Iterable<IOuterName> getIOuterNames();
}
