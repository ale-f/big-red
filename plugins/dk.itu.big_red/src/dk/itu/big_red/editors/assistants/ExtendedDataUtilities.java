package dk.itu.big_red.editors.assistants;

import org.eclipse.core.resources.IFile;

import dk.itu.big_red.model.ModelObject;

public final class ExtendedDataUtilities {
	private ExtendedDataUtilities() {}
	
	private static final String ED_FILE =
		"dk.itu.big_red.model.ModelObject.file";
	
	public static IFile getFile(ModelObject m) {
		Object o = m.getExtendedData(ED_FILE);
		return (o instanceof IFile ? (IFile)o : null);
	}
	
	public static void setFile(ModelObject m, IFile f) {
		m.setExtendedData(ED_FILE, f);
	}
}
