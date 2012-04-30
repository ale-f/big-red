package dk.itu.big_red.editors.assistants;

import org.eclipse.core.resources.IFile;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.changes.Change;

public final class ExtendedDataUtilities {
	private ExtendedDataUtilities() {}
	
	private static Object require(Object o, Class<?> klass) {
		return (klass.isInstance(o) ? o : null);
	}
	
	public static final String FILE =
			"dk.itu.big_red.model.ModelObject.file";
	
	public static IFile getFile(ModelObject m) {
		return (IFile)require(m.getExtendedData(FILE), IFile.class);
	}
	
	public static void setFile(ModelObject m, IFile f) {
		m.setExtendedData(FILE, f);
	}
	
	public static final String COMMENT =
			"dk.itu.big_red.model.ModelObject.comment";
	
	public static String getComment(ModelObject m) {
		return (String)require(m.getExtendedData(COMMENT), String.class);
	}
	
	public static void setComment(ModelObject m, String s) {
		m.setExtendedData(COMMENT, s);
	}
	
	public static Change changeComment(ModelObject m, String s) {
		return m.changeExtendedData(COMMENT, s);
	}
}
