package dk.itu.big_red.model.load_save.savers;

import static dk.itu.big_red.model.load_save.IRedNamespaceConstants.BIG_RED;
import static dk.itu.big_red.model.load_save.loaders.XMLLoader.getAttributeNS;
import static
	dk.itu.big_red.model.load_save.loaders.XMLLoader.getIntAttribute;
import static
	dk.itu.big_red.model.load_save.loaders.XMLLoader.getColorAttribute;
import static
	dk.itu.big_red.model.load_save.loaders.XMLLoader.getDoubleAttribute;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Control;
import org.bigraph.model.Edge;
import org.bigraph.model.Layoutable;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Port;
import org.bigraph.model.PortSpec;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Signature;
import org.bigraph.model.SimulationSpec;
import org.bigraph.model.ModelObject.ChangeExtendedData;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IChangeExecutor;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dk.itu.big_red.editors.assistants.Colour;
import dk.itu.big_red.editors.assistants.ColourUtilities;
import dk.itu.big_red.editors.assistants.Ellipse;
import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.editors.assistants.LayoutUtilities;
import dk.itu.big_red.model.load_save.IXMLLoader;
import dk.itu.big_red.model.load_save.IXMLUndecorator;
import dk.itu.big_red.model.load_save.LoaderNotice;
import dk.itu.big_red.model.load_save.loaders.XMLLoader;
import dk.itu.big_red.model.load_save.savers.XMLSaver.Decorator;

public class RedXMLDecorator implements Decorator, IXMLUndecorator {
	public static Element rectangleToElement(Element e, Rectangle r) {
		return XMLSaver.applyAttributes(e,
			"width", r.width(), "height", r.height(), "x", r.x(), "y", r.y());
	}
	
	@Override
	public void decorate(ModelObject object, Element el) {
		Document doc = el.getOwnerDocument();
		
		if (object instanceof Control) {
			Control c = (Control)object;
			Element aE = doc.createElementNS(BIG_RED, "big-red:shape");
			
			Object shape = ExtendedDataUtilities.getShape(c);
			aE.setAttributeNS(BIG_RED, "big-red:shape",
					(shape instanceof PointList ? "polygon" : "oval"));
			
			if (shape instanceof PointList) {
				PointList pl = (PointList)shape;
				for (int i = 0; i < pl.size(); i++) {
					Point p = pl.getPoint(i);
					Element pE = doc.createElementNS(BIG_RED, "big-red:point");
					pE.setAttributeNS(BIG_RED, "big-red:x", "" + p.x);
					pE.setAttributeNS(BIG_RED, "big-red:y", "" + p.y);
					aE.appendChild(pE);
				}
			}

			el.setAttributeNS(BIG_RED, "big-red:label",
					ExtendedDataUtilities.getLabel(c));
			el.appendChild(aE);
			/* continue */
		} else if (object instanceof PortSpec) {
			PortSpec p = (PortSpec)object;
			
			Element pA =
				doc.createElementNS(BIG_RED, "big-red:port-appearance");
			pA.setAttributeNS(BIG_RED, "big-red:segment", "" + ExtendedDataUtilities.getSegment(p));
			pA.setAttributeNS(BIG_RED, "big-red:distance", "" + ExtendedDataUtilities.getDistance(p));
			
			el.appendChild(pA);
			return;
		} else if (object instanceof Signature || object instanceof Bigraph ||
				object instanceof Port || object instanceof SimulationSpec ||
				object instanceof ReactionRule)
			return;
		
		Element aE = doc.createElementNS(BIG_RED, "big-red:appearance");
		
		if (object instanceof Layoutable) {
			Rectangle layout =
					LayoutUtilities.getLayoutRaw((Layoutable)object);
			if (layout != null)
				rectangleToElement(aE, layout);
		}
		
		Colour
			fill = ColourUtilities.getFillRaw(object),
			outline = ColourUtilities.getOutlineRaw(object);
		if (fill != null)
			aE.setAttributeNS(BIG_RED, "big-red:fillColor",
					fill.toHexString());
		if (outline != null)
			aE.setAttributeNS(BIG_RED, "big-red:outlineColor",
					outline.toHexString());
		
		String comment = ExtendedDataUtilities.getComment(object);
		if (comment != null)
			aE.setAttributeNS(BIG_RED, "big-red:comment", comment);
		
		if (aE.hasChildNodes() || aE.hasAttributes())
			el.appendChild(aE);
	}

	private static boolean cmpns(Node n, String ns, String ln) {
		return (ns.equals(n.getNamespaceURI()) && ln.equals(n.getLocalName()));
	}
	
