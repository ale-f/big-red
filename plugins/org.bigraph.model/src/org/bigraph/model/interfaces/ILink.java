package org.bigraph.model.interfaces;

import java.util.Collection;

import org.bigraph.model.Link;

/**
 * The abstract interface to {@link Link}s.
 * @author alec
 * @see Link
 */
public interface ILink extends IEntity {
	Collection<? extends IPoint> getPoints();
}
