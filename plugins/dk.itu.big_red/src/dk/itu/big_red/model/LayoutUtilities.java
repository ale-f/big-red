package dk.itu.big_red.model;

import java.util.Arrays;
import java.util.List;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.ModelObject;
import org.bigraph.model.NamedModelObject;
import org.bigraph.model.ModelObject.ChangeExtendedData;
import org.bigraph.model.ModelObject.FinalExtendedDataValidator;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.Point;
import org.bigraph.model.Port;
import org.bigraph.model.Root;
import org.bigraph.model.Site;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import static org.bigraph.model.assistants.ExtendedDataUtilities.getProperty;
import static org.bigraph.model.assistants.ExtendedDataUtilities.setProperty;

/**
 * The <strong>LayoutUtilities</strong> class is a collection of static
 * methods and fields for manipulating objects' layout information.
 * @author alec
 * @see ColourUtilities
 * @see ExtendedDataUtilities
 */
public abstract class LayoutUtilities {
	private LayoutUtilities() {}
	
	@RedProperty(fired = Rectangle.class, retrieved = Rectangle.class)
	public static final String LAYOUT =
			"eD!+dk.itu.big_red.Layoutable.layout";
	private static final String BOUNDARIES =
			"eD!+org.bigraph.model.Bigraph.boundaries";
	
	private static final String getFN(
			PropertyScratchpad context, NamedModelObject l) {
		String type;
		if (l instanceof Node) {
			type = ((Node)l).getControl().getName();
		} else type = l.getType();
		return type + " " + l.getName(context);
	}
	
	static final FinalExtendedDataValidator layoutValidator =
			new FinalExtendedDataValidator() {
		@Override
		public void validate(ChangeExtendedData c, PropertyScratchpad context)
				throws ChangeRejectedException {
			/* do nothing */
		}
		
		@Override
		public void finalValidate(
				ChangeExtendedData c, PropertyScratchpad context)
				throws ChangeRejectedException {
			Layoutable l = (Layoutable)c.getCreator();
			Container parent = l.getParent(context);
			
			if (l instanceof Edge || parent == null)
				return;
			
			Rectangle
				oldLayout = getLayout(l),
				newLayout = getLayout(context, l);
			boolean
				checkChildren = true,
				checkSiblings = true;
			
			if (newLayout != null &&
					(newLayout.x() < 0 || newLayout.y() < 0))
				throw new ChangeRejectedException(c,
						getFN(context, l) +
						" must not have negative co-ordinates");
			
			if (oldLayout != null) {
				if (oldLayout.contains(newLayout))
					checkSiblings = false;
				if (oldLayout.getSize().equals(newLayout.getSize()))
					checkChildren = false;
			}
			
			if (checkSiblings) {
				for (Layoutable i : parent.getChildren(context)) {
					if (i == l || i instanceof Edge) {
						continue;
					} else {
						if (getLayout(context, i).intersects(newLayout)) {
							throw new ChangeRejectedException(c,
									getFN(context, l) + " overlaps with " +
									getFN(context, i));
						}
					}
				}
			}
			
			if (l instanceof Container && checkChildren) {
				Rectangle adjusted = newLayout.getCopy().setLocation(0, 0);
				for (Layoutable i : ((Container)l).getChildren(context)) {
					if (!adjusted.contains(getLayout(context, i)))
						throw new ChangeRejectedException(c,
								getFN(context, i) + " cannot fit inside " +
								getFN(context, l));
				}
			}
			
			if (!(parent instanceof Bigraph)) {
				Rectangle parentLayout =
						getLayout(context, parent).getCopy().setLocation(0, 0);
				if (!parentLayout.contains(newLayout))
					throw new ChangeRejectedException(c,
							getFN(context, l) + " cannot fit inside " +
							getFN(context, parent));
			} else if (checkSiblings) {
				Bigraph b = (Bigraph)parent;
				
				/* Since the layout validator is a final validator, there are
				 * no further updates to come, so the boundary state can be
				 * calculated once and then stashed away in the scratchpad */
				BigraphBoundaryState bbs = getProperty(
						context, b, BOUNDARIES, BigraphBoundaryState.class);
				if (bbs == null)
					setProperty(context, parent, BOUNDARIES,
							bbs = new BigraphBoundaryState(context, b));
				
				int bs = bbs.getBoundaryState(newLayout);
				if (l instanceof Root) {
					if ((bs & BigraphBoundaryState.B_UR) != 0) {
						throw new ChangeRejectedException(c,
								"Roots must be placed below all outer names");
					} else if ((bs & BigraphBoundaryState.B_LR) != 0) {
						throw new ChangeRejectedException(c,
								"Roots must be placed above all inner names");
					}
				} else if (l instanceof OuterName &&
						(bs & BigraphBoundaryState.B_LON) != 0) {
					throw new ChangeRejectedException(c,
							"Outer names must be placed above all roots");
				} else if (l instanceof InnerName &&
						(bs & BigraphBoundaryState.B_UIN) != 0) {
					throw new ChangeRejectedException(c,
							"Inner names must be placed below all roots");
				}
			}
		}
	};
	
