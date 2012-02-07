package dk.itu.big_red.editors;

import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.palette.PaletteRoot;

public abstract class AbstractGEFEditor extends AbstractEditor {
	private DefaultEditDomain editDomain;
	
	protected void setEditDomain(DefaultEditDomain editDomain) {
		this.editDomain = editDomain;
		getEditDomain().setPaletteRoot(getPaletteRoot());
	}
	
	protected DefaultEditDomain getEditDomain() {
		return editDomain;
	}
	
	public AbstractGEFEditor() {
		setEditDomain(new DefaultEditDomain(this));
	}
	
	public CommandStack getCommandStack() {
		return getEditDomain().getCommandStack();
	}
	
	protected abstract PaletteRoot getPaletteRoot();
	
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter == CommandStack.class) {
			return getCommandStack();
		} else return super.getAdapter(adapter);
	}
}
