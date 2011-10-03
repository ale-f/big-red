package dk.itu.big_red.editors.bigraph.commands;

import java.util.HashMap;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeAddChild;
import dk.itu.big_red.model.changes.bigraph.BigraphChangeName;
import dk.itu.big_red.util.geometry.Rectangle;

public class LayoutableCreateCommand extends ChangeCommand {
	ChangeGroup cg = new ChangeGroup();
	
	public LayoutableCreateCommand() {
		setChange(cg);
	}
	
	private Rectangle layout = null;
	private Container container = null;
	private Layoutable node = null;
	
	private final static String _IAS_ALPHA = "0123456789abcdefghijklmnopqrstuvwxyz";

	private static String intAsString(int x) {
		String s = "";
		boolean nonZeroEncountered = false;
		for (int i = 5; i >= 0; i--) {
			int y = (int)Math.pow(36, i);
			int z = x / y;

			if (z == 0 && !nonZeroEncountered && i != 0)
				continue;

			nonZeroEncountered = true;
			s += _IAS_ALPHA.charAt(z);

			x -= y * z;
		}
		return s;
	}
	
	@Override
	public void prepare() {
		cg.clear();
		if (layout == null || container == null || node == null)
			return;
		setTarget(container.getBigraph());
		for (Layoutable i : container.getChildren()) {
			if (i instanceof Edge)
				continue;
			else if (i.getLayout().intersects(layout))
				return;
		}
		if (container instanceof Bigraph) {
			Bigraph bigraph = (Bigraph)container;
			int top = layout.getY(),
			    bottom = layout.getY() + layout.getHeight();
			if (node instanceof OuterName) {
				if (bottom > bigraph.getLowerOuterNameBoundary())
					return;
			} else if (node instanceof Root) {
				if (top < bigraph.getUpperRootBoundary() ||
						bottom > bigraph.getLowerRootBoundary())
					return;
			} else if (node instanceof InnerName) {
				if (top < bigraph.getUpperInnerNameBoundary())
					return;
			}
		}
		
		HashMap<Layoutable, String> ns =
			container.getBigraph().getNamespace(Bigraph.getNSI(node));
		int i = 0;
		String name = null;
		do {
			name = intAsString(i++);
		} while (ns.containsValue(name));
		
		cg.add(new BigraphChangeAddChild(container, node, layout),
				new BigraphChangeName(node, name));
	}
	
	public void setObject(Object s) {
		if (s instanceof Layoutable)
			node = (Layoutable)s;
	}
	
	public void setContainer(Object e) {
		if (e instanceof Container)
			container = (Container)e;
	}
	
	public void setLayout(Object r) {
		if (r instanceof Rectangle)
			layout = (Rectangle)r;
		else if (r instanceof org.eclipse.draw2d.geometry.Rectangle)
			layout = new Rectangle((org.eclipse.draw2d.geometry.Rectangle)r);
	}
}
