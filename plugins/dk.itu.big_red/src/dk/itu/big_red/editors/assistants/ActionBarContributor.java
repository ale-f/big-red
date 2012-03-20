package dk.itu.big_red.editors.assistants;

import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.EditorActionBarContributor;

import dk.itu.big_red.editors.AbstractEditor;

public class ActionBarContributor extends EditorActionBarContributor {
	@Override
	public void setActiveEditor(IEditorPart targetEditor) {
		if (!(targetEditor instanceof AbstractEditor))
			return;
		IActionBars bars = getActionBars();
		AbstractEditor editor = (AbstractEditor)targetEditor;
		ActionRegistry registry =
				(ActionRegistry)editor.getAdapter(ActionRegistry.class);
		for (String id : editor.getGlobalActionIDs())
			bars.setGlobalActionHandler(id, registry.getAction(id));
		bars.updateActionBars();
	}
}
