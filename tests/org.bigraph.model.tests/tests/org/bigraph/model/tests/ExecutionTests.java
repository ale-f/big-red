package org.bigraph.model.tests;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.bigraph.model.ModelObject;
import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.ValidatorManager;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IStepExecutor;
import org.bigraph.model.changes.IStepValidator;
import org.junit.Test;

public class ExecutionTests {
	private class Dummy extends ModelObject {
		public static final String PROPERTY_STRING = "DummyString";
		
		private String string;
		
		public String getString() {
			return string;
		}
		
		public String getString(PropertyScratchpad context) {
			return (String)getProperty(context, PROPERTY_STRING);
		}
		
		private class Change extends ModelObjectChange {
			@Override
			public Dummy getCreator() {
				return Dummy.this;
			}
			
			private final String string;
			
			public Change(String string) {
				this.string = string;
			}
			
			public String getString() {
				return string;
			}
			
			private String oldString;
			@Override
			public void beforeApply() {
				oldString = getCreator().getString();
			}
			
			@Override
			public Change inverse() {
				return new Change(oldString);
			}
			
			@Override
			public void simulate(PropertyScratchpad context) {
				context.setProperty(getCreator(), PROPERTY_STRING, string);
			}
		}
		
		public IChange change(String string) {
			return new Change(string);
		}
	}
	
	private class DummyHandler implements IStepValidator, IStepExecutor {
		@Override
		public boolean executeChange(IChange change_) {
			if (change_ instanceof Dummy.Change) {
				Dummy.Change change = (Dummy.Change)change_;
				change.getCreator().string = change.getString();
			} else return false;
			return true;
		}
		
		@Override
		public boolean tryValidateChange(Process context, IChange change_)
				throws ChangeRejectedException {
			final PropertyScratchpad scratch = context.getScratch();
			if (change_ instanceof Dummy.Change) {
				Dummy.Change change = (Dummy.Change)change_;
				if ("LOCKED".equals(change.getCreator().getString(scratch)))
					throw new ChangeRejectedException(change_,
							"" + change.getCreator() + "'s string is locked");
			} else return false;
			return true;
		}
	}
	
	private Dummy go(ExecutorManager em) throws ChangeRejectedException {
		Dummy d = new Dummy();
		assertEquals(null, d.getString());
		em.tryApplyChange(d.change("value"));
		assertEquals("value", d.getString());
		return d;
	}
	
	@Test(expected = ChangeRejectedException.class)
	public void blankManagers() throws ChangeRejectedException {
		ExecutorManager em = new ExecutorManager();
		go(em);
	}
	
	@Test
	public void newManagers() throws ChangeRejectedException {
		DummyHandler dh = new DummyHandler();
		
		ExecutorManager em = new ExecutorManager();
		em.addExecutor(dh);
		em.addValidator(dh);
		
		go(em);
	}
	
	@Test
	public void stackedValidation() throws ChangeRejectedException {
		DummyHandler dh = new DummyHandler();
		
		ValidatorManager vm = new ValidatorManager();
		vm.addValidator(ExecutorManager.getInstance());
		vm.addValidator(dh);
		
		Dummy d = new Dummy();
		
		ChangeGroup cg = new ChangeGroup();
		cg.addAll(Arrays.asList(
				d.change("foxtrot"),
				d.changeExtendedData("eD!+test", Object.class)));
		vm.tryValidateChange(cg);
	}
	
	@Test
	public void stackedExecution() throws ChangeRejectedException {
		DummyHandler dh = new DummyHandler();
		
		ExecutorManager em = new ExecutorManager();
		em.addExecutor(ExecutorManager.getInstance());
		em.addExecutor(dh);
		em.addValidator(dh);
		
		Dummy d = new Dummy();
		
		ChangeGroup cg = new ChangeGroup();
		cg.addAll(Arrays.asList(
				d.change("foxtrot"),
				d.changeExtendedData("eD!+test", Object.class)));
		em.tryApplyChange(cg);
	}
}
