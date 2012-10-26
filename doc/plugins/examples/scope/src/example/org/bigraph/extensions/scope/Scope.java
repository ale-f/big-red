package example.org.bigraph.extensions.scope;

import static org.bigraph.model.assistants.ExtendedDataUtilities.getProperty;
import static org.bigraph.model.assistants.ExtendedDataUtilities.setProperty;

import org.bigraph.model.PortSpec;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.IChange;

public abstract class Scope {
	private Scope() {}
	
	public static final String SCOPED = "eD!+ScopeExampleScoped";
	
	public static void setScoped(PortSpec p, boolean scoped) {
		setScoped(null, p, scoped);
	}
	
	public static void setScoped(
			PropertyScratchpad context, PortSpec p, boolean scoped) {
		setProperty(context, p, SCOPED, scoped);
	}
	
	public static boolean isScoped(PortSpec p) {
		return isScoped(null, p);
	}
	
	public static boolean isScoped(PropertyScratchpad context, PortSpec p) {
		Boolean b = getProperty(context, p, SCOPED, Boolean.class);
		return (b != null ? b : false);
	}
	
	public static IChange changeScoped(PortSpec p, boolean scoped) {
		return p.changeExtendedData(SCOPED, scoped);
	}
}
