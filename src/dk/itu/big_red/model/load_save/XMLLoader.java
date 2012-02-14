package dk.itu.big_red.model.load_save;

import org.eclipse.core.resources.IFile;
import org.w3c.dom.Element;

import dk.itu.big_red.utilities.resources.IFileBackable;

public abstract class XMLLoader extends Loader implements IFileBackable {
	private IFile file;
	
	@Override
	public IFile getFile() {
		return file;
	}

	@Override
	public XMLLoader setFile(IFile file) {
		this.file = file;
		return this;
	}
	
	public abstract Object makeObject(Element e) throws LoadFailedException;
}
