package dk.itu.big_red.model.load_save.loaders;

import dk.itu.big_red.model.load_save.LoaderUtilities;

public abstract class XMLLoader extends org.bigraph.model.loaders.XMLLoader {
	public static final String EXTENSION_POINT = "dk.itu.big_red.xml";
	
	public XMLLoader() {
		LoaderUtilities.installUndecorators(this);
	}
}
