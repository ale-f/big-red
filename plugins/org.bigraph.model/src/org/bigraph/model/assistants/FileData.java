package org.bigraph.model.assistants;

import org.bigraph.model.ModelObject;
import org.bigraph.model.resources.IFileWrapper;

public abstract class FileData {
	@RedProperty(fired = IFileWrapper.class, retrieved = IFileWrapper.class)
	public static final String FILE =
			"eD!+dk.itu.big_red.model.ModelObject.file";

	private FileData() {}

	public static IFileWrapper getFile(ModelObject m) {
		return getFile(null, m);
	}

	public static IFileWrapper getFile(
			PropertyScratchpad context, ModelObject m) {
		Object file = (m != null ? m.getExtendedData(context, FILE) : null);
		return (file instanceof IFileWrapper ? (IFileWrapper)file : null);
	}

	public static void setFile(ModelObject m, IFileWrapper f) {
		m.setExtendedData(FILE, f);
	}
}
