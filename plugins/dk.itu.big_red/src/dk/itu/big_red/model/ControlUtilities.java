package dk.itu.big_red.model;

import java.util.List;

import org.bigraph.model.Control;
import org.bigraph.model.PortSpec;
import org.bigraph.model.assistants.ExtendedDataUtilities.ChangeExtendedDataDescriptor;
import org.bigraph.model.assistants.ExtendedDataUtilities.SimpleHandler;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.RedProperty;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import static org.bigraph.model.assistants.ExtendedDataUtilities.getProperty;
import static org.bigraph.model.assistants.ExtendedDataUtilities.setProperty;

public abstract class ControlUtilities {
	private ControlUtilities() {}

	@RedProperty(fired = Integer.class, retrieved = Integer.class)
	public static final String SEGMENT =
			"eD!+dk.itu.big_red.model.PortSpec.segment";
	
	public static final class ChangeSegmentDescriptor
			extends ChangeExtendedDataDescriptor<
					PortSpec.Identifier, Integer> {
		static {
			DescriptorExecutorManager.getInstance().addParticipant(
					new SimpleHandler(ChangeSegmentDescriptor.class));
		}
		
		public ChangeSegmentDescriptor(PortSpec.Identifier id,
				int oldValue, int newValue) {
			super(SEGMENT, id, oldValue, newValue);
		}
		
		public ChangeSegmentDescriptor(PropertyScratchpad context,
				PortSpec mo, int newValue) {
			this(mo.getIdentifier(context),
					getSegment(context, mo), newValue);
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeSegmentDescriptor(
					getTarget(), getNewValue(), getOldValue());
		}
	}
	
	public static int getSegment(PortSpec p) {
		return getSegment(null, p);
	}

	public static int getSegment(PropertyScratchpad context, PortSpec p) {
		Integer i = getProperty(context, p, SEGMENT, Integer.class);
		if (i == null) {
			recalculatePosition(context, p);
			i = getProperty(context, p, SEGMENT, Integer.class);
		}
		return i;
	}

	@RedProperty(fired = Double.class, retrieved = Double.class)
	public static final String DISTANCE =
			"eD!+dk.itu.big_red.model.PortSpec.distance";
	
	private static void recalculatePosition(
			PropertyScratchpad context, PortSpec p) {
		int segment = 0;
		double distance = 0;
		
		Object shape = getShape(context, p.getControl(context));
		List<? extends PortSpec> l = p.getControl(context).getPorts(context);
		int index = l.indexOf(p) + 1;
		
		if (shape instanceof PointList) {
			PointList pl = (PointList)shape;
			int pls = pl.size();
			distance = (((double)pls) / l.size()) * index;
			
			segment = (int)Math.floor(distance);
			distance -= segment;
			segment %= pls;
		} else if (shape instanceof Ellipse) {
			segment = 0;
			distance = (1.0 / l.size()) * index;
		}
		
		setProperty(context, p, SEGMENT, segment);
		setProperty(context, p, DISTANCE, distance);
	}
	
	public static double getDistance(PortSpec p) {
		return getDistance(null, p);
	}

	public static double getDistance(
			PropertyScratchpad context, PortSpec p) {
		Double d = getProperty(context, p, DISTANCE, Double.class);
		if (d == null) {
			recalculatePosition(context, p);
			d = getProperty(context, p, DISTANCE, Double.class);
		}
		return d;
	}

	public static final class ChangeDistanceDescriptor
			extends ChangeExtendedDataDescriptor<PortSpec.Identifier, Double> {
		static {
			DescriptorExecutorManager.getInstance().addParticipant(
					new SimpleHandler(ChangeDistanceDescriptor.class));
		}
		
		public ChangeDistanceDescriptor(PortSpec.Identifier id,
				double oldValue, double newValue) {
			super(DISTANCE, id, oldValue, newValue);
		}
		
		public ChangeDistanceDescriptor(PropertyScratchpad context,
				PortSpec mo, double newValue) {
			this(mo.getIdentifier(context),
					getDistance(context, mo), newValue);
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeDistanceDescriptor(
					getTarget(), getNewValue(), getOldValue());
		}
	}

	@RedProperty(fired = Object.class, retrieved = Object.class)
	public static final String SHAPE =
			"eD!+dk.itu.big_red.model.Control.shape";
	
