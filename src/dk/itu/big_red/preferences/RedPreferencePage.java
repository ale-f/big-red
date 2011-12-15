package dk.itu.big_red.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import dk.itu.big_red.application.plugin.RedPlugin;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class RedPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public static final String PREFERENCE_BIGMC_PATH = "dk.itu.big_red.preferences.paths.bigmc";

	public RedPreferencePage() {
		super(GRID);
		setPreferenceStore(RedPlugin.getInstance().getPreferenceStore());
		setDescription("Miscellaneous preferences for Big Red.");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	@Override
	public void createFieldEditors() {
		addField(new StringFieldEditor(PREFERENCE_BIGMC_PATH,
				"Path to BigMC", getFieldEditorParent()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	}
	
}