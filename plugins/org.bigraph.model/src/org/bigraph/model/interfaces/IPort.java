package org.bigraph.model.interfaces;

import org.bigraph.model.Port;

/**
 * The abstract interface to {@link Port}s.
 * @author alec
 * @see Port
 * @see IPoint
 */
public interface IPort extends IPoint {
	INode getNode();
}
