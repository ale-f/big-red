package org.bigraph.model;

import java.util.HashMap;
import java.util.Map;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Container.ChangeAddChild;
import org.bigraph.model.Layoutable.ChangeName;
import org.bigraph.model.Layoutable.ChangeRemove;
import org.bigraph.model.Point.ChangeConnect;
import org.bigraph.model.Point.ChangeDisconnect;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.names.INamespace;
import org.bigraph.model.names.Namespace;

public class ReactionRule extends ModelObject {
	private Bigraph redex, reactum;
	private ChangeGroup changes;
	public static final String CONTENT_TYPE = "dk.itu.big_red.rule";
	
	public Bigraph getRedex() {
		return redex;
	}
	
	public void setRedex(Bigraph redex) {
		this.redex = redex;
		
		reactum = null;
	}

	public Bigraph getReactum() {
		if (reactum == null)
			reactum = redex.clone(null);
		return reactum;
	}
	
	protected abstract class OperationRunner {
		protected abstract ChangeGroup runStepActual(
				Change redexChange, ChangeGroup reactumChanges,
				Change reactumChange);
		
		protected ChangeGroup runStep(
				Change redexChange, ChangeGroup reactumChanges) {
			ChangeGroup cg = null;
			for (int i = 0; i < reactumChanges.size(); i++) {
				Change c = reactumChanges.get(i);
				if (c instanceof ChangeGroup) {
					cg = runStep(redexChange, (ChangeGroup)c);
					if (cg != null) {
						reactumChanges = reactumChanges.clone();
						reactumChanges.set(i, cg);
						return reactumChanges;
					}
				} else {
					cg = runStepActual(redexChange, reactumChanges, c);
					if (cg != null)
						return cg;
				}
			}
			return null;
		}
		
		public ChangeGroup run(
				Change redexChange, ChangeGroup reactumChanges) {
			ChangeGroup cg = null;
			if (redexChange instanceof ChangeGroup) {
				ChangeGroup redexChanges = (ChangeGroup)redexChange;
				while (redexChanges.size() > 0) {
					Change head = redexChanges.head();
					cg = run(head, reactumChanges);
					if (cg != null)
						reactumChanges = cg;
					redexChanges = redexChanges.tail();
				}
			} else {
				cg = runStep(redexChange, reactumChanges);
				if (cg != null)
					reactumChanges = cg;
			}
			return reactumChanges;
		}
	}
	
	protected class Operation2Runner extends OperationRunner {
		protected boolean equalsUnder(
				ModelObject redexObject, ModelObject reactumObject) {
			return false /* XXX */;
		}
		
