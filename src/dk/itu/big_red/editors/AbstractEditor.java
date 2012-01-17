package dk.itu.big_red.editors;

import java.util.List;

import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.part.EditorPart;

public abstract class AbstractEditor extends EditorPart {
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void registerActions(ActionRegistry registry,
		List actionIDList, IAction... actions) {
		for (IAction i : actions) {
			registry.registerAction(i);
			if (actionIDList != null)
				actionIDList.add(i.getId());
		}
	}

	private ActionRegistry actionRegistry = new ActionRegistry();
	
	protected ActionRegistry getActionRegistry() {
		return actionRegistry;
	}
	
	/**
	 * A convenience method for updating a set of actions defined by the given
	 * List of action IDs. The actions are found by looking up the ID in the
	 * {@link #getActionRegistry() action registry}. If the corresponding action
	 * is an {@link UpdateAction}, it will have its <code>update()</code> method
	 * called.
	 * 
	 * @param actionIds
	 *            the list of IDs to update
	 */
	protected void updateActions(List<String> actionIDs) {
		ActionRegistry registry = getActionRegistry();
		for (String i : actionIDs) {
			IAction action = registry.getAction(i);
			if (action instanceof UpdateAction)
				((UpdateAction)action).update();
		}
	}
	
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		if (adapter == ActionRegistry.class) {
			return getActionRegistry();
		} else return super.getAdapter(adapter);
	}
}
