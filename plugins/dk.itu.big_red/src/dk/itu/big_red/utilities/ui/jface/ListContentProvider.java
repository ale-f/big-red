package dk.itu.big_red.utilities.ui.jface;

import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ListContentProvider implements IStructuredContentProvider {
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		Assert.isTrue(newInput == null || newInput instanceof List<?>);
	}
	
	@Override
	public void dispose() {
	}
	
	@Override
	public Object[] getElements(Object inputElement) {
		return ((List<?>)inputElement).toArray();
	}
}
