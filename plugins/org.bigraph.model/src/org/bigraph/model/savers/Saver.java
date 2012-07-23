package org.bigraph.model.savers;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.ModelObject;

public abstract class Saver {
	public class Option {
		private final String id, name, description;
		
		protected Option(String id, String name) {
			this(id, name, null);
		}
		
		protected Option(String id, String name, String description) {
			this.id = id;
			this.name = name;
			this.description = description;
		}
		
		public String getID() {
			return id;
		}
		
		public String getName() {
			return name;
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
	protected final void addOption(String id, String name) {
		addOption(new Option(id, name));
	}
	
	/**
	 * @see #addOption(Option)
	 */
	protected final void addOption(String id, String name, String description) {
		addOption(new Option(id, name, description));
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
}
