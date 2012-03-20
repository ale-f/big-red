package dk.itu.big_red.model.interfaces;

import dk.itu.big_red.model.Point;

/**
 * The abstract interface to {@link Point}s.
 * @author alec
 * @see Point
 */
public interface IPoint extends IEntity {
	public ILink getILink();
}
