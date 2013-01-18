package org.bigraph.model.loaders;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.ModelObject;
import org.bigraph.model.process.AbstractParticipantHost;
import org.bigraph.model.process.ParticipantManager;
import org.bigraph.model.resources.IFileWrapper;

public abstract class Loader
		extends AbstractParticipantHost implements ILoader {
	private static final class Holder {
		private static final ParticipantManager MANAGER =
				new ParticipantManager();
	}
	
	public static ParticipantManager getParticipantManager() {
		return Holder.MANAGER;
	}
	
	private final Loader parent;
	
	public Loader() {
		this(null);
	}
	
	public Loader(Loader parent) {
		this.parent = parent;
		getParticipantManager().addParticipants(this);
	}
	
	@Override
	public Loader getParent() {
		return parent;
	}
	
	protected void cycleCheck() throws LoadFailedException {
		Loader parent = getParent();
		IFileWrapper mf = getFileRaw();
		if (parent == null || mf == null)
			return;
		while (parent != null) {
			IFileWrapper tf = parent.getFileRaw();
			if (mf.equals(tf))
				throw new LoadFailedException("Cycle detected: " + mf);
			parent = parent.getParent();
		}
	}
	
	private InputStream source = null;
	
	/**
	 * Sets the source of the import to the given {@link InputStream}. The
	 * InputStream will be closed once the input has been read.
	 * @param is an InputStream
	 * @return <code>this</code>, for convenience
	 */
	public Loader setInputStream(InputStream is) {
		source = is;
		return this;
	}
	
	protected InputStream getInputStream() {
		return source;
	}
	
	private IFileWrapper file;
	
	public Loader setFile(IFileWrapper file) {
		this.file = file;
		return this;
	}
	
	@Override
	public IFileWrapper getFile() {
		IFileWrapper file = this.file;
		Loader parent;
		if (file == null && (parent = getParent()) != null)
			file = parent.getFile();
		return file;
	}
	
	protected IFileWrapper getFileRaw() {
		return file;
	}
	
	/**
	 * Indicates whether or not the model is ready to be imported.
	 * @return <code>true</code> if the model is ready to be imported, or
	 *         <code>false</code> otherwise
	 */
	public boolean canImport() {
		return (source != null);
	}
	
	/**
	 * Imports the object. This function should not be called unless {@link
	 * Loader#canImport canImport} returns <code>true</code>.
	 * @throws LoadFailedException if the import failed
	 */
	public abstract ModelObject importObject() throws LoadFailedException;
	
	private ArrayList<LoaderNotice> notices;
	
	@Override
	public void addNotice(LoaderNotice status) {
		if (notices == null)
			notices = new ArrayList<LoaderNotice>();
		notices.add(status);
	}
	
	@Override
	public void addNotice(LoaderNotice.Type type, String message) {
		addNotice(new LoaderNotice(type, message));
	}
	
	public List<LoaderNotice> getNotices() {
		return notices;
	}
}
