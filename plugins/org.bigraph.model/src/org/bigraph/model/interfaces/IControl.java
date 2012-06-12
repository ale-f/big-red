package org.bigraph.model.interfaces;

import org.bigraph.model.Control;

/**
 * The abstract interface to {@link Control}s.
 * @author alec
 * @see Control
 */
public interface IControl {
	public Iterable<? extends IPort> getPorts();
	
	public String getName();
}
