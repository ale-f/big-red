package dk.itu.big_red.commands;

import java.util.HashMap;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.ui.IEditorPart;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.interfaces.internal.ILayoutable;

public class ContainerRelayoutCommand extends Command {
	protected IEditorPart editor = null;
	protected Container model = null;
	
	private HashMap<ILayoutable, Rectangle> oldLayouts = new HashMap<ILayoutable, Rectangle>();
	
	@Override
	public boolean canExecute() {
		return (editor != null && model != null && oldLayouts.isEmpty());
	}
	
	@Override
	public boolean canUndo() {
		return !oldLayouts.isEmpty();
	}
	
	@Override
	public void undo() {
		if (canUndo()) {
			for (ILayoutable i : model.getChildren())
				i.setLayout(oldLayouts.get(i));
			model.setLayout(oldLayouts.get(model));
			oldLayouts.clear();
		}
	}
	
	@Override
	public void execute() {
		if (canExecute())
			redo();
	}

	@Override
	public void redo() {
		oldLayouts.put(model, model.getLayout());
		for (ILayoutable i : model.getChildren())
			oldLayouts.put(i, i.getLayout());
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
