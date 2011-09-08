package dk.itu.big_red.model.interfaces;

import dk.itu.big_red.model.Control;

/**
 * The abstract interface to {@link Control}s.
 * @author alec
 * @see Control
 */
public interface IControl {
	public Iterable<? extends IPort> getIPorts();
	
	public String getName();
}
