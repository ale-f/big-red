package org.bigraph.model.wrapper;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

public final class Activator extends Plugin {
	private static Activator instance;
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		instance = this;
		
		ChangeExtensions.init();
		SaverUtilities.init();
		LoaderUtilities.init();
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		LoaderUtilities.fini();
		SaverUtilities.fini();
		ChangeExtensions.fini();
		
		instance = null;
		super.stop(context);
	}
	
	public static Activator getInstance() {
		return instance;
	}
}
