package org.bigraph.model.resources;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.bigraph.model.ModelObject;
import org.bigraph.model.loaders.BigraphXMLLoader;
import org.bigraph.model.loaders.LoadFailedException;
import org.bigraph.model.loaders.SignatureXMLLoader;

public class JavaFileWrapper extends JavaResourceWrapper
		implements IFileWrapper {
	public JavaFileWrapper(File file) {
		super(file);
	}

	private static final String fragment = ".bigraph";
	
	@Override
	public ModelObject load() throws LoadFailedException {
		String name = getResource().getName();
		int lf = name.lastIndexOf(fragment);
		if (lf != -1) {
			String extension = name.substring(lf + fragment.length());
			if (extension.equals("") || extension.equals("-agent")) {
				return new BigraphXMLLoader().setFile(this).
						setInputStream(getContents()).importObject();
			} else if (extension.equals("-signature")) {
				return new SignatureXMLLoader().setFile(this).
						setInputStream(getContents()).importObject();
			} else return null;
		}
		throw new LoadFailedException("No valid file extension found");
	}

	@Override
	public InputStream getContents() throws LoadFailedException {
		try {
			return new FileInputStream(getResource());
		} catch (FileNotFoundException e) {
			throw new LoadFailedException(e);
		}
	}

	@Override
	public InputStream open() {
		try {
			return getContents();
		} catch (LoadFailedException e) {
			return null;
		}
	}
}
