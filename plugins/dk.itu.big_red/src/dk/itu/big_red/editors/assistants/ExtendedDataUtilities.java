package dk.itu.big_red.editors.assistants;

import org.eclipse.core.resources.IFile;

import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.assistants.Colour;
import dk.itu.big_red.model.assistants.RedProperty;
import dk.itu.big_red.model.changes.Change;

public final class ExtendedDataUtilities {
	private ExtendedDataUtilities() {}
	
	private static Object require(Object o, Class<?> klass) {
		return (klass.isInstance(o) ? o : null);
	}
	
	@RedProperty(fired = IFile.class, retrieved = IFile.class)
	public static final String FILE =
			"dk.itu.big_red.model.ModelObject.file";
	
	public static IFile getFile(ModelObject m) {
		return (IFile)require(m.getExtendedData(FILE), IFile.class);
	}
	
	public static void setFile(ModelObject m, IFile f) {
		m.setExtendedData(FILE, f);
	}
	
	@RedProperty(fired = String.class, retrieved = String.class)
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
	
	@RedProperty(fired = Colour.class, retrieved = Colour.class)
	public static final String FILL =
			"dk.itu.big_red.model.Colourable.fill";
	
	public static Colour getFill(ModelObject m) {
		Colour c = (Colour)require(m.getExtendedData(FILL), Colour.class);
		return (c != null ? c : new Colour("white"));
	}
	
	public static void setFill(ModelObject m, Colour c) {
		m.setExtendedData(FILL, c);
	}
	
	public static Change changeFill(ModelObject m, Colour c) {
		return m.changeExtendedData(FILL, c);
	}
	
	@RedProperty(fired = Colour.class, retrieved = Colour.class)
	public static final String OUTLINE =
			"dk.itu.big_red.model.Colourable.outline";
	
	public static Colour getOutline(ModelObject m) {
		Colour c = (Colour)require(m.getExtendedData(OUTLINE), Colour.class);
		return (c != null ? c :
			new Colour(m instanceof Link ? "green" : "black"));
	}
	
	public static void setOutline(ModelObject m, Colour c) {
		m.setExtendedData(OUTLINE, c);
	}
	
	public static Change changeOutline(ModelObject m, Colour c) {
		return m.changeExtendedData(OUTLINE, c);
	}
}
