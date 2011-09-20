package dk.itu.big_red.import_export;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


/**
 * Classes extending Import can read objects from an {@link InputStream}.
 * 
 * <p>The existence of an Import class for a given format implies that a
 * corresponding {@link Export} class <i>should</i> exist for that format.
 * @see Export
 * @author alec
 *
 */

public abstract class Import<T> {
	protected InputStream source = null;
	
	/**
	 * Sets the source of the import to the given {@link InputStream}. The
	 * InputStream will be closed once the input has been read.
	 * @param is an InputStream
	 * @return <code>this</code>, for convenience
	 */
	public Import<T> setInputStream(InputStream is) {
		if (is != null)
			this.source = is;
		return this;
	}
	
	/**
	 * Sets the source of the import to the file specified.
	 * 
	 * <p>Equivalent to
	 * <code>setInputStream(new FileInputStream(path))</code>.
	 * @param path a path to a file
	 * @throws FileNotFoundException if
	 *         {@link FileInputStream#FileInputStream(String)} fails
	 * @return <code>this</code>, for convenience
	 */
	public Import<T> setInputFile(String path) throws FileNotFoundException {
		return setInputStream(new FileInputStream(path));
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
	 * Import#canImport canImport} returns <code>true</code>.
	 * @throws ImportFailedException if the import failed
	 */
	public abstract T importObject() throws ImportFailedException;
}