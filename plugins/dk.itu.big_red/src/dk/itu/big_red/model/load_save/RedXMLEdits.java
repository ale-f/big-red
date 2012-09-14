package dk.itu.big_red.model.load_save;

import org.bigraph.model.ModelObject.ChangeExtendedDataDescriptor;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.loaders.EditXMLLoader;
import org.bigraph.model.loaders.ILoader;
import org.bigraph.model.loaders.ILoader.Participant;
import org.bigraph.model.savers.BigraphEditSaver;
import org.bigraph.model.savers.EditXMLSaver;
import org.bigraph.model.savers.ISaver;
import org.bigraph.model.savers.IXMLSaver;
import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Element;

import dk.itu.big_red.model.Colour;
import dk.itu.big_red.model.ColourUtilities;
import dk.itu.big_red.model.ExtendedDataUtilities;
import dk.itu.big_red.model.LayoutUtilities;

import static org.bigraph.model.loaders.RedNamespaceConstants.BIG_RED;

public abstract class RedXMLEdits {
	private RedXMLEdits() {}
	
	public static final class LoadParticipant
			implements EditXMLLoader.Participant {
		@Override
		public Participant newInstance() {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public void setLoader(ILoader loader) {
			// TODO Auto-generated method stub
		}
		
		@Override
		public IChangeDescriptor getDescriptor(Element descriptor) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public IChangeDescriptor getRenameDescriptor(Element id, String name) {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	public static final class SaveParticipant
			implements EditXMLSaver.Participant {
		private IXMLSaver saver;
		
		@Override
		public void setSaver(ISaver saver) {
			if (saver instanceof IXMLSaver)
				this.saver = (IXMLSaver)saver;
		}
		
		@Override
		public SaveParticipant newInstance() {
			return new SaveParticipant();
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
			e.setAttributeNS(null, "colour", c.toHexString());
			return e;
		}
		
		@Override
		public Element processDescriptor(IChangeDescriptor cd_) {
			System.out.println(this + ".processDescriptor(" + cd_ + ")");
			Element e = null;
			if (cd_ instanceof ChangeExtendedDataDescriptor) {
				ChangeExtendedDataDescriptor cd =
						(ChangeExtendedDataDescriptor)cd_;
				String key = cd.getKey();
				System.out.println(key);
				if (LayoutUtilities.LAYOUT.equals(key)) {
					e = saveLayout(newElement(BIG_RED, "big-red:set-layout"),
							(Rectangle)cd.getNewValue());
				} else if (ColourUtilities.FILL.equals(key)) {
					e = saveColour(newElement(BIG_RED, "big-red:set-fill"),
							(Colour)cd.getNewValue());
				} else if (ColourUtilities.OUTLINE.equals(key)) {
					e = saveColour(newElement(BIG_RED, "big-red:set-outline"),
							(Colour)cd.getNewValue());
				} else if (ExtendedDataUtilities.COMMENT.equals(key)) {
					e = newElement(BIG_RED, "big-red:set-comment");
					e.setAttributeNS(null,
							"comment", (String)cd.getNewValue());
				}
				if (e != null)
					e.appendChild(BigraphEditSaver.makeID(
							saver.getDocument(), cd.getTarget()));
			}
			return e;
		}
	}
}
