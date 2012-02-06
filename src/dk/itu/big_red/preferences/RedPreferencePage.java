package dk.itu.big_red.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.utilities.ui.UI;

public class RedPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {
	
	public RedPreferencePage() {
		super(FLAT);
		setPreferenceStore(RedPlugin.getInstance().getPreferenceStore());
		setDescription("Miscellaneous preferences for Big Red.");
	}
	
	@Override
	public void createFieldEditors() {
		addField(
			new ListEditor(
				RedPreferences.PREFERENCE_EXTERNAL_TOOLS,
				"User-defined external tools",
				getFieldEditorParent()) {
			@Override
			protected String[] parseString(String stringList) {
				return RedPreferences.splitString(stringList);
			}
			
			@Override
			protected String getNewInputObject() {
				return UI.promptFor("External command",
						"Define an external command.", null, null);
			}
			
			@Override
			protected String createList(String[] items) {
				return RedPreferences.joinString(items);
			}
		});
	}

	@Override
	public void init(IWorkbench workbench) {
		/* do nothing */
	}	
}