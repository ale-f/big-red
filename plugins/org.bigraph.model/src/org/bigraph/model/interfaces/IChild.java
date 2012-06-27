package org.bigraph.model.interfaces;

/**
 * The abstract interface to model objects which can occur as children.
 * @author alec
 */
public interface IChild extends IPlace {
	IParent getIParent();
}
