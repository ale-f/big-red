package dk.itu.big_red.editors.bigraph.parts.tree;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;

public class TreePartFactory implements EditPartFactory {

	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		EditPart part = null; 
	    
		if (model instanceof Bigraph) {
			part = new BigraphTreePart();
		} else if (model instanceof Node) {
            part = new NodeTreePart();
        } else if (model instanceof Root) {
        	part = new RootTreePart();
        } else if (model instanceof Site) {
    		part = new SiteTreePart();
        } else if (model instanceof InnerName) {
        	part = new InnerNameTreePart();
        } else if (model instanceof Link) {
        	part = new LinkTreePart();
        }
	    
		if (part != null)
			part.setModel(model);
		
		return part;
	}

}
