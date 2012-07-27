package dk.itu.big_red.application.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.bigraph.model.loaders.XMLLoader;
import org.bigraph.model.resources.IOpenable;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import dk.itu.big_red.model.load_save.IRedNamespaceConstants;

/**
 * The RedPlugin class is responsible for starting and stopping the Big Red
 * plugin &mdash; and for keeping track of plugin-wide shared objects.
 */
public class RedPlugin extends AbstractUIPlugin {
	private static final class BundleOpenable implements IOpenable {
		private final String path;
		
		public BundleOpenable(String path) {
			this.path = path;
		}
		
		@Override
		public InputStream open() {
			try {
				URL u = FileLocator.find(
						getInstance().getBundle(), new Path(path), null);
				if (u != null)
					return u.openStream();
				else return null;
			} catch (IOException e) {
				return null;
			}
		}
	}
	
	public static final String PLUGIN_ID = "dk.itu.big_red";
	private static RedPlugin plugin;
	
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		XMLLoader.registerSchema(IRedNamespaceConstants.SIGNATURE,
				new BundleOpenable("resources/schema/signature.xsd"));
		XMLLoader.registerSchema(IRedNamespaceConstants.BIGRAPH,
				new BundleOpenable("resources/schema/bigraph.xsd"));
		XMLLoader.registerSchema(IRedNamespaceConstants.RULE,
				new BundleOpenable("resources/schema/rule.xsd"));
		XMLLoader.registerSchema(IRedNamespaceConstants.SPEC,
				new BundleOpenable("resources/schema/spec.xsd"));
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		XMLLoader.unregisterSchema(IRedNamespaceConstants.SPEC);
		XMLLoader.unregisterSchema(IRedNamespaceConstants.RULE);
		XMLLoader.unregisterSchema(IRedNamespaceConstants.BIGRAPH);
		XMLLoader.unregisterSchema(IRedNamespaceConstants.SIGNATURE);
		
		plugin = null;
		super.stop(context);
	}
	
	/**
	 * Returns the shared instance
	 * @return the shared instance
	 */
	public static RedPlugin getInstance() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
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
	public static InputStream getResource(String path) {
		return new BundleOpenable(path).open();
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
}
