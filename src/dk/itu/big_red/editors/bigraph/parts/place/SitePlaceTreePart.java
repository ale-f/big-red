package dk.itu.big_red.editors.bigraph.parts.place;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.gef.EditPolicy;
import org.eclipse.jface.resource.ImageDescriptor;
import dk.itu.big_red.application.plugin.RedPlugin;
import dk.itu.big_red.editors.bigraph.LayoutableDeletePolicy;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Site;

public class SitePlaceTreePart extends AbstractTreePart {
	@Override
	protected List<Layoutable> getModelChildren() {
		return new ArrayList<Layoutable>();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.COMPONENT_ROLE, new LayoutableDeletePolicy());
	}
	
	@Override
	public String getText() {
		return ((Site)getModel()).getName();
	}
	
	@Override
	public ImageDescriptor getImageDescriptor() {
		return RedPlugin.getImageDescriptor(
				"resources/icons/bigraph-palette/site.png");
	}
}
