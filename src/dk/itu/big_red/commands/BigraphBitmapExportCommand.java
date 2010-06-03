package dk.itu.big_red.commands;


import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.commands.Command;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import dk.itu.big_red.GraphicalEditor;

public class BigraphBitmapExportCommand extends Command {
	private String filename = null;
	private int format = SWT.IMAGE_PNG;
	private GraphicalEditor editor = null;
	
	@Override
	public boolean canExecute() {
		return (getFilename() != null && editor != null);
	}
	
	@Override
	public void execute() {
		if (canExecute()) {
			IFigure figure = getEditor().getPrintLayer();
			
			Device display = getEditor().getGraphicalViewer().getControl().getDisplay();
			Rectangle r = figure.getBounds();
			
			Image image = null;
			GC graphicsContext = null;
			Graphics graphics = null;
			
			try {
				image = new Image(display, r.width, r.height);
				graphics = new SWTGraphics(graphicsContext = new GC(image));
				graphics.translate(r.x * -1, r.y * -1);
				
				figure.paint(graphics);
				
				ImageLoader il = new ImageLoader();
				il.data = new ImageData[] {
					image.getImageData()
				};
				il.save(getFilename(), getFormat());
			} finally {
				if (graphics != null) graphics.dispose();
				if (graphicsContext != null) graphicsContext.dispose();
				if (image != null) image.dispose();
			}
		}
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}

	public void setFormat(int format) {
		this.format = format;
	}

	public int getFormat() {
		return format;
	}

	public void setEditor(GraphicalEditor editor) {
		this.editor = editor;
	}

	public GraphicalEditor getEditor() {
		return editor;
	}
}
