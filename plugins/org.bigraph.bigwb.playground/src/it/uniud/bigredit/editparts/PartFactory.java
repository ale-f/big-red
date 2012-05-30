package it.uniud.bigredit.editparts;


import it.uniud.bigredit.model.BRS;
import it.uniud.bigredit.model.Reaction;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;



import dk.itu.big_red.editors.bigraph.parts.EdgePart;
import dk.itu.big_red.editors.bigraph.parts.InnerNamePart;
import dk.itu.big_red.editors.bigraph.parts.LinkConnectionPart;
import dk.itu.big_red.editors.bigraph.parts.LinkPart;
import dk.itu.big_red.editors.bigraph.parts.NodePart;
import dk.itu.big_red.editors.bigraph.parts.OuterNamePart;
import dk.itu.big_red.editors.bigraph.parts.PortPart;
import dk.itu.big_red.editors.bigraph.parts.RootPart;
import dk.itu.big_red.editors.bigraph.parts.SitePart;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;

/**
 * PartFactories produce {@link EditPart}s from bigraph model objects.
 * @author alec
 *
 */
public class PartFactory implements EditPartFactory {

	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		AbstractGraphicalEditPart part = null;
		
		if (model == null)
			return null;
		
		Class<?> target = model.getClass();
		
		
		if (target == Bigraph.class) {
			System.out.println("created new Bigraph");
			part = new NestedBigraphPart();
		} else if (target == Node.class) {
			part = new NodePart();
        } else if (target == Reaction.class) {
			part = new ReactionPart();
        }else if (target == Root.class) {
        	part = new RootPart();
        } else if (target == Site.class) {
    		part = new SitePart();
        } else if (target == LinkPart.Connection.class) {
        	part = new LinkConnectionPart();
        } else if (target == Edge.class) {
        	part = new EdgePart();
        } else if (target == InnerName.class) {
        	part = new InnerNamePart();
        } else if (target == OuterName.class) {
        	part = new OuterNamePart();
        } else if (target == Port.class) {
        	part = new PortPart();
        } else if (target == BRS.class) {
        	part = new BRSPart();
        }else if (target == ModelObject.class) {
        	System.out.println("created new MODELObject");
			
        	part = new NestedBigraphPart();
        }
	       
		if (part != null)
			part.setModel(model);
		
		return part;
	}
}