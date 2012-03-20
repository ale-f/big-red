package dk.itu.big_red.model.interfaces;

import dk.itu.big_red.model.Port;

/**
 * The abstract interface to {@link Port}s.
 * @author alec
 * @see Port
 * @see IPoint
 */
public interface IPort extends IPoint {
	public INode getINode();
}
