package org.bigraph.model.resources;

import java.io.File;
import java.io.IOException;

public abstract class JavaResourceWrapper implements IResourceWrapper {
	private final File file;
	
	public JavaResourceWrapper(File file) {
		this.file = file;
	}
	
	public File getResource() {
		return file;
	}
	
	@Override
	public String getName() {
		return getResource().getName();
	}

	protected static String canonicalise(File f) throws IOException {
		return f.getCanonicalPath().replace(File.separatorChar, '/');
	}
	
	@Override
	public String getPath() {
		try {
			return canonicalise(getResource());
		} catch (IOException e) {
			return null;
		}
	}

	private static final String[] EMPTY = { "" };
	
	@Override
	public String getRelativePath(String relativeTo) {
		try {
			String[]
				me = getPath().split("/"),
				them = canonicalise(new File(relativeTo)).split("/");
			
			/* Non-Windows: handle the special case of the root */
			if (me.length == 0)
				me = EMPTY;
			if (them.length == 0)
				them = EMPTY;
			
			/* Windows: Cross-device relative paths are impossible */
			if ((me[0].indexOf(':') != -1 ||
					them[0].indexOf(':') != -1) &&
					!me[0].equals(them[0]))
				return null;
			
			String result = "";
			int i;
			for (i = 0; i < me.length && i < them.length; i++) {
				if (!me[i].equals(them[i])) {
					for (int j = i; j < them.length; j++)
						result += "../";
					break;
				}
			}
			
			int j = me.length - 1;
			while (i < j)
				result = result + me[i++] + "/";
			result += me[j];
			
			return result;
		} catch (IOException e) {
			return null;
		}
	}

	@Override
	public IContainerWrapper getParent() {
		return new JavaContainerWrapper(getResource().getParentFile());
	}
}
