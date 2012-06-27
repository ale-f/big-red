package org.bigraph.model.interfaces;

import org.bigraph.model.Point;

/**
 * The abstract interface to {@link Point}s.
 * @author alec
 * @see Point
 */
public interface IPoint extends IEntity {
	ILink getLink();
}
