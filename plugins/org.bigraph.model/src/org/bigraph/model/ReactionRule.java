package org.bigraph.model;

import java.util.HashMap;
import java.util.Map;

import org.bigraph.model.Layoutable.IChangeDescriptor;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Layoutable.ChangeDescriptorGroup;
import org.bigraph.model.changes.Change;
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
		protected abstract ChangeDescriptorGroup runStepActual(
				IChangeDescriptor redexCD, ChangeDescriptorGroup reactumCDs,
				IChangeDescriptor reactumCD);
		
		protected ChangeDescriptorGroup runStep(
				IChangeDescriptor redexCD, ChangeDescriptorGroup reactumCDs) {
			ChangeDescriptorGroup cdg = null;
			for (int i = 0; i < reactumCDs.size(); i++) {
				IChangeDescriptor c = reactumCDs.get(i);
				if (c instanceof ChangeDescriptorGroup) {
					cdg = runStep(redexCD, (ChangeDescriptorGroup)c);
					if (cdg != null) {
						reactumCDs = reactumCDs.clone();
						reactumCDs.set(i, cdg);
						return reactumCDs;
					}
				} else {
					cdg = runStepActual(redexCD, reactumCDs, c);
					if (cdg != null)
						return cdg;
				}
			}
			return null;
		}
		
		public ChangeDescriptorGroup run(
				IChangeDescriptor redexCD, ChangeDescriptorGroup reactumCDs) {
			ChangeDescriptorGroup cg = null;
			if (redexCD instanceof ChangeDescriptorGroup) {
				ChangeDescriptorGroup redexCDs =
						(ChangeDescriptorGroup)redexCD;
				while (redexCDs.size() > 0) {
					IChangeDescriptor head = redexCDs.head();
					cg = run(head, reactumCDs);
					if (cg != null)
						reactumCDs = cg;
					redexCDs = redexCDs.tail();
				}
			} else {
				cg = runStep(redexCD, reactumCDs);
				if (cg != null)
					reactumCDs = cg;
			}
			return reactumCDs;
		}
	}
	
	protected class Operation2Runner extends OperationRunner {
		@Override
		protected ChangeDescriptorGroup runStepActual(
				IChangeDescriptor redexCD, ChangeDescriptorGroup reactumCDs,
				IChangeDescriptor reactumCD) {
			if (redexCD.equals(reactumCD)) {
				reactumCDs = reactumCDs.clone();
				reactumCDs.remove(reactumCD);
				return reactumCDs;
			} else return null;
		}
	}
	
	/**
	 * <strong>Do not call this method.</strong>
	 * @deprecated <strong>Do not call this method.</strong>
	 * @param redexCD an {@link Object}
	 * @param reactumCDs an {@link Object}
	 * @return an {@link Object}
	 */
	@Deprecated
	public ChangeDescriptorGroup performOperation2(
			IChangeDescriptor redexCD, ChangeDescriptorGroup reactumCDs) {
		return new Operation2Runner().run(redexCD, reactumCDs);
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
