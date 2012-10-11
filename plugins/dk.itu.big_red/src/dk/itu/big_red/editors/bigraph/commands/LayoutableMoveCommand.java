package dk.itu.big_red.editors.bigraph.commands;

import java.util.ArrayList;
import java.util.List;

import org.bigraph.model.Layoutable;
import org.bigraph.model.changes.ChangeGroup;
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
		ChangeGroup cg = new ChangeGroup();
		setChange(cg);
		for (Layoutable l : objects) {
			setTarget(l.getBigraph());
			Rectangle r = LayoutUtilities.getLayout(l);
			if (r != null) {
				r = r.getTranslated(moveDelta).resize(sizeDelta);
				if (r.width < 10)
					r.width = 10;
				if (r.height < 10)
					r.height = 10;
				cg.add(LayoutUtilities.changeLayout(l, r));
			} else {
				System.out.println("Oh no: " + l);
				cg.clear();
				return;
			}
		}
	}

}
