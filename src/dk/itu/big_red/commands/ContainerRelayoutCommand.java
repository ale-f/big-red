package dk.itu.big_red.commands;

import org.eclipse.gef.commands.Command;
import org.eclipse.ui.IEditorPart;

import dk.itu.big_red.model.Container;

public class ContainerRelayoutCommand extends Command {
	protected IEditorPart editor = null;
	protected Container model = null;
	
	@Override
	public boolean canExecute() {
		return (editor != null && model != null);
	}
	
	@Override
	public boolean canUndo() {
		return false;
	}
	
	public void execute() {
		if (canExecute())
			redo();
	}

	public void redo() {
		model.relayout();
	}
	
	public IEditorPart getEditor() {
		return editor;
	}

	public void setEditor(IEditorPart editor) {
		this.editor = editor;
	}

	public void setModel(Object model) {
		if (model instanceof Container)
			this.model = (Container)model;
	}

	public Container getModel() {
		return this.model;
	}
}
