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
