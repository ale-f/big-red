package org.bigraph.model.interfaces;

import org.bigraph.model.Node;

/**
 * The abstract interface to {@link Node}s.
 * @author alec
 * @see Node
 */
public interface INode extends IParent, IChild {
	public IControl getControl();
	
	public Iterable<? extends IPort> getPorts();
}