	private Element getNamedChildElement(Element el, String ns, String ln) {
		NodeList children = el.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node j = children.item(i);
			if (j instanceof Element && cmpns(j, ns, ln))
				return (Element)j;
		}
		return null;
	}
	
	public static Rectangle getRectangle(Element e) {
		String
			rectX = XMLLoader.getAttributeNS(e, BIG_RED, "x"),
			rectY = XMLLoader.getAttributeNS(e, BIG_RED, "y"),
			rectW = XMLLoader.getAttributeNS(e, BIG_RED, "width"),
			rectH = XMLLoader.getAttributeNS(e, BIG_RED, "height");
		if (rectX != null && rectY != null && rectW != null && rectH != null) {
			try {
				return new Rectangle(
						Integer.parseInt(rectX), Integer.parseInt(rectY),
						Integer.parseInt(rectW), Integer.parseInt(rectH));
			} catch (NumberFormatException ex) {
				/* do nothing */
			}
		}
		return null;
	}
	
	private enum Tristate {
		TRUE,
		FALSE,
		UNKNOWN;
		
		private static Tristate fromBoolean(boolean b) {
			return (b ? TRUE : FALSE);
		}
	}
	
	private boolean partialAppearanceWarning = false;
	private Tristate appearanceAllowed = Tristate.UNKNOWN;
	
	private void doLayoutCheck(Rectangle r) {
		if (appearanceAllowed == Tristate.UNKNOWN) {
			appearanceAllowed = Tristate.fromBoolean(r != null);
		} else if (!partialAppearanceWarning &&
				(appearanceAllowed == Tristate.FALSE && r != null) ||
				(appearanceAllowed == Tristate.TRUE && r == null)) {
			loader.addNotice(LoaderNotice.Type.WARNING,
				"The layout data for this bigraph is incomplete and " +
				"so has been ignored.");
			appearanceAllowed = Tristate.FALSE;
			partialAppearanceWarning = true;
		}
	}
	
	@Override
	public void undecorate(ModelObject object, Element el) {
		ChangeGroup cg = new ChangeGroup();
		
		Rectangle r = null;
		Element eA = getNamedChildElement(el, BIG_RED, "appearance");
		if (eA != null) {
			Colour
				fill = getColorAttribute(eA, BIG_RED, "fillColor"),
				outline = getColorAttribute(eA, BIG_RED, "outlineColor");
			if (fill != null)
				cg.add(ColourUtilities.changeFill(object, fill));
			if (outline != null)
				cg.add(ColourUtilities.changeOutline(object, outline));
	
			if (object instanceof Layoutable) {
				r = getRectangle(eA);
				if (r != null)
					cg.add(
						LayoutUtilities.changeLayout((Layoutable)object, r));
			}
			
			String comment = XMLLoader.getAttributeNS(eA, BIG_RED, "comment");
			if (comment != null)
				cg.add(ExtendedDataUtilities.changeComment(object, comment));
		}
		
		if (object instanceof Layoutable && !(object instanceof Edge))
			doLayoutCheck(r);
		
		if (object instanceof PortSpec) {
			PortSpec p = (PortSpec)object;
			Element eS = getNamedChildElement(el, BIG_RED, "port-appearance");
			if (eS != null) {
				cg.add(ExtendedDataUtilities.changeSegment(p,
						getIntAttribute(eS, BIG_RED, "segment")));
				cg.add(ExtendedDataUtilities.changeDistance(p,
						getDoubleAttribute(eS, BIG_RED, "distance")));
			}
		}
		
		if (object instanceof Control) {
			Control c = (Control)object;
			
			String l = getAttributeNS(el, BIG_RED, "label");
			if (l != null)
				cg.add(ExtendedDataUtilities.changeLabel(c, l));
			
			Element eS = getNamedChildElement(el, BIG_RED, "shape");
			if (eS != null) {
				PointList pl = null;
				
				Object shape;
				String s = getAttributeNS(eS, BIG_RED, "shape");
				if (s != null && s.equals("polygon")) {
					pl = new PointList();
					NodeList nl = eS.getChildNodes();
					for (int i_ = 0; i_ < nl.getLength(); i_++) {
						Node i = nl.item(i_);
						if (i instanceof Element && cmpns(i, BIG_RED, "point"))
							pl.addPoint(
								getIntAttribute((Element)i, BIG_RED, "x"),
								getIntAttribute((Element)i, BIG_RED, "y"));
					}
					shape = pl;
				} else shape = Ellipse.SINGLETON;
				cg.add(ExtendedDataUtilities.changeShape(c, shape));
			}
		}
		
		if (cg.size() > 0)
			loader.addChange(cg);
	}
	
	private IXMLLoader loader;
	
	@Override
	public void setLoader(IXMLLoader loader) {
		this.loader = loader;
	}
	
	@Override
	public void finish(IChangeExecutor ex) {
		if (ex instanceof Bigraph) {
			Bigraph bigraph = (Bigraph)ex;
			IChange relayout =
					LayoutUtilities.relayout(loader.getScratch(), bigraph);
			
			if (appearanceAllowed == Tristate.FALSE) {
				loader.addChange(relayout);
			} else {
				try {
					bigraph.tryValidateChange(loader.getChanges());
				} catch (ChangeRejectedException cre) {
					IChange ch = cre.getRejectedChange();
					if (ch instanceof ChangeExtendedData) {
						ChangeExtendedData cd = (ChangeExtendedData)ch;
						if (LayoutUtilities.LAYOUT.equals(cd.key)) {
							loader.addNotice(LoaderNotice.Type.WARNING,
									"Layout data invalid: replacing.");
							loader.addChange(relayout);
						}
					}
				}
			}
		}
	}
}
