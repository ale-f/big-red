package example.org.bigraph.extensions.scope;

import static org.bigraph.model.assistants.ExtendedDataUtilities.getProperty;
import org.bigraph.model.PortSpec;
import org.bigraph.model.assistants.ExtendedDataUtilities.ChangeExtendedDataDescriptor;
import org.bigraph.model.assistants.ExtendedDataUtilities.SimpleHandler;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

public abstract class Scope {
	private Scope() {}
	
	public static final String SCOPED = "eD!+ScopeExampleScoped";
	
	public static final class ChangeScopedDescriptor
			extends ChangeExtendedDataDescriptor<
					PortSpec.Identifier, Boolean> {
		static {
			DescriptorExecutorManager.getInstance().addParticipant(
					new SimpleHandler(ChangeScopedDescriptor.class));
		}
		
		public ChangeScopedDescriptor(PortSpec.Identifier identifier,
				boolean oldValue, boolean newValue) {
			super(SCOPED, identifier, oldValue, newValue);
		}
		
		public ChangeScopedDescriptor(PropertyScratchpad context,
				PortSpec mo, boolean newValue) {
			this(mo.getIdentifier(context), isScoped(context, mo), newValue);
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeScopedDescriptor(
					getTarget(), getNewValue(), getOldValue());
		}
	}
	
	public static boolean isScoped(PortSpec p) {
		return isScoped(null, p);
	}
	
	public static boolean isScoped(PropertyScratchpad context, PortSpec p) {
		Boolean b = getProperty(context, p, SCOPED, Boolean.class);
		return (b != null ? b : false);
	}
}
