package org.bigraph.model.assistants;

import org.bigraph.model.Layoutable;
import org.bigraph.model.NamedModelObject.ChangeNameDescriptor;
import org.bigraph.model.Point;
import org.bigraph.model.Port;
import org.bigraph.model.Container.ChangeAddChildDescriptor;
import org.bigraph.model.Container.ChangeRemoveChildDescriptor;
import org.bigraph.model.Point.ChangeConnectDescriptor;
import org.bigraph.model.Point.ChangeDisconnectDescriptor;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

public abstract class DescriptorConflicts {
	private DescriptorConflicts() {}
	
	public interface IConflict {
		public boolean run(Object a, Object b);
	}
	
	private static abstract class Conflict<
			T extends IChangeDescriptor, V extends IChangeDescriptor>
					implements IConflict {
		private final Class<T> tK;
		private final Class<V> vK;
		
		private Conflict(Class<T> tK, Class<V> vK) {
			this.tK = tK;
			this.vK = vK;
		}
		
		public abstract boolean conflicts(T a, V b);
		
		@Override
		public boolean run(Object a, Object b) {
			T tInstance = tK.cast(
					tK.isInstance(a) ? a : (tK.isInstance(b) ? b : null));
			V vInstance = vK.cast(
					vK.isInstance(b) ? b : (vK.isInstance(a) ? a : null));
			if (tInstance != null && vInstance != null &&
					tInstance != vInstance) {
				return conflicts(tInstance, vInstance);
			} else return false;
		}
		
		@Override
		public String toString() {
			return "Conflict(" + tK.getSimpleName() +
					", " + vK.getSimpleName() + ")";
		}
	}
	
	private static abstract class Conflict1<T extends IChangeDescriptor>
			extends Conflict<T, T> {
		private Conflict1(Class<T> tK) {
			super(tK, tK);
		}
		
		@Override
		public final boolean conflicts(T a, T b) {
			return conflicts2(a, b) || conflicts2(b, a);
		}
		
		public abstract boolean conflicts2(T a, T b);
	}
	
	public static IConflict ADD_ADD = new Conflict1
			<ChangeAddChildDescriptor>
			(ChangeAddChildDescriptor.class) {
		@Override
		public boolean conflicts2(ChangeAddChildDescriptor a,
				ChangeAddChildDescriptor b) {
			return a.getChild().equals(b.getChild());
		}
	};
	
	public static IConflict ADD_REM = new Conflict
			<ChangeAddChildDescriptor, ChangeRemoveChildDescriptor>
			(ChangeAddChildDescriptor.class, ChangeRemoveChildDescriptor.class) {
		@Override
		public boolean conflicts(ChangeAddChildDescriptor a,
				ChangeRemoveChildDescriptor b) {
			return b.getChild().equals(a.getParent());
		}
	};
	
	public static IConflict ADD_REN = new Conflict
			<ChangeAddChildDescriptor, ChangeNameDescriptor>
			(ChangeAddChildDescriptor.class, ChangeNameDescriptor.class) {
		@Override
		public boolean conflicts(ChangeAddChildDescriptor a,
				ChangeNameDescriptor b) {
			return (a.getChild().equals(b.getTarget()) ||
					a.getParent().equals(b.getTarget()));
		}
	};
	
	public static IConflict REM_REM = new Conflict1
			<ChangeRemoveChildDescriptor>
			(ChangeRemoveChildDescriptor.class) {
		@Override
		public boolean conflicts2(ChangeRemoveChildDescriptor a,
				ChangeRemoveChildDescriptor b) {
			return (a.getChild().equals(b.getParent()));
		}
	};
	
	public static IConflict REM_CON = new Conflict
			<ChangeRemoveChildDescriptor, ChangeConnectDescriptor>
			(ChangeRemoveChildDescriptor.class, ChangeConnectDescriptor.class) {
		@Override
		public boolean conflicts(ChangeRemoveChildDescriptor a,
				ChangeConnectDescriptor b) {
			Layoutable.Identifier l = b.getPoint();
			if (l instanceof Port.Identifier)
				l = ((Port.Identifier)l).getNode();
			return (a.getChild().equals(l));
		}
	};
	
	public static IConflict REM_REN = new Conflict
			<ChangeRemoveChildDescriptor, ChangeNameDescriptor>
			(ChangeRemoveChildDescriptor.class, ChangeNameDescriptor.class) {
		@Override
		public boolean conflicts(ChangeRemoveChildDescriptor a,
				ChangeNameDescriptor b) {
			return a.getChild().equals(b.getTarget());
		}
	};
	
	public static IConflict CON_CON = new Conflict1
			<ChangeConnectDescriptor>
			(ChangeConnectDescriptor.class) {
		@Override
		public boolean conflicts2(ChangeConnectDescriptor a,
				ChangeConnectDescriptor b) {
			return a.getPoint().equals(b.getPoint());
		}
	};
	
	public static IConflict CON_REN = new Conflict
			<ChangeConnectDescriptor, ChangeNameDescriptor>
			(ChangeConnectDescriptor.class, ChangeNameDescriptor.class) {
		@Override
		public boolean conflicts(ChangeConnectDescriptor a,
				ChangeNameDescriptor b) {
			return a.getLink().equals(b.getTarget()) ||
					a.getPoint().equals(b.getTarget());
		}
	};
	
	public static IConflict DIS_REN = new Conflict
			<ChangeDisconnectDescriptor, ChangeNameDescriptor>
			(ChangeDisconnectDescriptor.class, ChangeNameDescriptor.class) {
		@Override
		public boolean conflicts(ChangeDisconnectDescriptor a,
				ChangeNameDescriptor b) {
			/* More */
			Point.Identifier point = a.getPoint();
			return (point.equals(b.getTarget()));
		}
	};
	
	public static IConflict REN_REN = new Conflict1
			<ChangeNameDescriptor>
			(ChangeNameDescriptor.class) {
		@Override
		public boolean conflicts2(ChangeNameDescriptor a,
				ChangeNameDescriptor b) {
			/* More */
			return a.getTarget().equals(b.getTarget());
		}
	};
}
