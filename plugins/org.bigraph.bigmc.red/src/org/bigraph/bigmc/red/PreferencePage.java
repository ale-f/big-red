package org.bigraph.bigmc.red;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class PreferencePage
	extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public PreferencePage() {
		super(FLAT);
		setDescription(null);
		setPreferenceStore(Preferences.getStore());
	}

	@Override
	protected void createFieldEditors() {
		addField(new FileFieldEditor(
			Preferences.PREFERENCE_BIGMC_PATH, "Path to BigMC",
				getFieldEditorParent()) {
			@Override
			protected boolean checkState() {
				/* Accept anything */
				return true;
			}
		});
	}

	@Override
	public void init(IWorkbench workbench) {
		/* do nothing */
	}
}
