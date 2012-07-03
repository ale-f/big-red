package org.bigraph.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bigraph.model.ModelObject;
import org.bigraph.model.Container.ChangeAddChild;
import org.bigraph.model.Layoutable.ChangeName;
import org.bigraph.model.Layoutable.ChangeRemove;
import org.bigraph.model.Point.ChangeConnect;
import org.bigraph.model.Point.ChangeDisconnect;
import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.names.INamespace;

public class ReactionRule extends ModelObject {
	private Bigraph redex, reactum;
	private Map<ModelObject, ModelObject> redexToReactum =
		new HashMap<ModelObject, ModelObject>();
	private ChangeGroup changes;
	public static final String CONTENT_TYPE = "dk.itu.big_red.rule";
	
	public Bigraph getRedex() {
		return redex;
	}
	
	public void setRedex(Bigraph redex) {
		this.redex = redex;
		
		reactum = null;
		redexToReactum.clear();
	}

	public Bigraph getReactum() {
		if (reactum == null)
			reactum = redex.clone(redexToReactum);
		return reactum;
	}
	
	public Map<ModelObject, ModelObject> getRedexToReactumMap() {
		return redexToReactum;
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
			return (redexToReactum.get(redexObject) == reactumObject);
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
		Change reactumChange =
				translateChange(getRedexToReactumMap(), redexChange);
		return (reactumChange != null ? reactumChange : Change.INVALID);
	}
	
	public static Change translateChange(
			Map<ModelObject, ModelObject> oldToNew, Change change) {
		if (change instanceof ChangeGroup) {
			ChangeGroup cg_ = (ChangeGroup)change,
				cg = new ChangeGroup();
			for (Change i : cg_) {
				Change iP = translateChange(oldToNew, i);
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
			
			Container reactumParent = (Container)oldToNew.get(ch.getCreator());
			Layoutable reactumChild = (Layoutable)oldToNew.get(ch.child);
			
			if (reactumParent == null)
				return null;
			if (reactumChild == null)
				reactumChild = ch.child.clone(oldToNew);
			
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
					(Layoutable)oldToNew.get(ch.getCreator());
			
			if (reactumChild == null)
				return null;
			
			return reactumChild.changeRemove();
		} else if (change instanceof ChangeName) {
			ChangeName ch = (ChangeName)change;
			
			Layoutable reactumModel = (Layoutable)oldToNew.get(ch.getCreator());
			if (reactumModel == null)
				return null;
			
			return reactumModel.changeName(ch.newName);
		} else if (change instanceof ChangeConnect) {
			ChangeConnect ch = (ChangeConnect)change;
			
			Point reactumPoint = (Point)oldToNew.get(ch.getCreator());
			Link reactumLink = (Link)oldToNew.get(ch.link);
			if (reactumPoint == null || reactumLink == null)
				return null;
			
			return reactumPoint.changeConnect(reactumLink);
		} else if (change instanceof ChangeDisconnect) {
			ChangeDisconnect ch = (ChangeDisconnect)change;
			
			Point reactumPoint = (Point)oldToNew.get(ch.getCreator());
			if (reactumPoint == null)
				return null;
			
			return reactumPoint.changeDisconnect();
		} else if (change instanceof ChangeExtendedData) {
			ChangeExtendedData ch = (ChangeExtendedData)change;
			
			ModelObject reactumObject = oldToNew.get(ch.getCreator());
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
		
		Map<ModelObject, ModelObject>
			/* redex to reactum */
			rR = getRedexToReactumMap(),
			/* redex to cloned redex */
			rCr = new HashMap<ModelObject, ModelObject>(),
			/* reactum to cloned reactum */
			RCR = new HashMap<ModelObject, ModelObject>(),
			/* cloned redex to cloned reactum */
			CrCR = rr.getRedexToReactumMap();
		
		rr.setRedex(getRedex().clone(rCr));
		
		Change inv = getChanges().inverse();
		try {
			getReactum().tryApplyChange(inv);
			inv = null;
		} catch (ChangeRejectedException cre) {
			throw new Error(
					"Apparently valid change " + inv +
					" rejected: shouldn't happen", cre);
		}
		rr.setReactum(getReactum().clone(RCR));
		
		ChangeGroup cg = rr.getChanges();
		for (Change c : getChanges()) {
			try {
				Change cP = translateChange(RCR, c);
				rr.getReactum().tryApplyChange(cP);
				cg.add(cP);
				
				getReactum().tryApplyChange(c);
			} catch (ChangeRejectedException cre) {
				throw new Error("Apparently valid change " + c +
						" rejected: shouldn't happen", cre);
			}
		}
		
		for (Entry<ModelObject, ModelObject> e : rCr.entrySet())
			CrCR.put(e.getValue(), RCR.get(rR.get(e.getKey())));
		
		if (m != null) {
			m.putAll(rCr);
			m.putAll(RCR);
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
		redex.dispose();
		reactum.dispose();
		redex = reactum = null;
		redexToReactum.clear();
		redexToReactum = null;
		changes.clear();
		
		super.dispose();
	}
}
