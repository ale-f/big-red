package dk.itu.big_red.model.import_export;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import dk.itu.big_red.exceptions.ExportFailedException;

/**
 * Classes extending Export can write a model object to an {@link
 * OutputStream}. (The export process can do anything it wants - one class
 * might export a {@link Bigraph} to a PNG image, and another might export a
 * {@link Signature} to a XML document.)
 * 
 * <p>The existence of an Export class for a given format does <i>not</i> imply
 * that a {@link Import} class should exist for that format - in most cases,
 * that'd be impossible (try importing a bigraph from a PNG!).
 * @see Import
 * @author alec
 *
 */
public abstract class Export<T> {
	protected T model = null;
	
	/**
	 * Sets the model object to be exported.
	 * @param model
	 */
	public void setModel(T model) {
		this.model = model;
	}
	
	protected OutputStream target = null;
	
	/**
	 * Sets the target of the export to the given {@link OutputStream}. The
	 * OutputStream will be closed once the output has been written.
	 * @param os an OutputStream
	 */
	public void setOutputStream(OutputStream os) {
		if (os != null)
			this.target = os;
	}
	
	/**
	 * Sets the target of the export to the file specified.
	 * 
	 * <p>Equivalent to
	 * <code>setOutputStream(new FileOutputStream(path))</code>.
	 * @param path a path to a file
	 * @throws FileNotFoundException if
	 *         {@link FileOutputStream#FileOutputStream(String)} fails
	 */
	public void setOutputFile(String path) throws FileNotFoundException {
		setOutputStream(new FileOutputStream(path));
	}
	
	/**
	 * Indicates whether or not the model is ready to be exported.
	 * @return <code>true</code> if the model is ready to be exported, or
	 *         <code>false</code> otherwise
	 */
	public boolean canExport() {
		return (this.model != null && this.target != null);
	}
	
	/**
	 * Exports the model. This function should not be called unless {@link
	 * Export#canExport canExport} returns <code>true</code>.
	 * @throws ExportFailedException if the export failed
	 */
	public abstract void exportModel() throws ExportFailedException;
}
