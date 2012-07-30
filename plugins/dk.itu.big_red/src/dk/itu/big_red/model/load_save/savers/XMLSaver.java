package dk.itu.big_red.model.load_save.savers;

import dk.itu.big_red.model.load_save.SaverUtilities;

public abstract class XMLSaver extends org.bigraph.model.savers.XMLSaver {
	public XMLSaver() {
		SaverUtilities.installDecorators(this);
	}
}