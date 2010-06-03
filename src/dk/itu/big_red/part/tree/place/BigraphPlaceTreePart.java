package dk.itu.big_red.part.tree.place;

import java.util.List;

import dk.itu.big_red.model.Thing;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.part.tree.AbstractTreePart;


public class BigraphPlaceTreePart extends AbstractTreePart {
	@Override
	protected List<Thing> getModelChildren() {
		return ((Bigraph)getModel()).getChildrenArray();
	}

	@Override
	protected void createEditPolicies() {
		// TODO Auto-generated method stub
		
	}

}
