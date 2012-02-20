package dk.itu.big_red.utilities.resources;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.content.IContentType;

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
