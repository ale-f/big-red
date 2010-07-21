package dk.itu.big_red.util;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

/**
 * Utility methods for dealing with Eclipse content types.
 * @author alec
 *
 */
public class Types {
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
		InputStream is = null;
		try {
			is = file.getContents();
			return getContentTypeManager().findContentTypeFor(is, file.getName());
		} catch (Exception e) {
			/* do nothing */
		} finally {
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				/* do nothing */
			}
		}
		return null;
	}
}
