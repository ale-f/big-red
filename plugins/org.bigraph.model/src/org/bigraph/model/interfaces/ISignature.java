package org.bigraph.model.interfaces;

import java.util.Collection;

import org.bigraph.model.Signature;

/**
 * The abstract interface to {@link Signature}s.
 * @author alec
 * @see Signature
 */
public interface ISignature {
	Collection<? extends IControl> getControls();
}
