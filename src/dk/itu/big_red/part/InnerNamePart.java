package dk.itu.big_red.part;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.Request;
import dk.itu.big_red.editpolicies.EdgeCreationPolicy;
import dk.itu.big_red.editpolicies.ILayoutableDeletePolicy;
import dk.itu.big_red.editpolicies.ILayoutableLayoutPolicy;
import dk.itu.big_red.figure.InnerNameFigure;
import dk.itu.big_red.figure.adornments.FixedPointAnchor;
import dk.itu.big_red.figure.adornments.FixedPointAnchor.Orientation;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.LinkConnection;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.interfaces.internal.ICommentable;
import dk.itu.big_red.model.interfaces.internal.IFillColourable;

/**
 * NameParts represent {@link InnerName}s, the model objects which define
 * (along with outer names) a bigraph's interface.
 * @see InnerName
 * @author alec
 *
 */
public class InnerNamePart extends AbstractPart implements NodeEditPart {
	@Override
	public InnerName getModel() {
		return (InnerName)super.getModel();
	}
	
	@Override
	protected IFigure createFigure() {
		return new InnerNameFigure();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new ILayoutableLayoutPolicy());
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new ILayoutableDeletePolicy());
		installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, new EdgeCreationPolicy());
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		if (evt.getPropertyName().equals(InnerName.PROPERTY_NAME) ||
			evt.getPropertyName().equals(ICommentable.PROPERTY_COMMENT) ||
			evt.getPropertyName().equals(IFillColourable.PROPERTY_FILL_COLOUR)) {
	    	refreshVisuals();
	    } else if (evt.getPropertyName().equals(Point.PROPERTY_LINK)) {
	    	refreshSourceConnections();
	    }
	}
	
	@Override
	protected void refreshVisuals(){
		super.refreshVisuals();
		
		InnerNameFigure figure = (InnerNameFigure)getFigure();
		InnerName model = getModel();
		
		figure.setName(model.getName());
		figure.setConstraint(model.getLayout());
		
		String toolTip = model.getName();
		Link l = model.getLink();
		if (l != null)
			toolTip += "\n(connected to " + l + ")";
		if (model.getComment() != null)
			toolTip += "\n\n" + model.getComment();
		figure.setToolTip(toolTip);
		
		figure.setBackgroundColor(l != null ?
				l.getOutlineColour() : Point.DEFAULT_COLOUR);
	}
	
	@Override
	protected List<LinkConnection> getModelSourceConnections() {
		ArrayList<LinkConnection> l = new ArrayList<LinkConnection>();
		Link link = getModel().getLink();
		if (link != null)
			l.add(link.getConnectionFor(getModel()));
        return l;
    }
	
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
		return new FixedPointAnchor(getFigure(), Orientation.NORTH);
    }
    
	@Override
	public ConnectionAnchor getSourceConnectionAnchor(Request request) {
		return new FixedPointAnchor(getFigure(), Orientation.NORTH);
    }
	
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
		return new FixedPointAnchor(getFigure(), Orientation.NORTH);
    }
    
	@Override
	public ConnectionAnchor getTargetConnectionAnchor(Request request) {
		return new FixedPointAnchor(getFigure(), Orientation.NORTH);
    }
}
