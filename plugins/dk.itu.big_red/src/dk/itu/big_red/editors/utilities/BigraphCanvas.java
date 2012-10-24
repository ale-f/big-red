package dk.itu.big_red.editors.utilities;

import org.bigraph.model.Bigraph;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.editparts.ScalableRootEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.parts.GraphicalViewerImpl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import dk.itu.big_red.editors.bigraph.parts.PartFactory;

/**
 * A {@link BigraphCanvas} is a {@link Canvas} to which a {@link
 * GraphicalViewer} has been connected, allowing it to display {@link
 * Bigraph}s.
 * @author alec
 */
public class BigraphCanvas extends Canvas {
	private GraphicalViewerImpl gvi = null;
	private ScalableRootEditPart rep = null;
	private ZoomManager zm = null;
	private Dimension preferredSize = null;

	public static final double zoomBounds[] = {
		0.05,
		20
	};

	public BigraphCanvas(Composite parent, int style) {
		super(parent, style);

		gvi = new GraphicalViewerImpl();
		gvi.setEditPartFactory(new PartFactory());
		gvi.setControl(this);
		gvi.setRootEditPart(rep = new ScalableRootEditPart());
		zm = rep.getZoomManager();
		zm.setZoomLevels(zoomBounds);

		addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event event) {
				fit();
			}
		});
	}

	/**
	 * Changes the {@link Bigraph} being shown by this widget.
	 * @param b a {@link Bigraph}
	 */
	 public void setContents(Bigraph b) {
		 gvi.setContents(b);

		 zm.setZoom(1);
		 IFigure f = rep.getFigure();
		 f.validate();
		 preferredSize = f.getLayoutManager().getPreferredSize(f, -1, -1);
		 if (preferredSize != null)
			 preferredSize.expand(25, 25); /* Bigraph.PADDING */

		 fit();
	 }

	 public Bigraph getContents() {
		 return (Bigraph)gvi.getContents().getModel();
	 }
	 
	 private void fit() {
		 getParent().layout();

		 Point availableSize = getSize();
		 if (availableSize == null || preferredSize == null)
			 return;

		 double widthRatio = (double)preferredSize.width / availableSize.x,
				 heightRatio = (double)preferredSize.height / availableSize.y;

		 zm.setZoom(1 / Math.max(widthRatio, heightRatio));
	 }
}