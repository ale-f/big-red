package org.bigraph.model.savers;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.ModelObject;
import org.bigraph.model.resources.IFileWrapper;

public abstract class Saver implements ISaver {
	private IFileWrapper file;
	
	public Saver setFile(IFileWrapper file) {
		this.file = file;
		return this;
	}
	
	public IFileWrapper getFile() {
		return file;
	}
	
	public interface ISaverOption {
		String getName();
		String getDescription();
		
		Object get();
		void set(Object value);
	}
	
	public static abstract class SaverOption implements ISaverOption {
		private final String name, description;
		
		public SaverOption(String name) {
			this(name, null);
		}
		
		public SaverOption(String name, String description) {
			this.name = name;
			this.description = description;
		}
		
		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public String getDescription() {
			return description;
		}
	}
	
	protected class Option extends SaverOption {
		private final String id;
		
		public Option(String id, String name) {
			this(id, name, null);
		}
		
		public Option(String id, String name, String description) {
			super(name, description);
			this.id = id;
		}
		
		public String getID() {
			return id;
		}
		
		@Override
		public Object get() {
			return getOption(id);
		}
		
		@Override
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
	
	private ArrayList<ISaverOption> options = new ArrayList<ISaverOption>();
	
	/**
	 * Adds an option to this {@link Saver}.
	 * @param d an {@link Option} specifying the new option
	 */
	@Override
	public final void addOption(ISaverOption d) {
		options.add(d);
	}
	
	/**
	 * Returns all of the options supported by this {@link Saver}.
	 * @return a list of {@link Option}s
	 */
	@Override
	public final List<? extends ISaverOption> getOptions() {
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
