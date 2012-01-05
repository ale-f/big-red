package dk.itu.big_red.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import dk.itu.big_red.application.plugin.RedPlugin;

public class RedPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public static final String PREFERENCE_BIGMC_PATH =
			"dk.itu.big_red.preferences.paths.bigmc";

	public RedPreferencePage() {
		super(GRID);
		setPreferenceStore(RedPlugin.getInstance().getPreferenceStore());
		setDescription("Miscellaneous preferences for Big Red.");
	}
	
	@Override
	public void createFieldEditors() {
		addField(new StringFieldEditor(PREFERENCE_BIGMC_PATH,
				"Path to BigMC", getFieldEditorParent()));
	}

	@Override
	public void init(IWorkbench workbench) {
		/* do nothing */
	}	
}