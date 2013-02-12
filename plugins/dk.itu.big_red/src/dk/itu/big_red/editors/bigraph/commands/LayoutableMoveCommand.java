package dk.itu.big_red.editors.bigraph.commands;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.Layoutable;
import org.bigraph.model.changes.descriptors.BoundDescriptor;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.LayoutUtilities;

public class LayoutableMoveCommand extends ChangeCommand {
	private Point moveDelta = new Point();
	private Dimension sizeDelta = new Dimension();
	private List<Layoutable> objects = new ArrayList<Layoutable>();
	
	public void setMoveDelta(Point moveDelta) {
		if (moveDelta != null)
			this.moveDelta = moveDelta;
	}
	
	public void setSizeDelta(Dimension sizeDelta) {
		if (sizeDelta != null)
			this.sizeDelta = sizeDelta;
	}
	
	public void addObject(Object o) {
		if (o instanceof Layoutable)
			objects.add((Layoutable)o);
	}
	
	@Override
	public void prepare() {
		ChangeDescriptorGroup cg = new ChangeDescriptorGroup();
		setChange(cg);
		for (Layoutable l : objects) {
			setContext(l.getBigraph());
			Rectangle r = LayoutUtilities.getLayout(l);
			if (r != null) {
				r = r.getTranslated(moveDelta).resize(sizeDelta);
				if (r.width < 10)
					r.width = 10;
				if (r.height < 10)
					r.height = 10;
				cg.add(new BoundDescriptor(l.getBigraph(),
						new LayoutUtilities.ChangeLayoutDescriptor(
								null, l, r)));
			} else {
				cg.clear();
				return;
			}
		}
	}
}