		protected boolean changesEqualUnder(
				Change redexChange_, Change reactumChange_) {
			if (!(redexChange_ instanceof ModelObjectChange &&
					reactumChange_ instanceof ModelObjectChange))
				return false;
			
			Class<? extends Change> sharedClass = redexChange_.getClass();
			if (!(reactumChange_.getClass().equals(sharedClass)))
				return false;
			
			ModelObjectChange
				redexChange = (ModelObjectChange)redexChange_,
				reactumChange = (ModelObjectChange)reactumChange_;
			if (equalsUnder(
					redexChange.getCreator(), reactumChange.getCreator())) {
				if (sharedClass.equals(ChangeRemove.class) ||
					sharedClass.equals(ChangeDisconnect.class)) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		protected ChangeGroup runStepActual(Change redexChange,
				ChangeGroup reactumChanges, Change reactumChange) {
			if (changesEqualUnder(redexChange, reactumChange)) {
				reactumChanges = reactumChanges.clone();
				reactumChanges.remove(reactumChange);
				return reactumChanges;
			} else return null;
		}
	}
	
	/**
	 * <strong>Do not call this method.</strong>
	 * @deprecated <strong>Do not call this method.</strong>
	 * @param redexChange an {@link Object}
	 * @param reactumChanges an {@link Object}
	 * @return an {@link Object}
	 */
	@Deprecated
	public ChangeGroup performOperation2(
			Change redexChange, ChangeGroup reactumChanges) {
		return new Operation2Runner().run(redexChange, reactumChanges);
	}
	
	/**
	 * Translates a {@link Change} targeted at the redex to the reactum.
	 * @param redexChange a {@link Change} targeted at the redex
	 * @return a {@link Change} targeted at the reactum, or {@link
	 * Change#INVALID} if the redex change no longer makes sense in the
	 * context of the reactum
	 */
	public Change getReactumChange(Change redexChange) {
		Change reactumChange = translateChange(getReactum(), redexChange);
		return (reactumChange != null ? reactumChange : Change.INVALID);
	}
	
	private static Layoutable getReactumObject(
			Bigraph reactum, Layoutable redexObject) {
		return getReactumObject(null, reactum, redexObject);
	}
	
	private static Layoutable getReactumObject(PropertyScratchpad context,
			Bigraph reactum, Layoutable redexObject) {
		Port p = null;
		if (redexObject instanceof Port) {
			p = (Port)redexObject;
			redexObject = p.getParent(context);
		}
		Namespace<Layoutable>
			reactumNamespace = reactum.getNamespace(
					Bigraph.getNSI(redexObject));
		if (reactumNamespace == null)
			return null;
		Layoutable
			counterpart = reactumNamespace.get(context, redexObject.getName());
		if (counterpart != null) {
			if (redexObject instanceof Node) {
				/* XXX */
				String
					redexControl = ((Node)redexObject).getControl().getName(),
					reactumControl =
						((Node)counterpart).getControl().getName();
				if (!redexControl.equals(reactumControl))
					return null;
			}
			if (p != null)
				counterpart = ((Node)counterpart).getPort(p.getName());
		}
		return counterpart;
	}
	
	public static Change translateChange(Bigraph reactum, Change change) {
		if (change instanceof ChangeGroup) {
			ChangeGroup cg_ = (ChangeGroup)change,
				cg = new ChangeGroup();
			for (Change i : cg_) {
				Change iP = translateChange(reactum, i);
				if (iP != null) {
					cg.add(iP);
				} else {
					cg.clear();
					return null;
				}
			}
			
			return cg;
		} else if (change instanceof ChangeAddChild) {
			ChangeAddChild ch = (ChangeAddChild)change;
			
			Container reactumParent =
					(Container)getReactumObject(reactum, ch.getCreator());
			Layoutable reactumChild = getReactumObject(reactum, ch.child);
			
			if (reactumParent == null)
				return null;
			if (reactumChild == null)
				reactumChild = ch.child.clone(null);
			
			/*
			 * XXX: a BigraphScratchpad should really be used here so that
			 * ChangeGroups will actually work properly
			 */
			String reactumName;
			INamespace<Layoutable> reactumNamespace =
				reactumParent.getBigraph().
				getNamespace(Bigraph.getNSI(reactumChild));
			if (reactumNamespace.get(ch.name) == null) {
				reactumName = ch.name;
			} else reactumName = reactumNamespace.getNextName();
			
			return reactumParent.changeAddChild(reactumChild, reactumName);
		} else if (change instanceof ChangeRemove) {
			ChangeRemove ch = (ChangeRemove)change;
			
			Layoutable reactumChild =
					getReactumObject(reactum, ch.getCreator());
			
			if (reactumChild == null)
				return null;
			
			return reactumChild.changeRemove();
		} else if (change instanceof ChangeName) {
			ChangeName ch = (ChangeName)change;
			
			Layoutable reactumModel =
					getReactumObject(reactum, ch.getCreator());
			if (reactumModel == null)
				return null;
			
			return reactumModel.changeName(ch.newName);
		} else if (change instanceof ChangeConnect) {
			ChangeConnect ch = (ChangeConnect)change;
			
			Point reactumPoint =
					(Point)getReactumObject(reactum, ch.getCreator());
			Link reactumLink =
					(Link)getReactumObject(reactum, ch.link);
			if (reactumPoint == null || reactumLink == null)
				return null;
			
			return reactumPoint.changeConnect(reactumLink);
		} else if (change instanceof ChangeDisconnect) {
			ChangeDisconnect ch = (ChangeDisconnect)change;
			
			Point reactumPoint =
					(Point)getReactumObject(reactum, ch.getCreator());
			if (reactumPoint == null)
				return null;
			
			return reactumPoint.changeDisconnect();
		} else if (change instanceof ChangeExtendedData) {
			ChangeExtendedData ch = (ChangeExtendedData)change;
			
			ModelObject reactumObject =
					getReactumObject(reactum, (Layoutable)ch.getCreator());
			if (reactumObject == null)
				return null;
			
			return reactumObject.changeExtendedData(
					ch.key, ch.newValue, ch.immediateValidator,
					ch.finalValidator);
		} else throw new Error(change + " unrecognised");
	}
	
	@Override
	public ReactionRule clone(Map<ModelObject, ModelObject> m) {
		if (m == null)
			m = new HashMap<ModelObject, ModelObject>();
		ReactionRule rr = (ReactionRule)super.clone(m);
		
		rr.setRedex(getRedex().clone(m));
		
		Change inv = getChanges().inverse();
		try {
			getReactum().tryApplyChange(inv);
			inv = null;
		} catch (ChangeRejectedException cre) {
			throw new Error(
					"Apparently valid change " + inv +
					" rejected: shouldn't happen", cre);
		}
		Bigraph rrr = getReactum().clone(m);
		rr.setReactum(rrr);
		
		ChangeGroup cg = rr.getChanges();
		for (Change c : getChanges()) {
			try {
				Change cP = translateChange(rrr, c);
				rrr.tryApplyChange(cP);
				cg.add(cP);
				
				getReactum().tryApplyChange(c);
			} catch (ChangeRejectedException cre) {
				throw new Error("Apparently valid change " + c +
						" rejected: shouldn't happen", cre);
			}
		}
		
		return rr;
	}
	
	private void setReactum(Bigraph b) {
		reactum = b;
	}
	
	public ChangeGroup getChanges() {
		if (changes == null)
			changes = new ChangeGroup();
		return changes;
	}
	
	@Override
	public void dispose() {
		if (redex != null) {
			redex.dispose();
			redex = null;
		}
		
		if (reactum != null) {
			reactum.dispose();
			reactum = null;
		}
		
		if (changes != null) {
			changes.clear();
			changes = null;
		}
		
		super.dispose();
	}
}
