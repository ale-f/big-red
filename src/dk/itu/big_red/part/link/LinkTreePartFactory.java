package dk.itu.big_red.part.link;


import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Node;

public class LinkTreePartFactory implements EditPartFactory {

	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null;
		
		if (model instanceof Bigraph) {
			part = new BigraphLinkTreePart();
		} else if (model instanceof Node) {
			part = new NodeLinkTreePart();
		}
		
		if (part != null) {
			part.setModel(model);
		}
		
		return part;
	}

}