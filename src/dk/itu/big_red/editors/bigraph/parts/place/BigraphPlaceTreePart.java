package dk.itu.big_red.editors.bigraph.parts.place;

import java.util.List;

import dk.itu.big_red.editors.bigraph.parts.AbstractTreePart;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Layoutable;

public class BigraphPlaceTreePart extends AbstractTreePart {
	@Override
	protected List<Layoutable> getModelChildren() {
		return ((Bigraph)getModel()).getChildren();
	}

	@Override
	protected void createEditPolicies() {
		// TODO Auto-generated method stub
		
	}

}
