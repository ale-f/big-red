package dk.itu.big_red.model.interfaces;

import dk.itu.big_red.model.Node;

/**
 * The abstract interface to {@link Node}s.
 * @author alec
 * @see Node
 */
public interface INode extends IParent, IChild {
	public IControl getControl();
	
	public Iterable<? extends IPort> getPorts();
}
