package org.bigraph.model.loaders;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.ModelObject;

public abstract class Loader implements ILoader {
	private InputStream source = null;
	
	/**
	 * Sets the source of the import to the given {@link InputStream}. The
	 * InputStream will be closed once the input has been read.
	 * @param is an InputStream
	 * @return <code>this</code>, for convenience
	 */
	public Loader setInputStream(InputStream is) {
		if (is != null)
			source = is;
		return this;
	}
	
	protected InputStream getInputStream() {
		return source;
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
		System.out.println(this + ".addNotice(" + status + ")");
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