	/**
	 * The space that should be present between any two {@link Layoutable}s
	 * after a <i>relayout</i> has been applied.
	 */
	protected static final int PADDING = 25;

	public static Rectangle getLayout(Layoutable l) {
		return getLayout(null, l);
	}

	public static Rectangle getLayout(
			PropertyScratchpad context, Layoutable l) {
		Rectangle r = getLayoutRaw(context, l);
		if (r == null) {
			if (l instanceof Port) {
				Port p = (Port)l;
				r = new Rectangle(0, 0, 10, 10);
				Object shape = ControlUtilities.getShape(p.getParent().getControl());
				double distance = ControlUtilities.getDistance(context, p.getSpec());
				if (shape instanceof PointList) {
					PointList polypt = fitPolygon(
							(PointList)shape, getLayout(p.getParent(context)));
					int segment = ControlUtilities.getSegment(context, p.getSpec());
					org.eclipse.draw2d.geometry.Point
						p1 = polypt.getPoint(segment),
						p2 = polypt.getPoint((segment + 1) % polypt.size());
					r.setLocation(new Line(p1, p2).
							getPointFromOffset(distance).translate(-5, -5));
				} else {
					r.setLocation(
						new Ellipse(
							getLayout(p.getParent()).getCopy().setLocation(0, 0)).
							getPointFromOffset(distance).translate(-5, -5));
				}
			} else if (l instanceof Edge) {
				List<? extends Point> points = ((Edge)l).getPoints(context);
				if (!points.isEmpty()) {
					org.eclipse.draw2d.geometry.Point fp =
							getRootLayout(context, points.get(0)).getCenter();
					r = new Rectangle(fp, fp);
					for (Point p : points)
						r.union(getRootLayout(p).getCenter());
					r.setLocation(r.getCenter().translate(-5, -5)).
							setSize(10, 10);
				}
			} else if (!(l instanceof Bigraph))
				setLayout(context, l, r = new Rectangle());
		}
		return r;
	}

	public static Rectangle getLayoutRaw(Layoutable l) {
		return getLayoutRaw(null, l);
	}
	
	public static Rectangle getLayoutRaw(
			PropertyScratchpad context, Layoutable l) {
		return getProperty(context, l, LAYOUT, Rectangle.class);
	}
	
	public static void setLayout(Layoutable l, Rectangle r) {
		setLayout(null, l, r);
	}

	public static void setLayout(
			PropertyScratchpad context, Layoutable l, Rectangle r) {
		setProperty(context, l, LAYOUT, r);
	}

	public static IChange changeLayout(Layoutable l, Rectangle r) {
		return l.changeExtendedData(LAYOUT, r, layoutValidator);
	}
	
	public static IChangeDescriptor changeLayoutDescriptor(
			Layoutable.Identifier l, Rectangle oldR, Rectangle newR) {
		return new ModelObject.ChangeExtendedDataDescriptor(
				l, LAYOUT, oldR, newR, layoutValidator, null);
	}

	public static Rectangle getRootLayout(Layoutable l) {
		return getRootLayout(null, l);
	}

	public static Rectangle getRootLayout(
			PropertyScratchpad context, Layoutable l) {
		Rectangle r = getLayout(context, l), r2;
		if (r != null) {
			r = r.getCopy();
			while ((l = l.getParent(context)) != null &&
					(r2 = getLayout(context, l)) != null)
				r.translate(r2.x, r2.y);
		}
		return r;
	}

