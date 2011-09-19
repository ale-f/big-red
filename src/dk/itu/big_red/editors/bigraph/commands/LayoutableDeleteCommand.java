package dk.itu.big_red.editors.bigraph.commands;

import java.util.ArrayList;

import org.eclipse.gef.commands.Command;

import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.LayoutableModelObject;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Point;

public class LayoutableDeleteCommand extends Command {
	private LayoutableModelObject object = null;
	private Container parent = null;
	
	public void setObject(Object model) {
		if (model instanceof LayoutableModelObject) {
			this.object = (LayoutableModelObject)model;
			this.parent = this.object.getParent();
			if (this.parent == null)
				this.object = null;
		}
	}
	
	@Override
	public boolean canExecute() {
		return (object != null && parent != null);
	}
	
	private Link link = null;
	private ArrayList<Point> points = null;
	
	@Override
	public void execute() {
		if (object instanceof Link) {
			Link object = (Link)this.object;
			if (points == null)
				points = new ArrayList<Point>();
			points.addAll(object.getPoints());
			for (Point i : points)
				object.removePoint(i);
		} else if (object instanceof Point) {
			Point object = (Point)this.object;
			link = object.getLink();
			if (link != null)
				link.removePoint(object);
		}
		parent.removeChild(object);
	}
	
	@Override
	public void undo() {
		parent.addChild(object);
		if (object instanceof Link && points != null) {
			Link object = (Link)this.object;
			for (Point i : points)
				object.addPoint(i);
			points.clear();
		} else if (object instanceof Point && link != null) {
			link.addPoint((Point)this.object);
		}
	}
	
	@Override
	public void redo() {
		execute();
	}
}
