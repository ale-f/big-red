package dk.itu.big_red.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import dk.itu.big_red.application.plugin.RedPlugin;

/**
 * Class used to initialize default preference values.
 */
public class RedPreferences extends AbstractPreferenceInitializer {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = RedPlugin.getInstance().getPreferenceStore();
		store.setDefault(RedPreferencePage.PREFERENCE_BIGMC_PATH, "bigmc");
	}

}
