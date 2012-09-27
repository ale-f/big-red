package dk.itu.big_red.model.load_save;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Control;
import org.bigraph.model.Edge;
import org.bigraph.model.Layoutable;
import org.bigraph.model.ModelObject;
import org.bigraph.model.ModelObject.ChangeExtendedData;
import org.bigraph.model.PortSpec;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IChangeExecutor;
import org.bigraph.model.loaders.ILoader;
import org.bigraph.model.loaders.IXMLLoader;
import org.bigraph.model.loaders.LoaderNotice;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import dk.itu.big_red.model.Colour;
import dk.itu.big_red.model.ColourUtilities;
import dk.itu.big_red.model.ControlUtilities;
import dk.itu.big_red.model.Ellipse;
import dk.itu.big_red.model.ExtendedDataUtilities;
import dk.itu.big_red.model.LayoutUtilities;
import static org.bigraph.model.loaders.RedNamespaceConstants.BIG_RED;
import static org.bigraph.model.loaders.XMLLoader.getAttributeNS;
import static org.bigraph.model.loaders.XMLLoader.getDoubleAttribute;
import static org.bigraph.model.loaders.XMLLoader.getIntAttribute;

public class RedXMLUndecorator implements IXMLLoader.Undecorator {
	@Override
	public RedXMLUndecorator newInstance() {
		return new RedXMLUndecorator();
	}
	
	private enum Tristate {
		FALSE,
		TRUE,
		UNKNOWN;
		
		private static Tristate fromBoolean(boolean b) {
			return (b ? TRUE : FALSE);
		}
	}
	
	private boolean partialAppearanceWarning = false;
	private Tristate appearanceAllowed = Tristate.UNKNOWN;
	private IXMLLoader loader;

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
			rectX = getAttributeNS(e, BIG_RED, "x"),
			rectY = getAttributeNS(e, BIG_RED, "y"),
			rectW = getAttributeNS(e, BIG_RED, "width"),
			rectH = getAttributeNS(e, BIG_RED, "height");
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

	private void doLayoutCheck(Rectangle r) {
		if (appearanceAllowed == Tristate.UNKNOWN) {
			appearanceAllowed = Tristate.fromBoolean(r != null);
		} else if (!partialAppearanceWarning &&
				((appearanceAllowed == Tristate.FALSE && r != null) ||
				 (appearanceAllowed == Tristate.TRUE && r == null))) {
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
				fill = RedXMLUndecorator.getColorAttribute(eA, BIG_RED, "fillColor"),
				outline = RedXMLUndecorator.getColorAttribute(eA, BIG_RED, "outlineColor");
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
			
			String comment = getAttributeNS(eA, BIG_RED, "comment");
			if (comment != null)
				cg.add(ExtendedDataUtilities.changeComment(object, comment));
		}
		
		if (object instanceof Layoutable && !(object instanceof Edge) &&
				!(object instanceof Bigraph))
			doLayoutCheck(r);
		
		if (object instanceof PortSpec) {
			PortSpec p = (PortSpec)object;
			Element eS = getNamedChildElement(el, BIG_RED, "port-appearance");
			if (eS != null) {
				cg.add(ControlUtilities.changeSegment(p,
						getIntAttribute(eS, BIG_RED, "segment")));
				cg.add(ControlUtilities.changeDistance(p,
						getDoubleAttribute(eS, BIG_RED, "distance")));
			}
		}
		
		if (object instanceof Control) {
			Control c = (Control)object;
			
			String l = getAttributeNS(el, BIG_RED, "label");
			if (l != null)
				cg.add(ControlUtilities.changeLabel(c, l));
			
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
				cg.add(ControlUtilities.changeShape(c, shape));
			}
		}
		
		if (cg.size() > 0)
			loader.addChange(cg);
	}

	@Override
	public void setLoader(ILoader loader) {
		if (loader instanceof IXMLLoader)
			this.loader = (IXMLLoader)loader;
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

	private static final Colour
			getColorAttribute(Element d, String nsURI, String n) {
		String attr = getAttributeNS(d, nsURI, n);
		return (attr != null ? new Colour(attr) : null);
	}
}