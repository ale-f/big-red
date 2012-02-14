package dk.itu.big_red.utilities.resources;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

/**
 * Utility methods for dealing with Eclipse content types.
 * @author alec
 *
 */
public final class Types {
	private Types() {}
	
	public static final String BIGRAPH_XML = "dk.itu.big_red.bigraph";
	public static final String SIGNATURE_XML = "dk.itu.big_red.signature";
	public static final String RULE_XML = "dk.itu.big_red.rule";
	public static final String SIMULATION_SPEC_XML = "dk.itu.big_red.simulation_spec";
	
	/**
	 * Gets the content type manager.
	 * @return the content type manager
	 */
	public static IContentTypeManager getContentTypeManager() {
		return Platform.getContentTypeManager();
	}
	
	/**
	 * Gets the {@link IContentType} with the given ID, if there is one.
	 * @param contentTypeIdentifier a content type ID
	 * @return an IContentType, or <code>null</code> if the ID was unknown
	 */
	public static IContentType getContentType(String contentTypeIdentifier) {
		return getContentTypeManager().getContentType(contentTypeIdentifier);
	}
	
	/**
	 * Gets the {@link IContentType} for a given {@link IFile}.
	 * @param file an IFile
	 * @return an IContentType
	 */
	public static IContentType findContentTypeFor(IFile file) {
		try {
			return file.getContentDescription().getContentType();
		} catch (CoreException e) {
			return null;
		}
	}
}
