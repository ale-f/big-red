package dk.itu.big_red.utilities.ui.jface;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;

public abstract class ModelObjectTreeContentProvider extends
		ModelObjectContentProvider implements ITreeContentProvider {
	private final AbstractTreeViewer atv;
	
	public ModelObjectTreeContentProvider(AbstractTreeViewer atv) {
		this.atv = atv;
	}
	
	protected AbstractTreeViewer getViewer() {
		return atv;
	}
}
