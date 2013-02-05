package dk.itu.big_red.editors.assistants;

import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.actions.RetargetAction;
import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.editors.AbstractGEFEditor;
import dk.itu.big_red.editors.actions.TogglePropertyAction;

public abstract class RedActions {
	public static final String ACTION_GUIDE = TogglePropertyAction.getId(
			AbstractGEFEditor.PROPERTY_DISPLAY_GUIDES);
	public static final String ACTION_EDGE = TogglePropertyAction.getId(
			AbstractGEFEditor.PROPERTY_DISPLAY_EDGES);

	private RedActions() {}
	
	public static RetargetAction createToggleGridAction() {
		return new RetargetAction(
				GEFActionConstants.TOGGLE_GRID_VISIBILITY,
				"Snap to grid", IAction.AS_CHECK_BOX) {
			{
				setImageDescriptor(RedPlugin.getImageDescriptor(
						"resources/icons/actions/snap-to-grid.png"));
			}
		};
	}
	
	public static RetargetAction createSnapNearAction() {
		return new RetargetAction(
				GEFActionConstants.TOGGLE_SNAP_TO_GEOMETRY,
				"Snap to nearby objects", IAction.AS_CHECK_BOX) {
			{
				setImageDescriptor(RedPlugin.getImageDescriptor(
						"resources/icons/actions/snap-to-object.png"));
			}
		};
	}
	
	public static RetargetAction createGuideDisplayAction() {
		return new RetargetAction(
				ACTION_GUIDE, "Toggle guide display", IAction.AS_CHECK_BOX) {
			{
				setImageDescriptor(RedPlugin.getImageDescriptor(
						"resources/icons/actions/guide-lines.png"));
			}
		};
	}
	
	public static RetargetAction createEdgeDisplayAction() {
		return new RetargetAction(
				ACTION_EDGE, "Toggle edge display", IAction.AS_CHECK_BOX) {
			{
				setImageDescriptor(RedPlugin.getImageDescriptor(
						"resources/icons/actions/edge-display.png"));
			}
		};
	}
}
