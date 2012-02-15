package dk.itu.big_red.editors.bigraph.parts.link;

import java.util.ArrayList;
import java.util.List;

import dk.itu.big_red.editors.bigraph.parts.place.AbstractTreePart;
import dk.itu.big_red.model.Container;

public class BigraphLinkTreePart extends AbstractTreePart {
	@Override
	protected List<Container> getModelChildren() {
		/*
		 * FIXME: notifications are only fired for *children* of the Bigraph
		 * (i.e., Roots), not further descendants.
		 */
		return new ArrayList<Container>();
	}
}
