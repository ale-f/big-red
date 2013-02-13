package dk.itu.big_red.editors.utilities;

import java.util.EventObject;

import org.bigraph.model.assistants.IObjectIdentifier;
import org.bigraph.model.changes.descriptors.BoundDescriptor;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertySheetEntry;

import dk.itu.big_red.editors.bigraph.commands.ChangeCommand;
import dk.itu.big_red.editors.bigraph.parts.IBigraphPart;

public class RedPropertySheetEntry extends PropertySheetEntry {
	public RedPropertySheetEntry() {
		this(null);
	}
	
	private final CommandStack commandStack;
	private final CommandStackListener commandStackListener;
	public RedPropertySheetEntry(CommandStack commandStack) {
		this.commandStack = commandStack;
		commandStackListener = (commandStack != null ?
					new CommandStackListener() {
				@Override
				public void commandStackChanged(EventObject event) {
					refreshFromRoot();
				}
			} : null);
		if (commandStackListener != null)
			commandStack.addCommandStackListener(commandStackListener);
	}
	
	@Override
	public void dispose() {
		if (commandStackListener != null)
			commandStack.removeCommandStackListener(commandStackListener);
		super.dispose();
	}
	
	public CommandStack getCommandStack() {
		if (getParent() != null) {
			return getParent().getCommandStack();
		} else {
			if (commandStack == null)
				throw new RuntimeException("BUG: root with no command stack");
			return commandStack;
		}
	}
	
	@Override
	protected RedPropertySheetEntry getParent() {
		return (RedPropertySheetEntry)super.getParent();
	}
	
	@Override
	protected RedPropertySheetEntry createChildEntry() {
		return new RedPropertySheetEntry();
	}
	
	@Override
	protected IRedPropertySource getPropertySource(Object object) {
		IPropertySource r = super.getPropertySource(object);
		return (r instanceof IRedPropertySource ?
				(IRedPropertySource)r : null);
	}
	
	@Override
	public void resetPropertyValue() {
		if (getParent() == null)
			return;
		Object[] values = getParent().getValues();
		ChangeDescriptorGroup cg = new ChangeDescriptorGroup();
		IObjectIdentifier.Resolver ex = null;
		for (int i = 0; i < values.length; i++) {
			Object o = values[i];
			if (ex == null && o instanceof IBigraphPart)
				ex = ((IBigraphPart)o).getBigraph();
			IRedPropertySource rps = getPropertySource(values[i]);
			if (rps == null)
				continue;
			IChangeDescriptor rcd =
					rps.resetPropertyValueChange(getDescriptor().getId());
			if (rcd != null)
				cg.add(new BoundDescriptor(rps.getResolver(), rcd));
		}
		if (cg.size() > 0) {
			getCommandStack().execute(new ChangeCommand(cg, ex));
			refreshFromRoot();
		}
	}
	
	@Override
	protected void valueChanged(PropertySheetEntry child) {
		valueChanged(
				(RedPropertySheetEntry)child, new ChangeDescriptorGroup());
	}
	
	private void valueChanged(
			RedPropertySheetEntry child, ChangeDescriptorGroup cg) {
		Object[] values = getValues();
		IObjectIdentifier.Resolver ex = null;
		for (int i = 0; i < values.length; i++) {
			Object o = values[i];
			if (ex == null && o instanceof IBigraphPart)
				ex = ((IBigraphPart)o).getBigraph();
			IRedPropertySource rps = getPropertySource(values[i]);
			if (rps == null)
				continue;
			IChangeDescriptor rcd = rps.setPropertyValueChange(
					child.getDescriptor().getId(), child.getEditValue(i));
			if (rcd != null)
				cg.add(new BoundDescriptor(rps.getResolver(), rcd));
		}
		if (getParent() != null) {
			getParent().valueChanged(this, cg);
		} else getCommandStack().execute(new ChangeCommand(cg, ex));
	}
}
