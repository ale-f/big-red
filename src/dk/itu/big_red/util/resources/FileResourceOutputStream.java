package dk.itu.big_red.util.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class FileResourceOutputStream extends ByteArrayOutputStream {
	public IFile file = null;
	
	public FileResourceOutputStream(IFile file) {
		this.file = file;
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		try {
    		file.setContents(new ByteArrayInputStream(toByteArray()), 0, null);
		} catch (CoreException e) {
			throw new IOException(e);
		}
	}
}
