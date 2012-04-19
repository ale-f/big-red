package dk.itu.big_red.model.load_save;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.RegistryFactory;

import dk.itu.big_red.model.ModelObject;

/**
 * Classes extending Saver can write objects to an {@link OutputStream}.
 * @see Loader
 * @author alec
 */
public abstract class Saver {
	public static final String EXTENSION_POINT = "dk.itu.big_red.export";
	
	public class Option {
		private String id, description;
		
		public Option(String id, String description) {
			this.id = id;
			this.description = description;
		}
		
		public String getID() {
			return id;
		}
		
		public String getDescription() {
			return description;
		}
		
		public Object get() {
			return getOption(id);
		}
		
		public void set(Object o) {
			setOption(id, o);
		}
	};
	
	private ModelObject model = null;
	
	/**
	 * Returns the model object previously set with {@link #setModel(ModelObject)}.
	 * @return the model object
	 */
	public ModelObject getModel() {
		return model;
	}

	/**
	 * Sets the model object to be exported.
	 * @param model
	 * @return <code>this</code>, for convenience
	 */
	public Saver setModel(ModelObject model) {
		this.model = model;
		return this;
	}
	
	private OutputStream target = null;
	
	/**
	 * Returns the export's target {@link OutputStream}, if one has been set.
	 * @return an {@link OutputStream}
	 */
	public OutputStream getOutputStream() {
		return target;
	}
	
	/**
	 * Sets the target of the export to the given {@link OutputStream}. The
	 * OutputStream will be closed once the output has been written.
	 * @param os an OutputStream
	 * @return <code>this</code>, for convenience
	 */
	public Saver setOutputStream(OutputStream os) {
		if (os != null)
			target = os;
		return this;
	}
	
	/**
	 * Indicates whether or not the object is ready to be exported.
	 * @return <code>true</code> if the object is ready to be exported, or
	 *         <code>false</code> otherwise
	 */
	public boolean canExport() {
		return (model != null && target != null);
	}
	
	/**
	 * Exports the object. This function should not be called unless {@link
	 * Saver#canExport canExport} returns <code>true</code>.
	 * @throws SaveFailedException if the export failed
	 */
	public abstract void exportObject() throws SaveFailedException;
	
	private ArrayList<Option> options = new ArrayList<Option>();
	
	/**
	 * Adds an option to this {@link Saver}.
	 * @param d an {@link Option} specifying the new option
	 */
	protected final void addOption(Option d) {
		options.add(d);
	}
	
	/**
	 * @see #addOption(Option)
	 */
	protected final void addOption(String id, String description) {
		addOption(new Option(id, description));
	}
	
	/**
	 * Returns all of the options supported by this {@link Saver}.
	 * @return a list of {@link Option}s
	 */
	public final List<Option> getOptions() {
		return options;
	}
	
	/**
	 * Retrieves the value of the named option.
	 * <p>Subclasses should override this method.
	 * @param id the ID of one of this {@link Saver}'s options
	 * @return the current value of this option, or <code>null</code>
	 */
	protected Object getOption(String id) {
		return null;
	}
	
	/**
	 * Changes the value of the named option.
	 * <p>Subclasses should override this method.
	 * @param id the ID of one of this {@link Saver}'s options
	 * @param value the option's new value
	 */
	protected void setOption(String id, Object value) {
		return;
	}
	
	public static final Saver forContentType(String contentType) {
		for (IConfigurationElement ice :
			RegistryFactory.getRegistry().
				getConfigurationElementsFor(EXTENSION_POINT)) {
			if (contentType.equals(ice.getAttribute("contentType"))) {
				try {
					return (Saver)ice.createExecutableExtension("class");
				} catch (CoreException e) {
					return null;
				}
			}
		}
		return null;
	}
}
