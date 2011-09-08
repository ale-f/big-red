package dk.itu.big_red.model.interfaces;

import dk.itu.big_red.model.Signature;

/**
 * The abstract interface to {@link Signature}s.
 * @author alec
 * @see Signature
 */
public interface ISignature {
	public Iterable<? extends IControl> getIControls();
}
