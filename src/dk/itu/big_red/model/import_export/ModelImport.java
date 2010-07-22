package dk.itu.big_red.model.import_export;

import dk.itu.big_red.import_export.Import;

/**
 * Classes extending ModelImport can read model objects from an {@link
 * InputStream}.
 * 
 * @author alec
 * @see ModelExport
 */
public abstract class ModelImport<T> extends Import<T> {

}
