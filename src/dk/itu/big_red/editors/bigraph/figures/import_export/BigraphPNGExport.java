package dk.itu.big_red.editors.bigraph.figures.import_export;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import dk.itu.big_red.import_export.Export;
import dk.itu.big_red.import_export.ExportFailedException;

public class BigraphPNGExport extends Export<IFigure> {

	@Override
	public void exportObject() throws ExportFailedException {
		Rectangle r = getModel().getBounds();
		
		Image image = null;
		GC graphicsContext = null;
		Graphics graphics = null;
		
		try {
			image = new Image(null, r.width, r.height);
			graphics = new SWTGraphics(graphicsContext = new GC(image));
			graphics.translate(r.x * -1, r.y * -1);
			
			getModel().paint(graphics);
			
			ImageLoader il = new ImageLoader();
			il.data = new ImageData[] {
				image.getImageData()
			};
			il.save(getOutputStream(), SWT.IMAGE_PNG);
		} catch (Exception e) {
			throw new ExportFailedException(e);
		} finally {
			if (graphics != null) graphics.dispose();
			if (graphicsContext != null) graphicsContext.dispose();
			if (image != null) image.dispose();
		}
	}

	@Override
	public Class<?> getType() {
		return IFigure.class;
	}

}
