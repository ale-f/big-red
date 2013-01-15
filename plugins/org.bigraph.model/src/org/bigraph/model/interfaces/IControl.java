package org.bigraph.model.interfaces;

import java.util.Collection;

import org.bigraph.model.Control;

/**
 * The abstract interface to {@link Control}s.
 * @author alec
 * @see Control
 */
public interface IControl {
	Collection<? extends IPort> getPorts();
	
	String getName();
}
