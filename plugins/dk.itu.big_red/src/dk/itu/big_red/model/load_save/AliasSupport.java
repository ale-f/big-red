package dk.itu.big_red.model.load_save;

import org.bigraph.model.ModelObject;
import org.bigraph.model.Site;
import org.bigraph.model.changes.IChangeExecutor;
import org.bigraph.model.loaders.ILoader;
import org.bigraph.model.loaders.IXMLLoader;
import org.bigraph.model.savers.ISaver;
import org.bigraph.model.savers.IXMLSaver;
import org.w3c.dom.Element;

import dk.itu.big_red.model.ExtendedDataUtilities;

import static org.bigraph.model.loaders.XMLLoader.getAttributeNS;
import static org.bigraph.model.loaders.RedNamespaceConstants.BIGRAPH;

public abstract class AliasSupport {
	private AliasSupport() {}
	
	public static final class Undecorator implements IXMLLoader.Undecorator {
		private IXMLLoader loader;
		
		@Override
		public void setLoader(ILoader loader) {
			if (loader instanceof IXMLLoader)
				this.loader = (IXMLLoader)loader;
		}
	
		@Override
		public void undecorate(ModelObject object, Element el) {
			if (object instanceof Site) {
				String alias = getAttributeNS(el, BIGRAPH, "alias");
				if (alias != null)
					loader.addChange(
							ExtendedDataUtilities.changeAlias((Site)object,
							alias));
			}
		}

		@Override
		public void finish(IChangeExecutor ex) {
		}
	}
	
	public static final class Decorator implements IXMLSaver.Decorator {
		@Override
		public void setSaver(ISaver saver) {
			/* do nothing */
		}

		@Override
		public void decorate(ModelObject object, Element el) {
			if (object instanceof Site) {
				String alias = ExtendedDataUtilities.getAlias((Site)object);
				if (alias != null)
					el.setAttribute("alias", alias);
			}
		}
	}
}
