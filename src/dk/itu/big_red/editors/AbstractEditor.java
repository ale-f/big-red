package dk.itu.big_red.editors;

import java.util.List;

import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.EditorPart;
import dk.itu.big_red.model.ModelObject;

public abstract class AbstractEditor extends EditorPart {
	/**
	 * Registers a number of {@link IAction}s with the given {@link
	 * ActionRegistry}, optionally copying their IDs into a {@link List}.
	 * @param registry an {@link ActionRegistry}
	 * @param actionIDList a list to be filled with {@link String} IDs; can be
	 * <code>null</code>
	 * @param actions a number of {@link IAction}s
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static void registerActions(ActionRegistry registry,
		List actionIDList, IAction... actions) {
		for (IAction i : actions) {
			registry.registerAction(i);
			if (actionIDList != null)
				actionIDList.add(i.getId());
		}
	}

	private ActionRegistry actionRegistry;
	
	/**
	 * Returns this editor's {@link ActionRegistry}, creating and initialising
	 * it if necessary.
	 * @return a (possibly newly-initialised!) {@link ActionRegistry}
	 * @see #initializeActionRegistry()
	 */
	protected ActionRegistry getActionRegistry() {
		if (actionRegistry == null) {
			actionRegistry = new ActionRegistry();
			initializeActionRegistry();
		}
		return actionRegistry;
	}
	
	/**
	 * Calls {@link UpdateAction#update()} on the actions registered with the
	 * given IDs (if they <i>are</i> {@link UpdateAction}s, that is).
	 * @param actionIDs the list of IDs to update
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
	
	/**
	 * Initialises the {@link ActionRegistry}. There's no need to call this
	 * method explicitly; the first call to {@link #getActionRegistry()} will
	 * do so automatically.
	 * <p>Subclasses should override this method, but they should also call
	 * <code>super.initializeActionRegistry()</code> before doing anything
	 * else.
	 */
	protected void initializeActionRegistry() {
		createActions();
	}
	
	/**
	 * Creates actions for this editor and registers them with the {@link
	 * ActionRegistry}.
	 */
	protected abstract void createActions();
	
	@Override
	protected void setInputWithNotify(IEditorInput input) {
		super.setInputWithNotify(input);
		setPartName(input.getName());
	}
	
	protected abstract ModelObject getModel();
	
	@Override
	public void dispose() {
		getModel().dispose();
		super.dispose();
	}
}
