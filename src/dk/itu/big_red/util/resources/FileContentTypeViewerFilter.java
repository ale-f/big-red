package dk.itu.big_red.util.resources;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.viewers.Viewer;


/**
 * ContentTypeViewerFilters filter {@link IResource} trees based on Eclipse
 * content types.
 * 
 * <p>They'll always allow {@link IWorkspaceRoot}s, {@link IProject}s, and
 * {@link IFolder}s through, but they'll only allow {@link IFile}s through if
 * their content types have been added to the filter with {@link
 * #addContentType(String)}.
 * @author alec
 *
 */
public class FileContentTypeViewerFilter extends ContainerViewerFilter {
	private ArrayList<IContentType> contentTypes =
		new ArrayList<IContentType>();
	
	/**
	 * Default constructor; takes a list of content type IDs and calls {@link
	 * #addContentType(String)} with each entry.
	 */
	public FileContentTypeViewerFilter(String... contentTypes) {
		for (String i : contentTypes)
			addContentType(i);
	}
	
	/**
	 * Adds the content type with the given ID, if one exists, to the list of
	 * permitted content types.
	 * @param contentType
	 */
	public void addContentType(String contentType) {
		IContentType type = Types.getContentType(contentType);
		if (type != null)
			contentTypes.add(type);
	}
	
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof IFile) {
			IResource i = (IResource)element;
			String fileName = i.getLocation().toOSString();
			for (IContentType type : contentTypes) {
				if (type.isAssociatedWith(fileName))
					return true;
			}
			return false;
		} else return super.select(viewer, parentElement, element);
	}

}
