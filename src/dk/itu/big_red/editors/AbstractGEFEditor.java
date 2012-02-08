package dk.itu.big_red.editors;

import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.commands.CommandStackEvent;
import org.eclipse.gef.commands.CommandStackEventListener;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.properties.IPropertySheetPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

import dk.itu.big_red.editors.bigraph.ChangePropertySheetEntry;
import dk.itu.big_red.editors.bigraph.actions.FileRevertAction;

public abstract class AbstractGEFEditor extends AbstractEditor
implements CommandStackEventListener {
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
	
	@Override
	public boolean isDirty() {
		return getCommandStack().isDirty();
	}
	
	protected abstract PaletteRoot getPaletteRoot();
	
	protected void createPaletteViewer(Composite parent) {
		PaletteViewer viewer = new PaletteViewer();
		setPaletteViewer(viewer);
		viewer.createControl(parent);
		getEditDomain().setPaletteViewer(getPaletteViewer());
	}
	
	private PaletteViewer paletteViewer;
	
	protected void setPaletteViewer(PaletteViewer paletteViewer) {
		this.paletteViewer = paletteViewer;
	}
    
	protected PaletteViewer getPaletteViewer() {
		return paletteViewer;
	}
	
	@Override
	public void stackChanged(CommandStackEvent event) {
		firePropertyChange(IEditorPart.PROP_DIRTY);
        updateActions(getStateActions());
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		getCommandStack().addCommandStackEventListener(this);
	}
	
	@Override
	public void dispose() {
		getCommandStack().removeCommandStackEventListener(this);
		super.dispose();
	}
	
	@Override
	public boolean canRedo() {
		return getCommandStack().canRedo();
	}
	
	@Override
	public void redo() {
		getCommandStack().redo();
	}
	
	@Override
	public boolean canUndo() {
		return getCommandStack().canUndo();
	}
	
	@Override
	public void undo() {
		getCommandStack().undo();
	}
	
	@Override
	protected void initializeActionRegistry() {
		super.initializeActionRegistry();
		setGlobalActionHandlers(registerActions(getStateActions(),
				new FileRevertAction(this)));
	}
	
	public void revert() {
		CommandStack cs = getCommandStack();
		while (cs.canUndo())
			cs.undo();
		cs.flush();
		
		firePropertyChange(PROP_DIRTY);
		updateActions(getStateActions());
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter == CommandStack.class) {
			return getCommandStack();
		} else if (adapter == IPropertySheetPage.class) {
    		PropertySheetPage psp = new PropertySheetPage();
    		psp.setRootEntry(new ChangePropertySheetEntry(getCommandStack()));
    		return psp;
    	} else return super.getAdapter(adapter);
	}
}
