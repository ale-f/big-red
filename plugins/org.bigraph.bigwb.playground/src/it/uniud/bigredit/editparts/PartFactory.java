package it.uniud.bigredit.editparts;


import it.uniud.bigredit.model.BRS;
import it.uniud.bigredit.model.Reaction;

import org.bigraph.model.Bigraph;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Site;
import org.eclipse.gef.EditPart;

/**
 * PartFactories produce {@link EditPart}s from bigraph model objects.
 * @author alec
 *
 */
public class PartFactory
		extends dk.itu.big_red.editors.bigraph.parts.PartFactory {
	@Override
	protected EditPart createEditPart(Class<?> target) {
		if (target == Bigraph.class) {
			//System.out.println("created new Bigraph");
			return new NestedBigraphPart();
		} else if (target == Reaction.class) {
        	return new ReactionPart();
        } else if (target == Site.class) {
        	return new SitePlusPart();
        } else if (target == BRS.class) {
        	return new BRSPart();
        }else if (target == ModelObject.class) {
        	//System.out.println("created new MODELObject");
			
        	return new NestedBigraphPart();
        } else return super.createEditPart(target);
	}
}