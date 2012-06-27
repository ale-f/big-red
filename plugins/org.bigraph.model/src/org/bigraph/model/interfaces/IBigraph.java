package org.bigraph.model.interfaces;

import org.bigraph.model.Bigraph;

/**
 * The abstract interface to {@link Bigraph}s, for clients not interested in
 * any of Big Red's implementation-specific details.
 * @author alec
 * @see Bigraph
 */
public interface IBigraph {
	ISignature getSignature();

	Iterable<? extends IEdge> getEdges();
	
	/* Inner interface */
	Iterable<? extends ISite> getSites();
	Iterable<? extends IInnerName> getInnerNames();
	
	/* Outer interface */
	Iterable<? extends IRoot> getRoots();
	Iterable<? extends IOuterName> getOuterNames();
}
