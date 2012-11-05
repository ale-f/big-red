package dk.itu.big_red.editors.bigraph.parts;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.Port;
import org.bigraph.model.Root;
import org.bigraph.model.Site;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;

import dk.itu.big_red.editors.utilities.ModelPropertySource;

/**
 * PartFactories produce {@link EditPart}s from bigraph model objects.
 * @author alec
 */
public class PartFactory implements EditPartFactory, IPropertySourceProvider {
	@Override
	public IPropertySource getPropertySource(Object o) {
		return (o instanceof Layoutable ?
				new ModelPropertySource((Layoutable)o) : null);
	}
	
	protected EditPart createEditPart(Class<?> klass) {
		if (klass == Bigraph.class) {
			return new BigraphPart();
		} else if (klass == Node.class) {
			return new NodePart();
		} else if (klass == Root.class) {
			return new RootPart();
		} else if (klass == Site.class) {
			return new SitePart();
		} else if (klass == LinkPart.Connection.class) {
			return new LinkConnectionPart();
		} else if (klass == Edge.class) {
			return new EdgePart();
		} else if (klass == InnerName.class) {
			return new InnerNamePart();
		} else if (klass == OuterName.class) {
			return new OuterNamePart();
		} else if (klass == Port.class) {
			return new PortPart();
		} else return null;
	}
	
	@Override
	public EditPart createEditPart(EditPart context, Object model) {
		if (model == null)
			return null;
	    
		EditPart part = createEditPart(model.getClass());
		if (part != null)
			part.setModel(model);
		
		return part;
	}
}