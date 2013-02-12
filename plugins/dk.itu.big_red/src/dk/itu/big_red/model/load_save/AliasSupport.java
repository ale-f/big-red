package dk.itu.big_red.model.load_save;

import org.bigraph.model.ModelObject;
import org.bigraph.model.Site;
import org.bigraph.model.loaders.IXMLLoader;
import org.bigraph.model.process.IParticipantHost;
import org.bigraph.model.savers.IXMLSaver;
import org.w3c.dom.Element;

import dk.itu.big_red.model.ExtendedDataUtilities;
import dk.itu.big_red.model.ExtendedDataUtilities.ChangeAliasDescriptor;

import static org.bigraph.model.loaders.XMLLoader.getAttributeNS;
import static org.bigraph.model.loaders.RedNamespaceConstants.BIGRAPH;

public abstract class AliasSupport {
	private AliasSupport() {}
	
	public static final class Undecorator implements IXMLLoader.Undecorator {
		private IXMLLoader loader;
		
		@Override
		public void setHost(IParticipantHost host) {
			if (host instanceof IXMLLoader)
				loader = (IXMLLoader)host;
		}
	
		@Override
		public void undecorate(ModelObject object, Element el) {
			if (object instanceof Site) {
				Site site = (Site)object;
				String alias = getAttributeNS(el, BIGRAPH, "alias");
				if (alias != null)
					loader.addChange(new ChangeAliasDescriptor(
							site.getIdentifier(), null,
							ExtendedDataUtilities.getAlias(
									loader.getScratch(), site)));
			}
		}

		@Override
		public void finish() {
		}
	}
	
	public static final class Decorator implements IXMLSaver.Decorator {
		@Override
		public void setHost(IParticipantHost host) {
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
