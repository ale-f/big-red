package dk.itu.big_red.editors.bigraph.parts;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.Layoutable;
import org.bigraph.model.ModelObject;
import org.bigraph.model.OuterName;
import org.bigraph.model.Root;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.editors.bigraph.LayoutableLayoutPolicy;
import dk.itu.big_red.editors.bigraph.figures.BigraphFigure;

/**
 * BigraphParts represent {@link Bigraph}s, the top-level container of the
 * model.
 * @see Bigraph
 * @author alec
 *
 */
public class BigraphPart extends ContainerPart {
	@Override
	public Bigraph getModel() {
		return (Bigraph)super.getModel();
	}
	
	@Override
	public void activate() {
		super.activate();
		for (ModelObject i : getModel().getChildren())
			i.addPropertyChangeListener(this);
		refreshBoundaries();
	}
	
	@Override
	public void deactivate() {
		for (ModelObject i : getModel().getChildren())
			i.removePropertyChangeListener(this);
		super.deactivate();
	}
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		super.propertyChange(evt);
		String prop = evt.getPropertyName();
		if (evt.getSource() == getModel()) {
			if (prop.equals(Container.PROPERTY_CHILD)) {
				ModelObject
					oldValue = (ModelObject)evt.getOldValue(),
					newValue = (ModelObject)evt.getNewValue();
				if (oldValue == null && newValue != null) {
					newValue.addPropertyChangeListener(this);
				} else if (oldValue != null && newValue == null) {
					oldValue.removePropertyChangeListener(this);
				}
				refreshChildren();
				refreshBoundaries();
			}
		} else if (evt.getSource() instanceof Layoutable) {
			Layoutable l = (Layoutable)evt.getSource();
			if (l.getParent() == getModel()) {
				if (prop.equals(ExtendedDataUtilities.LAYOUT))
					refreshBoundaries();
			}
		}
	}
	
	@Override
	protected IFigure createFigure() {
		return new BigraphFigure();
	}

	@Override
	protected void createEditPolicies() {
		installEditPolicy(EditPolicy.LAYOUT_ROLE, new LayoutableLayoutPolicy());
	}
	
	@Override
	public List<Layoutable> getModelChildren() {
		List<Layoutable> nc = new ArrayList<Layoutable>();
		for (Layoutable i : getModel().getChildren()) {
			if (i instanceof Edge) {
				nc.add(i);
			} else nc.add(0, i);
		}
		return nc;
	}
	
	public static final int
		B_NONE = 0,
		B_LON = 1,
		B_UR = 1 << 1,
		B_LR = 1 << 2,
		B_R = B_UR | B_LR,
		B_UIN = 1 << 3;
	
	public boolean boundariesSatisfied(Rectangle r, Object l) {
		int bs = getBoundaryState(r);
		return !(
			(l instanceof Root && (bs & BigraphPart.B_R) != 0) ||
			(l instanceof OuterName && (bs & BigraphPart.B_LON) != 0) ||
			(l instanceof InnerName && (bs & BigraphPart.B_UIN) != 0));
	}
	
	public int getBoundaryState(Rectangle r) {
		int top = r.y(), bottom = r.bottom();
		return
			(top < upperRootBoundary ? B_UR : 0) |
			(bottom > lowerRootBoundary ? B_LR : 0) |
			(bottom > lowerOuterNameBoundary ? B_LON : 0) |
			(top < upperInnerNameBoundary ? B_UIN : 0);
	}
	
	private int upperRootBoundary = Integer.MIN_VALUE,
            lowerOuterNameBoundary = Integer.MAX_VALUE,
            upperInnerNameBoundary = Integer.MIN_VALUE,
            lowerRootBoundary = Integer.MAX_VALUE;
	
	public int getUpperRootBoundary() {
		return upperRootBoundary;
	}

	public int getLowerOuterNameBoundary() {
		return lowerOuterNameBoundary;
	}

	public int getUpperInnerNameBoundary() {
		return upperInnerNameBoundary;
	}

	public int getLowerRootBoundary() {
		return lowerRootBoundary;
	}

	protected void refreshBoundaries() {
		int oldUR = upperRootBoundary,
			    oldLON = lowerOuterNameBoundary,
			    oldUIN = upperInnerNameBoundary,
			    oldLR = lowerRootBoundary;
		upperRootBoundary = Integer.MIN_VALUE;
		lowerOuterNameBoundary = Integer.MAX_VALUE;
		upperInnerNameBoundary = Integer.MIN_VALUE;
		lowerRootBoundary = Integer.MAX_VALUE;
		
		for (Layoutable i : getModel().getChildren()) {
			if (i instanceof Edge)
				continue;
			Rectangle r = ExtendedDataUtilities.getLayout(i);
			int top = r.y(), bottom = r.y() + r.height();
			if (i instanceof OuterName) {
				if (bottom > upperRootBoundary)
					upperRootBoundary = bottom;
			} else if (i instanceof Root) {
				if (top < lowerOuterNameBoundary)
					lowerOuterNameBoundary = top;
				if (bottom > upperInnerNameBoundary)
					upperInnerNameBoundary = bottom;
			} else if (i instanceof InnerName) {
				if (top < lowerRootBoundary)
					lowerRootBoundary = top;
			}
		}
		
		if (oldUR != upperRootBoundary || oldLR != lowerRootBoundary ||
				oldLON != lowerOuterNameBoundary ||
				oldUIN != upperInnerNameBoundary) {
			BigraphFigure figure = (BigraphFigure)getFigure();
			
			figure.setUpperRootBoundary(upperRootBoundary);
			figure.setLowerOuterNameBoundary(lowerOuterNameBoundary);
			figure.setUpperInnerNameBoundary(upperInnerNameBoundary);
			figure.setLowerRootBoundary(lowerRootBoundary);
			
			figure.repaint();
		}
	}
	
	@Override
	public String getToolTip() {
		return "Bigraph " + getModel().getName();
	}
}
