package dk.itu.big_red.model.import_export;

import dk.itu.big_red.import_export.Export;

/**
 * Classes extending ModelExport can write model objects to an {@link
 * OutputStream}.
 * 
 * @author alec
 * @see ModelImport
 */
public abstract class ModelExport<T> extends Export<T> {

}
