package org.bigraph.model.interfaces;

import java.util.Collection;

import org.bigraph.model.Bigraph;

/**
 * The abstract interface to {@link Bigraph}s, for clients not interested in
 * any of Big Red's implementation-specific details.
 * @author alec
 * @see Bigraph
 */
public interface IBigraph {
	ISignature getSignature();

	Collection<? extends IEdge> getEdges();
	
	/* Inner interface */
	Collection<? extends ISite> getSites();
	Collection<? extends IInnerName> getInnerNames();
	
	/* Outer interface */
	Collection<? extends IRoot> getRoots();
	Collection<? extends IOuterName> getOuterNames();
}
