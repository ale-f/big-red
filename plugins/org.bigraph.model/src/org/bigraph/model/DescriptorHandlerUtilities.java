package org.bigraph.model;

import java.util.List;

import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

abstract class DescriptorHandlerUtilities {
	private DescriptorHandlerUtilities() {}
	
	static void checkAddBounds(
			IChangeDescriptor cd, List<?> l, int position)
			throws ChangeCreationException {
		if (position == -1)
			position = l.size();
		if (position < 0 || position > l.size())
			throw new ChangeCreationException(cd,
					"" + position + " is not a valid position");
	}
	
	static void checkRemove(
			IChangeDescriptor cd, List<?> l, Object o, int position)
			throws ChangeCreationException {
		checkRemoveBounds(cd, l, position);
		checkRemoveObject(cd, l, o, position);
	}
	
	static void checkRemoveBounds(
			IChangeDescriptor cd, List<?> l, int position)
			throws ChangeCreationException {
		if (position == -1)
			position = l.size() - 1;
		if (position < 0 || position >= l.size())
			throw new ChangeCreationException(cd,
					"" + position + " is not a valid position");
	}
	
	static void checkRemoveObject(
			IChangeDescriptor cd, List<?> l, Object o, int position)
			throws ChangeCreationException {
		if (position == -1)
			position = l.size() - 1;
		Object p = l.get(position);
		if (o != null ? !o.equals(p) : p != null)
			throw new ChangeCreationException(cd,
					"" + o + " is not at position " + position);
	}
}
