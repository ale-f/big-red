package org.bigraph.model.wrapper;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public final class Activator extends Plugin {
	private static Activator instance;
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		
		install();
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		uninstall();
		
		instance = null;
		super.stop(context);
	}
	
	public static Activator getInstance() {
		return instance;
	}

	public static final String EXTENSION_POINT_CHANGES =
			"org.bigraph.model.wrapper.changes";
	
	private static void install() {
		ChangeExtensions.init();
		SaverUtilities.init();
		LoaderUtilities.init();
	}
	
	private static void uninstall() {
		LoaderUtilities.fini();
		SaverUtilities.fini();
		ChangeExtensions.fini();
	}
}
