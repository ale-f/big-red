package org.bigraph.model.savers;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.ModelObject;
import org.bigraph.model.internal.utilities.FilteringIterable;
import org.bigraph.model.resources.IFileWrapper;

public abstract class Saver implements ISaver {
	private final ISaver parent;
	
	public Saver() {
		this(null);
	}
	
	public Saver(ISaver parent) {
		this.parent = parent;
		if (parent != null)
			for (InheritableParticipant p : getParticipants(
					parent, InheritableParticipant.class))
				addParticipant(p.newInstance());
	}
	
	@Override
	public ISaver getParent() {
		return parent;
	}
	
	private IFileWrapper file;
	
	public Saver setFile(IFileWrapper file) {
		this.file = file;
		return this;
	}
	
	@Override
	public IFileWrapper getFile() {
		IFileWrapper file = this.file;
		ISaver parent;
		if (file == null && (parent = getParent()) != null)
			file = parent.getFile();
		return file;
	}
	
	protected IFileWrapper getFileRaw() {
		return file;
	}
	
	public static abstract class SaverOption implements Option {
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
		
		@Override
		public Object getCookie() {
			return getClass();
		}
	}
	
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
	
	private List<Option> options = new ArrayList<Option>();
	
	/**
	 * Adds an option to this {@link Saver}.
	 * @param d an {@link Option} specifying the new option
	 */
	@Override
	public final void addOption(Option d) {
		options.add(d);
		copyOption(d, getParent());
	}
	
	/**
	 * Returns all of the options supported by this {@link Saver}.
	 * @return a list of {@link Option}s
	 */
	@Override
	public final List<Option> getOptions() {
		return options;
	}
	
	private List<Participant> participants = new ArrayList<Participant>();
	
	@Override
	public void addParticipant(Participant p) {
		participants.add(p);
		p.setSaver(this);
	}
	
	@Override
	public List<? extends Participant> getParticipants() {
		return participants;
	}
	
	protected <T extends Participant>
			Iterable<T> getParticipants(Class<T> klass) {
		return getParticipants(this, klass);
	}
	
	protected static <T extends Participant>
			Iterable<T> getParticipants(ISaver saver, Class<T> klass) {
		return new FilteringIterable<T>(klass, saver.getParticipants());
	}
	
	private static final void copyOption(Option option, ISaver parent) {
		if (option == null || parent == null)
			return;
		for (Option i : parent.getOptions()) {
			if (option.getCookie().equals(i.getCookie())) {
				option.set(i.get());
				return;
			}
		}
		copyOption(option, parent.getParent());
	}
}
