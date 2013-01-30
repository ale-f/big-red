package dk.itu.big_red.model.load_save;

import org.bigraph.model.Layoutable;
import org.bigraph.model.NamedModelObject;
import org.bigraph.model.ModelObject.Identifier;
import org.bigraph.model.Site;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.loaders.BigraphEditLoader;
import org.bigraph.model.loaders.EditXMLLoader;
import org.bigraph.model.loaders.XMLLoader;
import org.bigraph.model.process.IParticipantHost;
import org.bigraph.model.savers.BigraphEditSaver;
import org.bigraph.model.savers.EditXMLSaver;
import org.bigraph.model.savers.IXMLSaver;
import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Element;

import dk.itu.big_red.model.Colour;
import dk.itu.big_red.model.ColourUtilities.ChangeFillDescriptor;
import dk.itu.big_red.model.ColourUtilities.ChangeOutlineDescriptor;
import dk.itu.big_red.model.ExtendedDataUtilities.ChangeAliasDescriptor;
import dk.itu.big_red.model.ExtendedDataUtilities.ChangeCommentDescriptor;
import dk.itu.big_red.model.LayoutUtilities.ChangeLayoutDescriptor;

import static dk.itu.big_red.model.BigRedNamespaceConstants.BIG_RED;
import static org.bigraph.model.loaders.XMLLoader.getAttributeNS;

public abstract class RedXMLEdits {
	private RedXMLEdits() {}
	
	public static final class LoadParticipant
			implements EditXMLLoader.Participant {
		@Override
		public void setHost(IParticipantHost host) {
			if (host instanceof EditXMLLoader)
				;
		}
		
		private static Rectangle loadLayout(Element e) {
			try {
				return new Rectangle(
						Integer.parseInt(getAttributeNS(e, BIG_RED, "x")),
						Integer.parseInt(getAttributeNS(e, BIG_RED, "y")),
						Integer.parseInt(getAttributeNS(e, BIG_RED, "width")),
						Integer.parseInt(getAttributeNS(e, BIG_RED, "height")));
			} catch (NumberFormatException ex) {
				return null;
			}
		}
		
		private static Colour loadColour(Element e) {
			String desc = getAttributeNS(e, BIG_RED, "colour");
			return (desc != null ? new Colour(desc) : null);
		}
		
		@Override
		public IChangeDescriptor getDescriptor(Element descriptor) {
			IChangeDescriptor cd = null;
			if (BIG_RED.equals(descriptor.getNamespaceURI())) {
				Layoutable.Identifier id = null;
				String ln = descriptor.getLocalName();
				/* XXX: the use of "null" here discards some history */
				if ("set-layout".equals(ln)) {
					id = BigraphEditLoader.getIdentifier(
							XMLLoader.getChildElements(descriptor).get(0),
							Layoutable.Identifier.class);
					cd = new ChangeLayoutDescriptor(id, null, loadLayout(descriptor));
				} else if ("set-fill".equals(ln) || "set-outline".equals(ln)) {
					id = BigraphEditLoader.getIdentifier(
							XMLLoader.getChildElements(descriptor).get(0),
							Layoutable.Identifier.class);
					Colour c = loadColour(descriptor);
					if ("set-fill".equals(ln)) {
						cd = new ChangeFillDescriptor(id, null, c);
					} else cd = new ChangeOutlineDescriptor(id, null, c);
				} else if ("set-comment".equals(ln)) {
					id = BigraphEditLoader.getIdentifier(
							XMLLoader.getChildElements(descriptor).get(0),
							Layoutable.Identifier.class);
					cd = new ChangeCommentDescriptor(id, null,
							getAttributeNS(descriptor, BIG_RED, "comment"));
				} else if ("set-alias".equals(ln)) {
					Site.Identifier sid = BigraphEditLoader.getIdentifier(
							XMLLoader.getChildElements(descriptor).get(0),
							Site.Identifier.class);
					cd = new ChangeAliasDescriptor(sid, null,
							getAttributeNS(descriptor, BIG_RED, "alias"));
				}
			}
			return cd;
		}

		@Override
		public IChangeDescriptor getRenameDescriptor(Element id, String name) {
			return null;
		}
	}
	
	public static final class SaveParticipant
			implements EditXMLSaver.Participant {
		private IXMLSaver saver;
		
		@Override
		public void setHost(IParticipantHost host) {
			if (host instanceof IXMLSaver)
				saver = (IXMLSaver)host;
		}
		
		private final Element newElement(String ns, String qn) {
			return saver.getDocument().createElementNS(ns, qn);
		}
		
		private static Element saveLayout(Element e, Rectangle r) {
			if (r != null) {
				e.setAttributeNS(null, "x", "" + r.x());
				e.setAttributeNS(null, "y", "" + r.y());
				e.setAttributeNS(null, "width", "" + r.width());
				e.setAttributeNS(null, "height", "" + r.height());
			}
			return e;
		}
		
		private static Element saveColour(Element e, Colour c) {
			if (c != null)
				e.setAttributeNS(null, "colour", c.toHexString());
			return e;
		}
		
		@Override
		public Element processDescriptor(IChangeDescriptor cd_) {
			Element e = null;
			Identifier target = null;
			if (cd_ instanceof ChangeCommentDescriptor) {
				ChangeCommentDescriptor cd = (ChangeCommentDescriptor)cd_;
				target = cd.getTarget();
				e = newElement(BIG_RED, "big-red:set-comment");
				e.setAttributeNS(null, "comment", cd.getNewValue());
			} else if (cd_ instanceof ChangeAliasDescriptor) {
				ChangeAliasDescriptor cd = (ChangeAliasDescriptor)cd_;
				target = cd.getTarget();
				e = newElement(BIG_RED, "big-red:set-alias");
				e.setAttributeNS(null, "alias", cd.getNewValue());
			} else if (cd_ instanceof ChangeFillDescriptor) {
				ChangeFillDescriptor cd = (ChangeFillDescriptor)cd_;
				target = cd.getTarget();
				e = saveColour(newElement(BIG_RED, "big-red:set-fill"),
						cd.getNewValue());
			} else if (cd_ instanceof ChangeOutlineDescriptor) {
				ChangeOutlineDescriptor cd = (ChangeOutlineDescriptor)cd_;
				target = cd.getTarget();
				e = saveColour(newElement(BIG_RED, "big-red:set-outline"),
						cd.getNewValue());
			} else if (cd_ instanceof ChangeLayoutDescriptor) {
				ChangeLayoutDescriptor cd = (ChangeLayoutDescriptor)cd_;
				target = cd.getTarget();
				e = saveLayout(newElement(BIG_RED, "big-red:set-layout"),
						cd.getNewValue());
			}
			if (e != null && target instanceof NamedModelObject.Identifier)
				e.appendChild(BigraphEditSaver.makeID(
						saver.getDocument(),
						(NamedModelObject.Identifier)target));
			return e;
		}
	}
}
