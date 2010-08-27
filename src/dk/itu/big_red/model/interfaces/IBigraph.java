package dk.itu.big_red.model.interfaces;

public interface IBigraph {
	public ISignature getISignature();
	
	public Iterable<? extends IEdge> getIEdges();
	public Iterable<? extends IRoot> getIRoots();
	public Iterable<? extends IInnerName> getIInnerNames();
	public Iterable<? extends IOuterName> getIOuterNames();
}
