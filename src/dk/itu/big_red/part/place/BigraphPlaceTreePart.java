package dk.itu.big_red.part.place;

import java.util.List;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.interfaces.ILayoutable;
import dk.itu.big_red.part.AbstractTreePart;


public class BigraphPlaceTreePart extends AbstractTreePart {
	@Override
	protected List<ILayoutable> getModelChildren() {
		return ((Bigraph)getModel()).getChildren();
	}

	@Override
	protected void createEditPolicies() {
		// TODO Auto-generated method stub
		
	}

}
