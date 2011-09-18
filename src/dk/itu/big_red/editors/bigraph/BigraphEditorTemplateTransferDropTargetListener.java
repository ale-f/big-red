package dk.itu.big_red.editors.bigraph;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.requests.CreationFactory;

import dk.itu.big_red.model.assistants.ModelFactory;

/**
 * BigraphEditorTemplateTransferDropTargetListeners are responsible for
 * creating a {@link ModelFactory} when the user drags-and-drops a palette
 * entry onto the bigraph (so that an object of the appropriate type can be
 * created).
 * @author alec
 *
 */
public class BigraphEditorTemplateTransferDropTargetListener extends
		TemplateTransferDropTargetListener {

	public BigraphEditorTemplateTransferDropTargetListener(EditPartViewer viewer) {
		super(viewer);
	}
	
	@Override
	protected CreationFactory getFactory(Object template) {
		if (template instanceof Class<?>)
			return new ModelFactory((Class<?>)template);
		else return null;
	}

}
