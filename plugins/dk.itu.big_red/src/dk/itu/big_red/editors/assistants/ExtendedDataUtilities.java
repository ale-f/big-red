package dk.itu.big_red.editors.assistants;

import org.eclipse.core.resources.IFile;

import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.ModelObject.ChangeExtendedData;
import dk.itu.big_red.model.ModelObject.ExtendedDataValidator;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.assistants.Colour;
import dk.itu.big_red.model.assistants.IPropertyProviderProxy;
import dk.itu.big_red.model.assistants.RedProperty;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.names.policies.INamePolicy;

public final class ExtendedDataUtilities {
	private ExtendedDataUtilities() {}
	
	private static Object require(Object o, Class<?> klass) {
		return (klass.isInstance(o) ? o : null);
	}
	
	@RedProperty(fired = IFile.class, retrieved = IFile.class)
	public static final String FILE =
			"eD!+dk.itu.big_red.model.ModelObject.file";
	
	public static IFile getFile(ModelObject m) {
		return (IFile)require(m.getExtendedData(FILE), IFile.class);
	}
	
	public static void setFile(ModelObject m, IFile f) {
		m.setExtendedData(FILE, f);
	}
	
	@RedProperty(fired = String.class, retrieved = String.class)
	public static final String COMMENT =
			"eD!+dk.itu.big_red.model.ModelObject.comment";
	
	public static String getComment(ModelObject m) {
		String c = (String)require(m.getExtendedData(COMMENT), String.class);
		return (c != null ? c : "");
	}
	
	public static void setComment(ModelObject m, String s) {
		m.setExtendedData(COMMENT, (s.length() != 0 ? s : null));
	}
	
	public static Change changeComment(ModelObject m, String s) {
		return m.changeExtendedData(COMMENT, (s.length() != 0 ? s : null));
	}
	
	@RedProperty(fired = Colour.class, retrieved = Colour.class)
	public static final String FILL =
			"eD!+dk.itu.big_red.model.Colourable.fill";
	
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
			"eD!+dk.itu.big_red.model.Colourable.outline";
	
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
	
	@RedProperty(fired = INamePolicy.class, retrieved = INamePolicy.class)
	public static final String PARAMETER_POLICY =
			"eD!+dk.itu.big_red.model.Control.parameter-policy";
	
	public static INamePolicy getParameterPolicy(Control c) {
		return (INamePolicy)require(
				c.getExtendedData(PARAMETER_POLICY), INamePolicy.class);
	}
	
	public static void setParameterPolicy(Control c, INamePolicy n) {
		c.setExtendedData(PARAMETER_POLICY, n);
	}
	
	public static Change changeParameterPolicy(Control c, INamePolicy n) {
		return c.changeExtendedData(PARAMETER_POLICY, n);
	}
	
	private static final ExtendedDataValidator parameterValidator =
			new ExtendedDataValidator() {
		@Override
		public String validate(ChangeExtendedData c,
				IPropertyProviderProxy context) {
			Node n = (Node)require(c.getCreator(), Node.class);
			if (n == null)
				return c.getCreator() + " is not a Node";
			
			Control control = n.getControl();
			INamePolicy policy = getParameterPolicy(control);
			if (policy == null)
				return "The control " + control.getName() +
						" does not define a parameter";
			
			String value = (String)require(c.newValue, String.class);
			if ((c.newValue = policy.normalise(value)) == null)
				return "\"" + value + "\" is not a valid value for the " +
						"parameter of " + control.getName();
			return null;
		}
	};
	
	@RedProperty(fired = String.class, retrieved = String.class)
	public static final String PARAMETER =
			"eD!+dk.itu.big_red.model.Node.parameter";
	
	public static String getParameter(Node n) {
		String s = (String)require(n.getExtendedData(PARAMETER), String.class);
		if (s == null) {
			INamePolicy p = getParameterPolicy(n.getControl());
			if (p != null)
				s = p.get(0);
		}
		return s;
	}
	
	public static void setParameter(Node n, String s) {
		n.setExtendedData(PARAMETER, s);
	}
	
	public static Change changeParameter(Node n, String s) {
		return n.changeExtendedData(PARAMETER, s, parameterValidator);
	}
}
