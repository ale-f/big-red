package org.bigraph.model.interfaces;

import org.bigraph.model.Signature;

/**
 * The abstract interface to {@link Signature}s.
 * @author alec
 * @see Signature
 */
public interface ISignature {
	public Iterable<? extends IControl> getControls();
}
