package dk.itu.big_red.model.load_save;

import org.bigraph.model.ModelObject;
import org.bigraph.model.Site;
import org.bigraph.model.changes.IChangeExecutor;
import org.bigraph.model.loaders.IXMLLoader;
import org.bigraph.model.loaders.IXMLUndecorator;
import org.bigraph.model.savers.IXMLDecorator;
import org.bigraph.model.savers.Saver;
import org.w3c.dom.Element;

import dk.itu.big_red.model.ExtendedDataUtilities;

import static org.bigraph.model.loaders.XMLLoader.getAttributeNS;
import static org.bigraph.model.loaders.RedNamespaceConstants.BIGRAPH;

public abstract class AliasSupport {
	private AliasSupport() {}
	
	public static final class Undecorator implements IXMLUndecorator {
		private IXMLLoader loader;
		
		@Override
		public void setLoader(IXMLLoader loader) {
			this.loader = loader;
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

		@Override
		public IXMLUndecorator newInstance() {
			return new Undecorator();
		}
	}
	
	public static final class Decorator implements IXMLDecorator {
		@Override
		public void setSaver(Saver saver) {
			/* do nothing */
		}
		
		@Override
		public IXMLDecorator newInstance() {
			return new Decorator();
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
