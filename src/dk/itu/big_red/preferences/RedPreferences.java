package dk.itu.big_red.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import dk.itu.big_red.application.plugin.RedPlugin;

public class RedPreferences extends AbstractPreferenceInitializer {
	public static final String PREFERENCE_EXTERNAL_TOOLS =
			"dk.itu.big_red.preferences.externalTools";

	@Override
	public void initializeDefaultPreferences() {
	}
	
	protected static IPreferenceStore getStore() {
		return RedPlugin.getInstance().getPreferenceStore();
	}
	
	protected static String getString(String id) {
		return getStore().getString(id);
	}

	public static String[] getExternalTools() {
		String s = getString(RedPreferences.PREFERENCE_EXTERNAL_TOOLS);
		if (s == null || s.length() == 0)
			return new String[0];
		return s.split(":");
	}
}
