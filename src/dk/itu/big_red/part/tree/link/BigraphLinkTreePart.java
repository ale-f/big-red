package dk.itu.big_red.part.tree.link;

import java.util.List;

import dk.itu.big_red.model.Thing;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.part.tree.AbstractTreePart;


public class BigraphLinkTreePart extends AbstractTreePart {
	@Override
	protected List<Thing> getModelChildren() {
		/*
		 * FIXME: notifications are only fired for *children* of the Bigraph
		 * (i.e., Roots), not further descendants.
		 */
		return ((Bigraph)getModel()).findAllChildren(Node.class);
	}
}
