package org.bigraph.model.resources;

import java.io.InputStream;

/**
 * Classes implementing <strong>IOpenable</strong> can produce an {@link
 * InputStream} on demand.
 * @author alec
 */
public interface IOpenable {
	/**
	 * Opens this resource in some way, returning its contents as an {@link
	 * InputStream}.
	 * @return an {@link InputStream}, or {@code null} if something goes wrong
	 */
	InputStream open();
}
