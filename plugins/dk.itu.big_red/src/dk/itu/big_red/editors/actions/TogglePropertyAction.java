package dk.itu.big_red.editors.actions;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.jface.action.Action;

public class TogglePropertyAction extends Action {
	public static String getId(String property) {
		return "TPA!" + property;
	}
	
	private String property;
	private boolean defaultValue;
	private GraphicalViewer viewer;
	
	public TogglePropertyAction(
			String property, boolean defaultValue, GraphicalViewer viewer) {
		super(property, AS_CHECK_BOX);
		this.viewer = viewer;
		this.defaultValue = defaultValue;
		this.property = property;
		
		setId(getId(property));
		setChecked(isChecked());
	}
	
	@Override
	public boolean isChecked() {
		Object o = viewer.getProperty(property);
		return (o instanceof Boolean ? (Boolean)o : defaultValue);
	}
	
	@Override
	public void run() {
		viewer.setProperty(property, Boolean.valueOf(!isChecked()));
	}
}
