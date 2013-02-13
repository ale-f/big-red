package dk.itu.big_red.editors.bigraph.parts.tree;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Layoutable;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.Request;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.gef.tools.SelectEditPartTracker;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.properties.IPropertySource;

import dk.itu.big_red.editors.bigraph.parts.AbstractPart;
import dk.itu.big_red.editors.bigraph.parts.IBigraphPart;
import dk.itu.big_red.editors.bigraph.parts.tree.TreePartFactory.Mode;
import dk.itu.big_red.utilities.ui.UI;

public abstract class AbstractTreePart extends AbstractTreeEditPart
		implements IBigraphPart, PropertyChangeListener {
	private IPropertySource propertySource;
	
	protected final IPropertySource getPropertySource() {
		return (propertySource != null ? propertySource :
			(propertySource = AbstractPart.createPropertySource(this)));
	}
	
	@Override
	public Layoutable getModel() {
		return (Layoutable)super.getModel();
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class key) {
		if (key == IPropertySource.class) {
			return getPropertySource();
		} else return super.getAdapter(key);
	}
	
	@Override
	public void activate() {
		super.activate();
		getModel().addPropertyChangeListener(this);
	}

	@Override
	public void deactivate() {
		getModel().removePropertyChangeListener(this);
		super.deactivate();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
    	refresh();
	}
	
	@Override
	public DragTracker getDragTracker(Request req) {
		return new SelectEditPartTracker(this);
	}
	
	@Override
	public void performRequest(Request req) {
		if (req.getType().equals(RequestConstants.REQ_OPEN)) {
			try {
				UI.getWorkbenchPage().showView(IPageLayout.ID_PROP_SHEET);
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected String getText() {
		return getModel().getName();
	}
	
	protected abstract ImageDescriptor getImageDescriptor();
	
	@Override
	protected Image getImage() {
		return (Image)getImageDescriptor().createResource(UI.getDisplay());
	}
	
	private Mode m = Mode.PLACE;
	
	protected void setMode(Mode m) {
		this.m = m;
	}
	
	protected Mode getMode() {
		return m;
	}
	
	protected Collection<? extends Layoutable> getPlaceChildren() {
		return Collections.emptyList();
	}
	
	protected Collection<? extends Layoutable> getLinkChildren() {
		return Collections.emptyList();
	}
	
	@Override
	protected final List<? extends Layoutable> getModelChildren() {
		switch (getMode()) {
		case PLACE:
			return new ArrayList<Layoutable>(getPlaceChildren());
		case LINK:
			return new ArrayList<Layoutable>(getLinkChildren());
		default:
			throw new RuntimeException("Oh no, extra enum value");
		}
	}
	
	@Override
	public Bigraph getBigraph() {
		return getModel().getBigraph();
	}
}
