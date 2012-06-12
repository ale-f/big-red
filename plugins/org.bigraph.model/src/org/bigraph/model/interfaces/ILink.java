package org.bigraph.model.interfaces;

import org.bigraph.model.Link;

/**
 * The abstract interface to {@link Link}s.
 * @author alec
 * @see Link
 */
public interface ILink extends IEntity {
	public Iterable<? extends IPoint> getPoints();
}
