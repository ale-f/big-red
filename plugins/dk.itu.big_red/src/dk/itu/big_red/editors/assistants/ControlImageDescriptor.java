package dk.itu.big_red.editors.assistants;

import org.bigraph.model.Control;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

import dk.itu.big_red.model.ColourUtilities;
import dk.itu.big_red.model.ControlUtilities;
import dk.itu.big_red.model.Ellipse;
import dk.itu.big_red.model.LayoutUtilities;
import dk.itu.big_red.utilities.ui.ColorWrapper;
import dk.itu.big_red.utilities.ui.UI;

/**
 * The <strong>ControlImageDescriptor</strong> class can be used to return a
 * thumbnail for a {@link Control}.
 * @author alec
 */
public class ControlImageDescriptor extends ImageDescriptor {
	private final Control c;
	private final int width, height;
	
	public ControlImageDescriptor(Control c, int width, int height) {
		this.c = c;
		this.width = width;
		this.height = height;
	}
	
	@Override
	public ImageData getImageData() {
		Display d = UI.getDisplay();
		Image i = new Image(d, width, height);
		try {
			ColorWrapper
				fill = new ColorWrapper(),
				outline = new ColorWrapper();
			
			GC gc = new GC(i);
			gc.setAntialias(SWT.ON);
			try {
				gc.setBackground(
						outline.update(ColourUtilities.getFill(c)));
				gc.setForeground(
						outline.update(ColourUtilities.getOutline(c)));
				Object shape = ControlUtilities.getShape(c);
				if (shape instanceof PointList) {
					PointList modified =
							LayoutUtilities.fitPolygon((PointList)shape,
									new Rectangle(0, 0, width, height));
					gc.fillPolygon(modified.toIntArray());
					gc.drawPolygon(modified.toIntArray());
				} else if (shape instanceof Ellipse) {
					gc.fillOval(0, 0, width - 1, height - 1);
					gc.drawOval(0, 0, width - 1, height - 1);
				} else {
					gc.setBackground(ColorConstants.red);
					gc.fillRectangle(0, 0, width, height);
				}
			} finally {
				gc.dispose();
			}
			
			fill.update(null);
			outline.update(null);
			
			return i.getImageData();
		} finally {
			i.dispose();
		}
	}
}