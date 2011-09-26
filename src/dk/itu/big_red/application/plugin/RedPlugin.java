package dk.itu.big_red.application.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Random;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The RedPlugin class is responsible for starting and stopping the Big Red
 * plugin.
 */
public class RedPlugin extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "dk.itu.big_red";

	private static RedPlugin plugin;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static RedPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	/**
	 * Returns an {@link InputStream} for the given file in this plugin, if it
	 * exists.
	 * @param path a (plugin root-relative) path to a file
	 * @return an InputStream, or <code>null</code> if the file wasn't found
	 */
	public static InputStream getPluginResource(String path) {
		try {
			URL u = FileLocator.find(
					getDefault().getBundle(), new Path(path), null);
			if (u != null)
				return u.openStream();
			else return null;
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Returns a new {@link Status} object with a {@link Throwable} attached.
	 * <p>The severity of the new Status object is always {@link
	 * IStatus#ERROR}.
	 * @param t a {@link Throwable}
	 * @return a new {@link Status} object
	 */
	public static Status getThrowableStatus(Throwable t) {
		return new Status(IStatus.ERROR, PLUGIN_ID, t.getLocalizedMessage(), t);
	}
	
	private static Random r = null;
	public static Random getRandom() {
		if (r == null)
			r = new Random();
		return r;
	}
}
