package dk.itu.big_red.model.import_export;

import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.InputStream;

import dk.itu.big_red.exceptions.ImportFailedException;

public abstract class Import {
	InputStream source = null;
	
	/**
	 * Sets the source of the import to the given {@link InputStream}.
	 * @param os an InputStream
	 */
	void setInputStream(InputStream is) {
		if (is != null)
			this.source = is;
	}
	
	/**
	 * Sets the source of the import to the file specified.
	 * 
	 * <p>Equivalent to
	 * <code>setInputStream(new FileInputStream(path))</code>.
	 * @param path a path to a file
	 * @throws FileNotFoundException if
	 *         {@link FileInputStream#FileInputStream(String)} fails
	 */
	void setInputFile(String path) throws FileNotFoundException {
		setInputStream(new FileInputStream(path));
	}
	
	/**
	 * Indicates whether or not the model is ready to be imported.
	 * @return <code>true</code> if the model is ready to be imported, or
	 *         <code>false</code> otherwise
	 */
	abstract boolean canImport();
	
	/**
	 * Imports the model. This function should not be called unless {@link
	 * Import#canImport canImport} returns <code>true</code>.
	 * @throws ImportFailedException if the import failed
	 */
	abstract Object importModel() throws ImportFailedException;
}