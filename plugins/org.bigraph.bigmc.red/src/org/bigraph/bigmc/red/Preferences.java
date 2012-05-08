package org.bigraph.bigmc.red;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.jface.preference.IPreferenceStore;

public class Preferences extends AbstractPreferenceInitializer {
	public static final String PREFERENCE_BIGMC_PATH =
			"org.bigraph.bigmc.red.preferences.path";

	@Override
	public void initializeDefaultPreferences() {
		DefaultScope.INSTANCE.getNode("org.bigraph.bigmc.red").
			put(PREFERENCE_BIGMC_PATH, "bigmc");
	}
	
	public static IPreferenceStore getStore() {
		return Activator.getDefault().getPreferenceStore();
	}
	
	protected static String getString(String id) {
		return getStore().getString(id);
	}

	public static String getBigMCPath() {
		return getString(PREFERENCE_BIGMC_PATH);
	}
}
