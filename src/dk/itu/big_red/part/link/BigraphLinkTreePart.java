package dk.itu.big_red.part.link;

import java.util.ArrayList;
import java.util.List;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.part.AbstractTreePart;

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
