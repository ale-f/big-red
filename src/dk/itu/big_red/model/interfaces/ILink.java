package dk.itu.big_red.model.interfaces;

import dk.itu.big_red.model.Link;

/**
 * The abstract interface to {@link Link}s.
 * @author alec
 * @see Link
 */
public interface ILink extends IEntity {
	public Iterable<? extends IPoint> getIPoints();
	
	public String getName();
}
