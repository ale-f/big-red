package dk.itu.big_red.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.Point;
import org.bigraph.model.Port;
import org.bigraph.model.Root;
import org.bigraph.model.Site;
import org.bigraph.model.assistants.ExtendedDataUtilities.ChangeExtendedDataDescriptor;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
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
			} else if (!(l instanceof Bigraph)) {
				setProperty(context, l, LAYOUT, r = new Rectangle());
			}
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

	public static final class ChangeLayoutDescriptor
			extends ChangeExtendedDataDescriptor<
					Layoutable.Identifier, Rectangle> {
		static {
			DescriptorExecutorManager.getInstance().addParticipant(
					new LayoutHandler());
		}
		
		private static final class LayoutHandler extends Handler {
			@Override
			public boolean tryValidateChange(Process context,
					IChangeDescriptor change) throws ChangeCreationException {
				final PropertyScratchpad scratch = context.getScratch();
				final Resolver resolver = context.getResolver();
				if (change instanceof ChangeLayoutDescriptor) {
					final ChangeLayoutDescriptor cd =
							(ChangeLayoutDescriptor)change;
					final Layoutable l =
							cd.getTarget().lookup(scratch, resolver);
					if (l == null)
						throw new ChangeCreationException(cd,
								"" + cd.getTarget() + ": lookup failed");
					
					context.addCallback(new Callback() {
		@Override
		public void run() throws ChangeCreationException {
			Container parent = l.getParent(scratch);
			if (l instanceof Edge || parent == null)
				return;
			
			Rectangle
				oldLayout = getLayout(l),
				newLayout = getLayout(scratch, l);
			boolean
				checkChildren = true,
				checkSiblings = true;
			
			if (newLayout != null &&
					(newLayout.x() < 0 || newLayout.y() < 0))
				throw new ChangeCreationException(cd,
						l.toString(scratch) +
						" must not have negative co-ordinates");
			
			if (oldLayout != null) {
				if (oldLayout.contains(newLayout))
					checkSiblings = false;
				if (oldLayout.getSize().equals(newLayout.getSize()))
					checkChildren = false;
			}
			
			if (checkSiblings) {
				for (Layoutable i : parent.getChildren(scratch)) {
					if (i == l || i instanceof Edge) {
						continue;
					} else {
						if (getLayout(scratch, i).intersects(newLayout)) {
							throw new ChangeCreationException(cd,
									l.toString(scratch) + " overlaps with " +
									i.toString(scratch));
						}
					}
				}
			}
			
			if (l instanceof Container && checkChildren) {
				Rectangle adjusted = newLayout.getCopy().setLocation(0, 0);
				for (Layoutable i : ((Container)l).getChildren(scratch)) {
					if (!adjusted.contains(getLayout(scratch, i)))
						throw new ChangeCreationException(cd,
								i.toString(scratch) + " cannot fit inside " +
								l.toString(scratch));
				}
			}
			
			if (!(parent instanceof Bigraph)) {
				Rectangle parentLayout =
						getLayout(scratch, parent).getCopy().setLocation(0, 0);
				if (!parentLayout.contains(newLayout))
					throw new ChangeCreationException(cd,
							l.toString(scratch) + " cannot fit inside " +
							parent.toString(scratch));
			} else if (checkSiblings) {
				Bigraph b = (Bigraph)parent;
				
				/* Since the layout validator is a final validator, there are
				 * no further updates to come, so the boundary state can be
				 * calculated once and then stashed away in the scratchpad */
				BigraphBoundaryState bbs = getProperty(
						scratch, b, BOUNDARIES, BigraphBoundaryState.class);
				if (bbs == null)
					setProperty(scratch, parent, BOUNDARIES,
							bbs = new BigraphBoundaryState(scratch, b));
				
				int bs = bbs.getBoundaryState(newLayout);
				if (l instanceof Root) {
					if ((bs & BigraphBoundaryState.B_UR) != 0) {
						throw new ChangeCreationException(cd,
								"Roots must be placed below all outer names");
					} else if ((bs & BigraphBoundaryState.B_LR) != 0) {
						throw new ChangeCreationException(cd,
								"Roots must be placed above all inner names");
					}
				} else if (l instanceof OuterName &&
						(bs & BigraphBoundaryState.B_LON) != 0) {
					throw new ChangeCreationException(cd,
							"Outer names must be placed above all roots");
				} else if (l instanceof InnerName &&
						(bs & BigraphBoundaryState.B_UIN) != 0) {
					throw new ChangeCreationException(cd,
							"Inner names must be placed below all roots");
				}
			}
						}
					});
				} else return false;
				return true;
			}
			
			@Override
			public boolean executeChange(Resolver resolver,
					IChangeDescriptor change) {
				if (change instanceof ChangeLayoutDescriptor) {
					ChangeLayoutDescriptor cd = (ChangeLayoutDescriptor)change;
					cd.getTarget().lookup(null, resolver).setExtendedData(
							cd.getKey(),
							cd.getNormalisedNewValue(null, resolver));
				} else return false;
				return true;
			}
		}
		
		public ChangeLayoutDescriptor(Layoutable.Identifier identifier,
				Rectangle oldValue, Rectangle newValue) {
			super(LAYOUT, identifier, oldValue, newValue);
		}
		
		public ChangeLayoutDescriptor(PropertyScratchpad context,
				Layoutable mo, Rectangle newValue) {
			this(mo.getIdentifier(context),
					getLayoutRaw(context, mo), newValue);
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeLayoutDescriptor(
					getTarget(), getNewValue(), getOldValue());
		}
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

	public static IChangeDescriptor relayout(Bigraph b) {
		return relayout(new PropertyScratchpad(), b);
	}

	public static IChangeDescriptor relayout(
			PropertyScratchpad context, Bigraph b) {
		ChangeDescriptorGroup cdg = new ChangeDescriptorGroup();
		relayout(context, new PropertyScratchpad(context), b, cdg);
		return cdg;
	}

	static Rectangle relayout(PropertyScratchpad context,
			PropertyScratchpad tracker,
			Layoutable l, ChangeDescriptorGroup cdg) {
		/* The first PropertyScratchpad represents the world and the second
		 * stores the provisional changes made by the relayout operation */
		assert
			tracker != null;
		
		if (l instanceof Link)
			cdg.addAll(Arrays.<IChangeDescriptor>asList(
					new LinkStyleUtilities.ChangeLinkStyleDescriptor(
							context, (Link)l, null),
					new ColourUtilities.ChangeOutlineDescriptor(
							l.getIdentifier(context),
							ColourUtilities.getOutline(context, l),
							Colour.random())));
		
		Rectangle r = null;
		if (l instanceof Site || l instanceof InnerName ||
				l instanceof OuterName) {
			setProperty(tracker, l, LAYOUT, r = new Rectangle(0, 0, 50, 50));
		} else if (l instanceof Edge) {
			setProperty(tracker, l, LAYOUT, r = null);
		} else if (l instanceof Node || l instanceof Root) {
			boolean horizontal = (l instanceof Root);
			int progress = PADDING, max = 0;
			Collection<? extends Layoutable> children =
					((Container)l).getChildren(context);
			for (Layoutable i : children) {
				r = relayout(context, tracker, i, cdg);
				if (horizontal) {
					r.setLocation(progress, 0);
					progress = progress + r.width + PADDING;
					if (max < r.height)
						max = r.height;
				} else {
					r.setLocation(0, progress);
					progress = progress + r.height + PADDING;
					if (max < r.width)
						max = r.width;
				}
				cdg.add(new ChangeLayoutDescriptor(context, i, r));
			}
			for (Layoutable i : children) {
				r = getLayout(tracker, i);
				if (horizontal) {
					r.y = PADDING + ((max - r.height) / 2);
				} else r.x = PADDING + ((max - r.width) / 2);
			}
			if (progress == PADDING)
				progress += PADDING;
			if (horizontal) {
				r = new Rectangle(0, 0, progress, (PADDING * 2) + max);
			} else {
				r = new Rectangle(0, 0, (PADDING * 2) + max, progress);
			}
			setProperty(tracker, l, LAYOUT, r);
		} else if (l instanceof Bigraph) {
			Bigraph b = (Bigraph)l;
			Collection<? extends Layoutable> children = b.getChildren(context);
			
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
				r = relayout(context, tracker, i, cdg);
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
				cdg.add(new ChangeLayoutDescriptor(context, i, r));
			}
			
			for (Layoutable i : children) {
				r = getLayout(tracker, i);
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
					cdg.add(new ChangeLayoutDescriptor(context, i,
							relayout(context, tracker, i, cdg)));
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
