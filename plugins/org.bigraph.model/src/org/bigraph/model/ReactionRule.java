package org.bigraph.model;

import java.util.HashMap;
import java.util.Map;

import org.bigraph.model.Layoutable.IChangeDescriptor;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Layoutable.ChangeDescriptorGroup;
import org.bigraph.model.Layoutable.ChangeRemove;
import org.bigraph.model.Point.ChangeDisconnect;
import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;

public class ReactionRule extends ModelObject {
	private Bigraph redex, reactum;
	private ChangeDescriptorGroup changes;
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
			if (redexObject instanceof Layoutable &&
					reactumObject instanceof Layoutable) {
				Layoutable reactumCandidate = null; /* XXX FIXME */
					//getReactumObject(getReactum(), (Layoutable)redexObject);
				return (reactumCandidate == reactumObject);
			} else return false;
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
	
	@Override
	public ReactionRule clone(Map<ModelObject, ModelObject> m) {
		if (m == null)
			m = new HashMap<ModelObject, ModelObject>();
		ReactionRule rr = (ReactionRule)super.clone(m);
		
		rr.setRedex(getRedex().clone(m));
		Bigraph reactum = getReactum().clone(m);
		rr.setReactum(getReactum().clone(m));
		
		Change c = getChanges().createChange(reactum, null);
		try {
			reactum.tryApplyChange(c);
			for (IChangeDescriptor d : getChanges())
				rr.getChanges().add(d);
		} catch (ChangeRejectedException cre) {
			throw new Error("Apparently valid change " + c +
					" rejected: shouldn't happen", cre);
		}
		
		return rr;
	}
	
	private void setReactum(Bigraph b) {
		reactum = b;
	}
	
	public ChangeDescriptorGroup getChanges() {
		if (changes == null)
			changes = new ChangeDescriptorGroup();
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
