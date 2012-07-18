package org.bigraph.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bigraph.model.ModelObject;
import org.bigraph.model.Container.ChangeAddChildDescriptor;
import org.bigraph.model.Layoutable.ChangeNameDescriptor;
import org.bigraph.model.Layoutable.ChangeRemoveDescriptor;
import org.bigraph.model.Point.ChangeConnectDescriptor;
import org.bigraph.model.Point.ChangeDisconnectDescriptor;
import org.bigraph.model.assistants.DescriptorConflicts;
import org.bigraph.model.assistants.DescriptorConflicts.IConflict;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

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
	
	protected static abstract class OperationRunner {
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
	
	protected static class Operation2Runner extends OperationRunner {
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
	
	private static final IConflict dcs[] = {
		DescriptorConflicts.ADD_ADD,
		DescriptorConflicts.ADD_REM,
		DescriptorConflicts.ADD_REN,
		DescriptorConflicts.REM_REM,
		DescriptorConflicts.REM_CON,
		DescriptorConflicts.REM_REN,
		DescriptorConflicts.CON_CON,
		DescriptorConflicts.CON_REN,
		DescriptorConflicts.DIS_REN,
		DescriptorConflicts.REN_REN
	};
	
	protected static class Operation3PrimeRunner extends OperationRunner {
		private boolean conflicts(
				IChangeDescriptor redexCD, IChangeDescriptor reactumCD) {
			for (IConflict i : dcs)
				if (i.run(redexCD, reactumCD))
					return true;
			return false;
		}
		
		private IChangeDescriptor reverse(IChangeDescriptor cd_) {
			if (cd_ instanceof ChangeAddChildDescriptor) {
				ChangeAddChildDescriptor cd = (ChangeAddChildDescriptor)cd_;
				return new ChangeRemoveDescriptor(
						cd.getChild(), cd.getParent());
			} else if (cd_ instanceof ChangeRemoveDescriptor) {
				ChangeRemoveDescriptor cd = (ChangeRemoveDescriptor)cd_;
				return new ChangeAddChildDescriptor(
						cd.getParent(), cd.getTarget());
			} else if (cd_ instanceof ChangeConnectDescriptor) {
				ChangeConnectDescriptor cd = (ChangeConnectDescriptor)cd_;
				return new ChangeDisconnectDescriptor(
						cd.getPoint(), cd.getLink());
			} else if (cd_ instanceof ChangeDisconnectDescriptor) {
				ChangeDisconnectDescriptor cd =
						(ChangeDisconnectDescriptor)cd_;
				return new ChangeConnectDescriptor(
						cd.getPoint(), cd.getLink());
			} else if (cd_ instanceof ChangeNameDescriptor) {
				ChangeNameDescriptor cd = (ChangeNameDescriptor)cd_;
				Layoutable.Identifier target = cd.getTarget();
				return new ChangeNameDescriptor(
						target.getRenamed(cd.getNewName()), target.getName());
			} else if (cd_ instanceof ChangeExtendedDataDescriptor) {
				return null /* aieee */;
			} else return null;
		}
		
		@Override
		protected ChangeDescriptorGroup runStepActual(
				IChangeDescriptor redexCD, ChangeDescriptorGroup reactumCDs,
				IChangeDescriptor reactumCD) {
			if (conflicts(redexCD, reactumCD)) {
				reactumCDs = reactumCDs.clone();
				reactumCDs.add(0, reverse(redexCD));
				return reactumCDs;
			} else return null;
		}
	}
	
	public static ChangeDescriptorGroup performFixups(
			ChangeDescriptorGroup lRedexCDs,
			ChangeDescriptorGroup reactumCDs) {
		return new Operation3PrimeRunner().run(lRedexCDs,
				new Operation2Runner().run(lRedexCDs, reactumCDs));
	}
	
	@Override
	public ReactionRule clone(Map<ModelObject, ModelObject> m) {
		if (m == null)
			m = new HashMap<ModelObject, ModelObject>();
		ReactionRule rr = (ReactionRule)super.clone(m);
		
		rr.setRedex(getRedex().clone(m));
		Bigraph reactum = getReactum().clone(m);
		rr.setReactum(getReactum().clone(m));
		
		IChange c = null;
		try {
			c = getChanges().createChange(null, reactum);
			reactum.tryApplyChange(c);
			for (IChangeDescriptor d : getChanges())
				rr.getChanges().add(d);
		} catch (ChangeCreationException cce) {
			throw new Error("BUG: reactum changes were completely invalid",
					cce);
		} catch (ChangeRejectedException cre) {
			throw new Error("BUG: reactum changes were slightly invalid", cre);
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
