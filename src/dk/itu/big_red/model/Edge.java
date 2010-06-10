package dk.itu.big_red.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.Connection;
import org.eclipse.ui.views.properties.IPropertySource;
import org.w3c.dom.Node;

import dk.itu.big_red.model.interfaces.IConnectable;
import dk.itu.big_red.model.interfaces.IPropertyChangeNotifier;
import dk.itu.big_red.model.interfaces.IXMLisable;
import dk.itu.big_red.propertysources.EdgePropertySource;

/**
 * An Edge is a connection which connects any number of {@link Port}s and
 * {@link Name}s. (An Edge which "connects" only one point is perfectly
 * legitimate.)
 * 
 * <p>Note that Edges represent the <i>bigraphical</i> concept of an edge
 * rather than a GEF/GMF {@link Connection}, and so they lack any concept of a
 * "source" or "target"; Ports and Names are always source nodes as far as the
 * underlying framework is concerned, and the target is always an {@link
 * EdgeTarget}.
 * @author alec
 *
 */
public class Edge implements IAdaptable, IPropertyChangeNotifier, IXMLisable {
	private PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	
	public static final String PROPERTY_COMMENT = "EdgeComment";
	
	private ArrayList<IConnectable> points = new ArrayList<IConnectable>();
	
	private IPropertySource propertySource = null;

	private String comment = null;
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			if (propertySource == null) {
				propertySource = new EdgePropertySource(this);
			}
			return propertySource;
		}
		return null;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		listeners.addPropertyChangeListener(listener);
	}
	
	public String getComment() {
		return this.comment;
	}
	
	public void setComment(String comment) {
		String oldComment = getComment();
		this.comment = comment;
		listeners.firePropertyChange(PROPERTY_COMMENT, oldComment, comment);
	}
	
	@Override
	public void fromXML(Node d) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public Node toXML(Node d) {
		// TODO Auto-generated method stub
		return null;
	}
}
