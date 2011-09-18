package dk.itu.big_red.editors.bigraph.figures.import_export;

import org.eclipse.draw2d.IFigure;

import dk.itu.big_red.import_export.Export;

/**
 * Classes extending FigureExport can write {@link IFigure}s to an {@link
 * OutputStream}.
 * @author alec
 */
public abstract class FigureExport<T extends IFigure> extends Export<T> {
}
