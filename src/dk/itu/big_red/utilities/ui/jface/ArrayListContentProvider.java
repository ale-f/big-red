package dk.itu.big_red.utilities.ui.jface;

import java.util.ArrayList;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ArrayListContentProvider implements IStructuredContentProvider {
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		Assert.isTrue(
				newInput == null ||
				newInput instanceof ArrayList<?>);
	}
	
	@Override
	public void dispose() {
	}
	
	@Override
	public Object[] getElements(Object inputElement) {
		return ((ArrayList<?>)inputElement).toArray();
	}
}
