package dk.itu.big_red.model;

import static org.bigraph.model.assistants.ExtendedDataUtilities.getProperty;
import static dk.itu.big_red.model.BigRedNamespaceConstants.BIG_RED;

import java.util.Locale;

import org.bigraph.model.Link;
import org.bigraph.model.ModelObject;
import org.bigraph.model.assistants.ExtendedDataUtilities.ChangeExtendedDataDescriptor;
import org.bigraph.model.assistants.ExtendedDataUtilities.SimpleHandler;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.loaders.IXMLLoader;
import org.bigraph.model.loaders.XMLLoader;
import org.bigraph.model.process.IParticipantHost;
import org.bigraph.model.savers.IXMLSaver;
import org.w3c.dom.Element;

public abstract class LinkStyleUtilities {
	private LinkStyleUtilities() {}
	
	public static enum Style {
		CURVY {
			@Override
			public String getDisplayName() {
				return "B\u00E9zier curve (default)";
			}
		},
		STRAIGHT {
			@Override
			public String getDisplayName() {
				return "Straight lines";
			}
		},
		MANHATTAN {
			@Override
			public String getDisplayName() {
				return "Taxicab geometry";
			}
		};
		
		public abstract String getDisplayName();
	}
	
	@RedProperty(fired = Style.class, retrieved = Style.class)
	public static final String STYLE =
			"eD!+org.bigraph.model.Link.style";
	
	public static final class ChangeLinkStyleDescriptor
			extends ChangeExtendedDataDescriptor<Link.Identifier, Style> {
		static {
			DescriptorExecutorManager.getInstance().addParticipant(
					new SimpleHandler(ChangeLinkStyleDescriptor.class));
		}
		
		public ChangeLinkStyleDescriptor(Link.Identifier identifier,
				Style oldValue, Style newValue) {
			super(STYLE, identifier, oldValue, newValue);
		}
		
		public ChangeLinkStyleDescriptor(PropertyScratchpad context,
				Link mo, Style newValue) {
			this(mo.getIdentifier(context),
					getStyleRaw(context, mo), newValue);
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeLinkStyleDescriptor(
					getTarget(), getNewValue(), getOldValue());
		}
	}
	
	public static Style getStyle(Link l) {
		return getStyle(null, l);
	}
	
	public static Style getStyle(PropertyScratchpad context, Link l) {
		Style s = getStyleRaw(context, l);
		return (s != null ? s : Style.CURVY);
	}
	
	public static Style getStyleRaw(PropertyScratchpad context, Link l) {
		return getProperty(context, l, STYLE, Style.class);
	}
	
	public static final class Decorator implements IXMLSaver.Decorator {
		@Override
		public void setHost(IParticipantHost host) {
		}

		@Override
		public void decorate(ModelObject object, Element el) {
			if (object instanceof Link) {
				Style style = getStyleRaw(null, (Link)object);
				String name = null;
				if (Style.CURVY.equals(style)) {
					name = "curvy";
				} else if (Style.MANHATTAN.equals(style)) {
					name = "manhattan";
				} else if (Style.STRAIGHT.equals(style)) {
					name = "straight";
				}
				if (name != null)
					el.setAttributeNS(BIG_RED, "big-red:link-style", name);
			}
		}
	}
	
	public static final class Undecorator implements IXMLLoader.Undecorator {
		private IXMLLoader loader;
		
		@Override
		public void setHost(IParticipantHost host) {
			if (host instanceof IXMLLoader)
				loader = (IXMLLoader)host;
		}
		
		@Override
		public void undecorate(ModelObject object, Element el) {
			if (object instanceof Link) {
				Link l = (Link)object;
				String styleName =
						XMLLoader.getAttributeNS(el, BIG_RED, "link-style");
				if (styleName != null)
					styleName = styleName.toLowerCase(Locale.ENGLISH);
				Style style = null;
				if ("curvy".equals(styleName)) {
					style = Style.CURVY;
				} else if ("manhattan".equals(styleName)) {
					style = Style.MANHATTAN;
				} else if ("straight".equals(styleName)) {
					style = Style.STRAIGHT;
				}
				if (style != null)
					loader.addChange(new ChangeLinkStyleDescriptor(
							loader.getScratch(), l, style));
			}
		}

		@Override
		public void finish() {
		}
	}
}