	public static IChange relayout(Bigraph b) {
		return relayout(new PropertyScratchpad(), b);
	}

	public static IChange relayout(PropertyScratchpad context, Bigraph b) {
		ChangeGroup cg = new ChangeGroup();
		relayout(context, b, cg);
		return cg;
	}

	static Rectangle relayout(
			PropertyScratchpad context, Layoutable l, ChangeGroup cg) {
		assert
			context != null;
		
		if (l instanceof Link) {
			cg.addAll(Arrays.asList(
					LinkStyleUtilities.changeStyle((Link)l, null),
					ColourUtilities.changeOutline(l, Colour.random())));
		}
		
		Rectangle r = null;
		if (l instanceof Site || l instanceof InnerName ||
				l instanceof OuterName) {
			setLayout(context, l, r = new Rectangle(0, 0, 50, 50));
		} else if (l instanceof Edge) {
			setLayout(context, l, r = null);
		} else if (l instanceof Node || l instanceof Root) {
			int left = PADDING, height = 0;
			for (Layoutable i : ((Container)l).getChildren(context)) {
				r = relayout(context, i, cg).setLocation(left, 0);
				left = left + r.width + PADDING;
				if (height < r.height)
					height = r.height;
				cg.add(changeLayout(i, r));
			}
			for (Layoutable i : ((Container)l).getChildren(context))
				(r = getLayout(context, i)).y =
					PADDING + ((height - r.height) / 2);
			if (left == PADDING)
				left += PADDING;
			setLayout(context, l,
					r = new Rectangle(0, 0, left, (PADDING * 2) + height));
		} else if (l instanceof Bigraph) {
			Bigraph b = (Bigraph)l;
			List<? extends Layoutable> children = b.getChildren(context);
			
			int
				tallestOuterName = 0,
				tallestRoot = 0;
			int
				onLeft = PADDING,
				rootLeft = PADDING,
				inLeft = PADDING;
			
			for (Layoutable i : children) {
				if (i instanceof Edge)
					continue;
				r = relayout(context, i, cg);
				if (r != null)
					r.y = PADDING;
				if (i instanceof OuterName) {
					r.x = onLeft;
					onLeft = onLeft + r.width + PADDING;
					if (tallestOuterName < r.height)
						tallestOuterName = r.height;
				} else if (i instanceof Root) {
					r.x = rootLeft;
					rootLeft = rootLeft + r.width + PADDING;
					if (tallestRoot < r.height)
						tallestRoot = r.height;
				} else if (i instanceof InnerName) {
					r.x = inLeft;
					inLeft = inLeft + r.width + PADDING;
				}
				cg.add(changeLayout(i, r));
			}
			
			for (Layoutable i : children) {
				r = getLayout(context, i);
				if (i instanceof Root) {
					if (tallestOuterName > 0)
						r.y += ((tallestRoot - r.height) / 2) +
								tallestOuterName + PADDING;
				} else if (i instanceof InnerName) {
					if (tallestOuterName > 0)
						r.y += tallestOuterName + PADDING;
					if (tallestRoot > 0)
						r.y += tallestRoot + PADDING;
				} else if (i instanceof Edge) {
					cg.add(changeLayout(i,
							relayout(context, i, cg)));
				}
			}
		}
		return r;
	}

	public static final PointList fitPolygon(PointList p, Rectangle l) {
		(p = p.getCopy()).translate(p.getBounds().getTopLeft().getNegated());
		
		Rectangle adjustedBounds = new Rectangle(p.getBounds());
		double xScale = l.width() - 2,
		       yScale = l.height() - 2;
		xScale /= adjustedBounds.width() - 1;
		yScale /= adjustedBounds.height() - 1;
		
		org.eclipse.draw2d.geometry.Point tmp =
				org.eclipse.draw2d.geometry.Point.SINGLETON;
		for (int i = 0; i < p.size(); i++) {
			p.getPoint(tmp, i).scale(xScale, yScale).translate(1, 1);
			p.setPoint(tmp, i);
		}
		
		return p;
	}
}
