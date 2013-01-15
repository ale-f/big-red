package org.bigraph.model.interfaces;

import java.util.Collection;

import org.bigraph.model.Node;

/**
 * The abstract interface to {@link Node}s.
 * @author alec
 * @see Node
 */
public interface INode extends IParent, IChild {
	IControl getControl();
	
	Collection<? extends IPort> getPorts();
}
