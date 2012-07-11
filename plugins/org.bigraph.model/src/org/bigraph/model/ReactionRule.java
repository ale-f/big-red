package org.bigraph.model;

import java.util.HashMap;
import java.util.Iterator;
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
		
		protected final ChangeDescriptorGroup runStep(
				IChangeDescriptor redexCD, ChangeDescriptorGroup reactumCDs) {
			for (int i = 0; i < reactumCDs.size(); i++) {
				IChangeDescriptor c = reactumCDs.get(i);
				if (c instanceof ChangeDescriptorGroup) {
					ChangeDescriptorGroup cdg =
							runStep(redexCD, (ChangeDescriptorGroup)c);
					if (cdg != null) {
						reactumCDs = reactumCDs.clone();
						if (cdg.size() != 0) {
							reactumCDs.set(i, cdg);
						} else reactumCDs.remove(i);
						return reactumCDs;
					}
				} else return runStepActual(redexCD, reactumCDs, c);
			}
			return null;
		}
		
		public final ChangeDescriptorGroup run(
				ChangeDescriptorGroup linearisedRedexCDs,
				ChangeDescriptorGroup reactumCDs) {
			ChangeDescriptorGroup cdg;
			Iterator<IChangeDescriptor> it = linearisedRedexCDs.iterator();
			while (it.hasNext()) {
				IChangeDescriptor ice = it.next();
				cdg = runStep(ice, reactumCDs);
				if (cdg != null) {
					reactumCDs = cdg;
					it.remove();
				}
			}
			return reactumCDs;
		}
	}
	
	protected class Operation2Runner extends OperationRunner {
		@Override
		protected ChangeDescriptorGroup runStepActual(
				IChangeDescriptor redexCD, ChangeDescriptorGroup reactumCDs,
				IChangeDescriptor reactumCD) {
			boolean eq = redexCD.equals(reactumCD);
			System.out.println(redexCD + (eq ? " == " : " != ") + reactumCD);
			if (eq) {
				reactumCDs = reactumCDs.clone();
				reactumCDs.remove(reactumCD);
				return reactumCDs;
			} else return null;
		}
	}
	
	protected class Operation3PrimeRunner extends OperationRunner {
		private boolean conflicts(
				IChangeDescriptor redexCD, IChangeDescriptor reactumCD) {
			return false;
		}
		
		private IChangeDescriptor reverseSomehow(IChangeDescriptor cd) {
			return null;
		}
		
		@Override
		protected ChangeDescriptorGroup runStepActual(
				IChangeDescriptor redexCD, ChangeDescriptorGroup reactumCDs,
				IChangeDescriptor reactumCD) {
			if (conflicts(redexCD, reactumCD)) {
				reactumCDs = reactumCDs.clone();
				reactumCDs.prepend(reverseSomehow(redexCD));
				return reactumCDs;
			} else return null;
		}
	}
	
	/**
	 * <strong>Do not call this method.</strong>
	 * @deprecated <strong>Do not call this method.</strong>
	 * @param lRedexCDs an {@link Object}
	 * @param reactumCDs an {@link Object}
	 * @return an {@link Object}
	 */
	@Deprecated
	public ChangeDescriptorGroup performOperation2(
			ChangeDescriptorGroup lRedexCDs,
			ChangeDescriptorGroup reactumCDs) {
		return new Operation2Runner().run(lRedexCDs, reactumCDs);
	}
	
	/**
	 * <strong>Do not call this method.</strong>
	 * @deprecated <strong>Do not call this method.</strong>
	 * @param lRedexCDs an {@link Object}
	 * @param reactumCDs an {@link Object}
	 * @return an {@link Object}
	 */
	@Deprecated
	public ChangeDescriptorGroup performOperation3Prime(
			ChangeDescriptorGroup lRedexCDs,
			ChangeDescriptorGroup reactumCDs) {
		return new Operation3PrimeRunner().run(lRedexCDs, reactumCDs);
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
