package dk.itu.big_red.editors.bigraph.parts.tree;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Link;
import org.bigraph.model.Node;
import org.bigraph.model.Point;
import org.bigraph.model.Root;
import org.bigraph.model.Site;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;

public class TreePartFactory implements EditPartFactory {
	public enum Mode {
		PLACE,
		LINK
	}
	
	private Mode m;
	
	public TreePartFactory(Mode m) {
		this.m = m;
	}
	
	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		AbstractTreePart part = null; 
	    
		if (model instanceof Bigraph) {
			part = new BigraphTreePart();
		} else if (model instanceof Node) {
            part = new NodeTreePart();
        } else if (model instanceof Root) {
        	part = new RootTreePart();
        } else if (model instanceof Site) {
    		part = new SiteTreePart();
        } else if (model instanceof Point) {
        	part = new PointTreePart();
        } else if (model instanceof Link) {
        	part = new LinkTreePart();
        }
	    
		if (part != null) {
			part.setMode(m);
			part.setModel(model);
		}
		
		return part;
	}
}
