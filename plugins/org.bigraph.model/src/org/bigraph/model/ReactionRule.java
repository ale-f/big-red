package org.bigraph.model;

import java.util.Iterator;
import org.bigraph.model.ModelObject;
import org.bigraph.model.assistants.DescriptorConflicts;
import org.bigraph.model.assistants.DescriptorConflicts.IConflict;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

public class ReactionRule extends ModelObject {
	private Bigraph redex;
	private Edit edit;
	public static final String CONTENT_TYPE = "dk.itu.big_red.rule";
	
	public Bigraph getRedex() {
		return redex;
	}
	
	public void setRedex(Bigraph redex) {
		this.redex = redex;
	}
	
	public Bigraph createReactum() throws ChangeCreationException {
		Bigraph reactum = redex.clone();
		DescriptorExecutorManager.getInstance().tryApplyChange(
				reactum, getEdit());
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
				ChangeDescriptorGroup cdg = null;
				if (c instanceof ChangeDescriptorGroup) {
					cdg = runStep(redexCD, (ChangeDescriptorGroup)c);
					if (cdg != null) {
						/* c is a subgroup, so move cdg into a copy of c and
						 * return that */
						reactumCDs = reactumCDs.clone();
						if (cdg.size() != 0) {
							reactumCDs.set(i, cdg);
						} else reactumCDs.remove(i);
						return reactumCDs;
					}
				} else {
					cdg = runStepActual(redexCD, reactumCDs, c);
					if (cdg != null)
						/* c isn't a subgroup, so just return cdg directly */
						return cdg;
				}
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
			if (redexCD.equals(reactumCD)) {
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
		private static boolean conflicts(
				IChangeDescriptor redexCD, IChangeDescriptor reactumCD) {
			for (IConflict i : dcs)
				if (i.run(redexCD, reactumCD))
					return true;
			return false;
		}
		
		@Override
		protected ChangeDescriptorGroup runStepActual(
				IChangeDescriptor redexCD, ChangeDescriptorGroup reactumCDs,
				IChangeDescriptor reactumCD) {
			if (conflicts(redexCD, reactumCD)) {
				reactumCDs = reactumCDs.clone();
				reactumCDs.add(0, redexCD.inverse());
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
	public ReactionRule clone() {
		ReactionRule rr = (ReactionRule)super.clone();
		rr.setRedex(getRedex().clone());
		
		try {
			DescriptorExecutorManager.getInstance().tryApplyChange(
					rr.getRedex(), getEdit().getDescriptors());
			for (IChangeDescriptor d : getEdit().getDescriptors())
				rr.getEdit().getDescriptors().add(d);
		} catch (ChangeCreationException cce) {
			throw new RuntimeException(
					"BUG: reactum changes were completely invalid", cce);
		}
		
		return rr;
	}
	
	public Edit getEdit() {
		if (edit == null)
			edit = new Edit();
		return edit;
	}
	
	@Override
	public void dispose() {
		if (redex != null) {
			redex.dispose();
			redex = null;
		}
		
		if (edit != null) {
			edit.dispose();
			edit = null;
		}
		
		super.dispose();
	}
}
