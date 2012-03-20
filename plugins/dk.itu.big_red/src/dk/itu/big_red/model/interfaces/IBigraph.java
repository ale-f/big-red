package dk.itu.big_red.model.interfaces;

import dk.itu.big_red.model.Bigraph;

/**
 * The abstract interface to {@link Bigraph}s, for clients not interested in
 * any of Big Red's implementation-specific details.
 * @author alec
 * @see Bigraph
 */
public interface IBigraph {
	public ISignature getSignature();
	
	public Iterable<? extends IEdge> getIEdges();
	public Iterable<? extends IRoot> getIRoots();
	public Iterable<? extends IInnerName> getIInnerNames();
	public Iterable<? extends IOuterName> getIOuterNames();
}
