package org.bigraph.model.tests;

import static org.junit.Assert.*;

import org.bigraph.model.ModelObject;
import org.bigraph.model.ModelObject.Identifier.Resolver;
import org.bigraph.model.NamedModelObject;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.changes.descriptors.IDescriptorStepExecutor;
import org.bigraph.model.changes.descriptors.IDescriptorStepValidator;
import org.bigraph.model.process.IParticipantHost;
import org.junit.Test;

public class ExecutionTests {
	private static class Dummy extends ModelObject implements Resolver {
		public static final String PROPERTY_STRING = "DummyString";
		
		private String string;
		
		public String getString() {
			return string;
		}
		
		public String getString(PropertyScratchpad context) {
			return getProperty(context, PROPERTY_STRING, String.class);
		}
		
		@Override
		protected Object getProperty(String name) {
			if (PROPERTY_STRING.equals(name)) {
				return getString();
			} else return null;
		}
		
		private static final class Identifier
				implements ModelObject.Identifier {
			@Override
			public Dummy lookup(PropertyScratchpad context, Resolver r) {
				return NamedModelObject.require(
						r.lookup(context, this), Dummy.class);
			}
			
			@Override
			public String toString() {
				return "Dummy";
			}
		}
		
		@Override
		public Object lookup(PropertyScratchpad context,
				org.bigraph.model.ModelObject.Identifier identifier) {
			if (identifier instanceof Dummy.Identifier) {
				return this;
			} else return null;
		}
		
		private static class ChangeDescriptor
				extends ModelObjectChangeDescriptor {
			private final Identifier target;
			private final String oldString, newString;
			
			public ChangeDescriptor(
					Identifier target, String oldString, String newString) {
				this.target = target;
				this.oldString = oldString;
				this.newString = newString;
			}
			
			public Identifier getTarget() {
				return target;
			}
			
			public String getOldString() {
				return oldString;
			}
			
			public String getNewString() {
				return newString;
			}
			
			@Override
			public IChangeDescriptor inverse() {
				return new ChangeDescriptor(
						getTarget(), getNewString(), getOldString());
			}
			
			@Override
			public void simulate(PropertyScratchpad context, Resolver r) {
				Dummy self = getTarget().lookup(context, r);
				context.setProperty(self, PROPERTY_STRING, getNewString());
			}
			
			@Override
			public String toString() {
				return "ChangeDescriptor(set string of " + getTarget() +
						" from " + getOldString() + " to " + getNewString() +
						")";
			}
		}
	}
	
	private final class DummyHandler
			implements IDescriptorStepValidator, IDescriptorStepExecutor {
		@Override
		public void setHost(IParticipantHost host) {
			/* do nothing */
		}
		
		@Override
		public boolean executeChange(Resolver r, IChangeDescriptor change_) {
			if (change_ instanceof Dummy.ChangeDescriptor) {
				Dummy.ChangeDescriptor cd = (Dummy.ChangeDescriptor)change_;
				cd.getTarget().lookup(null, r).string =
						cd.getNewString();
			} else return false;
			return true;
		}
		
		@Override
		public boolean tryValidateChange(
				Process context, IChangeDescriptor change_)
				throws ChangeCreationException {
			final PropertyScratchpad scratch = context.getScratch();
			final Resolver resolver = context.getResolver();
			if (change_ instanceof Dummy.ChangeDescriptor) {
				Dummy.ChangeDescriptor cd = (Dummy.ChangeDescriptor)change_;
				
				Dummy d = cd.getTarget().lookup(scratch, resolver);
				if (d == null)
					throw new ChangeCreationException(cd,
							"" + cd.getTarget() + ": lookup failed");
				
				if ("LOCKED".equals(d.getString(scratch)))
					throw new ChangeCreationException(cd,
							"" + cd.getTarget() + "'s string is locked");
			} else return false;
			return true;
		}
	}
	
	private static final Dummy.ChangeDescriptor makeDescriptor(
			String oldS, String newS) {
		return new Dummy.ChangeDescriptor(new Dummy.Identifier(), oldS, newS);
	}
	
	private static Dummy go(
			DescriptorExecutorManager em) throws ChangeCreationException {
		Dummy d = new Dummy();
		assertEquals(null, d.getString());
		em.tryApplyChange(d, makeDescriptor(null, "value"));
		assertEquals("value", d.getString());
		return d;
	}
	
	@Test(expected = ChangeCreationException.class)
	public void blankManagers() throws ChangeCreationException {
		DescriptorExecutorManager em = new DescriptorExecutorManager();
		go(em);
	}
	
	@Test
	public void newManagers() throws ChangeCreationException {
		DescriptorExecutorManager em = new DescriptorExecutorManager();
		em.addParticipant(new DummyHandler());
		go(em);
	}
	
	@Test(expected = ChangeCreationException.class)
	public void testLock() throws ChangeCreationException {
		DescriptorExecutorManager em = new DescriptorExecutorManager();
		em.addParticipant(new DummyHandler());
		
		Dummy d = new Dummy();
		em.tryApplyChange(d, makeDescriptor(null, "LOCKED"));
		em.tryApplyChange(d, makeDescriptor("LOCKED", "value"));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void testLockScratch() throws ChangeCreationException {
		DescriptorExecutorManager em = new DescriptorExecutorManager();
		em.addParticipant(new DummyHandler());
		
		Dummy d = new Dummy();
		em.tryApplyChange(d, DescriptorTestRunner.cdg(
				makeDescriptor(null, "LOCKED"),
				makeDescriptor("LOCKED", "value")));
	}
	
	@Test
	public void stackedValidation() throws ChangeCreationException {
		DescriptorExecutorManager vm = new DescriptorExecutorManager();
		vm.addParticipant(DescriptorExecutorManager.getInstance());
		vm.addParticipant(new DummyHandler());
		
		vm.tryValidateChange(new Dummy(), makeDescriptor(null, "value"));
	}
	
	@Test
	public void stackedExecution() throws ChangeCreationException {
		DescriptorExecutorManager em = new DescriptorExecutorManager();
		em.addParticipant(DescriptorExecutorManager.getInstance());
		em.addParticipant(new DummyHandler());
		
		Dummy d = new Dummy();
		em.tryApplyChange(d, makeDescriptor(null, "value"));
		assertEquals("value", d.getString());
	}
}