	public static final class ChangeShapeDescriptor
			extends ChangeExtendedDataDescriptor<
					Control.Identifier, Object> {
		static {
			DescriptorExecutorManager.getInstance().addParticipant(
					new SimpleHandler(ChangeShapeDescriptor.class));
		}
		
		public ChangeShapeDescriptor(Control.Identifier identifier,
				Object oldValue, Object newValue) {
			super(SHAPE, identifier, oldValue, newValue);
		}
		
		public ChangeShapeDescriptor(PropertyScratchpad context,
				Control mo, Object newValue) {
			this(mo.getIdentifier(context),
					getShapeRaw(context, mo), newValue);
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeShapeDescriptor(getTarget(),
					getNewValue(), getOldValue());
		}
	}
	
	public static Object getShape(Control c) {
		return getShape(null, c);
	}

	public static Object getShape(PropertyScratchpad context, Control c) {
		Object o = getShapeRaw(context, c);
		if (!(o instanceof PointList || o instanceof Ellipse)) {
			o = new Ellipse(new Rectangle(0, 0, 300, 300)).
				getPolygon(Math.max(3, c.getPorts(context).size()));
			setProperty(context, c, SHAPE, o);
			
			for (PortSpec p : c.getPorts(context))
				recalculatePosition(context, p);
		}
		return o;
	}

	public static Object getShapeRaw(PropertyScratchpad context, Control c) {
		return getProperty(context, c, SHAPE, Object.class);
	}

	@RedProperty(fired = String.class, retrieved = String.class)
	public static final String LABEL =
			"eD!+dk.itu.big_red.model.Control.label";
	
	public static final class ChangeLabelDescriptor
			extends ChangeExtendedDataDescriptor<
					Control.Identifier, String> {
		static {
			DescriptorExecutorManager.getInstance().addParticipant(
					new LabelHandler());
		}
		
		private static final class LabelHandler extends Handler {
			@Override
			public boolean tryValidateChange(Process context,
					IChangeDescriptor change) throws ChangeCreationException {
				final PropertyScratchpad scratch = context.getScratch();
				final Resolver resolver = context.getResolver();
				if (change instanceof ChangeLabelDescriptor) {
					ChangeLabelDescriptor cd = (ChangeLabelDescriptor)change;
					Control co = cd.getTarget().lookup(scratch, resolver);
					if (co == null)
						throw new ChangeCreationException(cd,
								"" + cd.getTarget() + ": lookup failed");
					
					String s = cd.getNormalisedNewValue(scratch, resolver);
					if (s.length() == 0)
						throw new ChangeCreationException(cd,
								"The label of " + cd.getTarget() +
								" must not be empty");
				} else return false;
				return true;
			}
			
			@Override
			public boolean executeChange(Resolver resolver,
					IChangeDescriptor change) {
				if (change instanceof ChangeLabelDescriptor) {
					ChangeLabelDescriptor cd = (ChangeLabelDescriptor)change;
					cd.getTarget().lookup(null, resolver).setExtendedData(
							LABEL, cd.getNormalisedNewValue(null, resolver));
				} else return false;
				return true;
			}
		}
		
		@Override
		protected String getNormalisedNewValue(
				PropertyScratchpad context, Resolver r) {
			String s = getNewValue();
			return (s != null ? s.trim() : null);
		}
		
		public ChangeLabelDescriptor(Control.Identifier identifier,
				String oldValue, String newValue) {
			super(LABEL, identifier, oldValue, newValue);
		}
		
		public ChangeLabelDescriptor(PropertyScratchpad context,
				Control mo, String newValue) {
			this(mo.getIdentifier(context),
					getLabel(context, mo), newValue);
		}
		
		@Override
		public IChangeDescriptor inverse() {
			return new ChangeLabelDescriptor(getTarget(),
					getNewValue(), getOldValue());
		}
	}
	
	public static String getLabel(Control c) {
		return getLabel(null, c);
	}

	public static String labelFor(String s) {
		return (s != null ? (s.length() > 0 ? s.substring(0, 1) : s) : "?");
	}

	public static String getLabel(PropertyScratchpad context, Control c) {
		String s = getProperty(context, c, LABEL, String.class);
		if (s == null)
			setProperty(context, c, LABEL, s = labelFor(c.getName(context)));
		return s;
	}
}
