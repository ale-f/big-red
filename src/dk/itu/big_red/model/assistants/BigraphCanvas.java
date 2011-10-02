package dk.itu.big_red.model.assistants;

import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.parts.GraphicalViewerImpl;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import dk.itu.big_red.editors.bigraph.parts.PartFactory;
import dk.itu.big_red.model.Bigraph;

/**
 * A {@link BigraphCanvas} is a {@link Canvas} to which a {@link
 * GraphicalViewer} has been connected, allowing it to display {@link
 * Bigraph}s.
 * @author alec
 *
 */
public class BigraphCanvas extends Canvas {
	private GraphicalViewerImpl gvi = null;
	
	public BigraphCanvas(Composite parent, int style) {
		super(parent, style);
		
		gvi = new GraphicalViewerImpl();
		gvi.setEditPartFactory(new PartFactory());
		gvi.setControl(this);
	}
	
	/**
	 * Changes the {@link Bigraph} being shown by this widget.
	 * @param b a {@link Bigraph}
	 */
	public void setContents(Bigraph b) {
		gvi.setContents(b);
	}
}
