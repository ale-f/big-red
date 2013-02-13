package org.bigraph.model.tests;

import static org.junit.Assert.*;

import org.bigraph.model.ModelObject;
import org.bigraph.model.assistants.IObjectIdentifier;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.DescriptorValidatorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.changes.descriptors.IDescriptorStepExecutor;
import org.bigraph.model.changes.descriptors.IDescriptorStepValidator;
import org.bigraph.model.process.IParticipantHost;
import org.junit.Test;

public class ExecutionTests {
	/*
	 * As well as being a JUnit test, this file also serves as a
	 * heavily-documented example of how to create a Big Red model object.
	 */
	
	/*
	 * Big Red represents changes to its model objects as "change descriptors".
	 * Change descriptors are executed by descriptor executor managers, which
	 * encapsulate the process of validating and executing batches of changes.
	 * (Although there is a shared, process-wide manager, we won't be using it
	 * in order to make the tests more minimal.)
	 */
	private static final DescriptorExecutorManager MANAGER =
			new DescriptorExecutorManager();
	
	private static class Dummy extends ModelObject
			implements IObjectIdentifier.Resolver {
		/*
		 * Big Red's model is built around the model-view-controller pattern;
		 * when model properties change, interested parties are notified. This
		 * property name is used as part of that notification (and for other
		 * purposes which we'll see in a moment).
		 */
		public static final String PROPERTY_STRING = "DummyString";
		
		private String string;
		
		public String getString() {
			return string;
		}
		
		/*
		 * This extra getter is used in the validation process (which we'll
		 * see below). Essentially, this method allows clients to ask the
		 * question "Given this batch of changes to the universe, what is the
		 * value of your string?"
		 */
		public String getString(PropertyScratchpad context) {
			/*
			 * Property scratchpads are essentially maps from (ModelObject,
			 * String) pairs to Objects. This call to the getProperty method
			 * tries to find the Object associated with the (this,
			 * PROPERTY_STRING) pair, and casts it to a String. (If there's
			 * no such object, then the property is instead retrieved by
			 * passing its name to the _other_ getProperty method below.)
			 */
			return getProperty(context, PROPERTY_STRING, String.class);
		}
		
		/*
		 * Notice that this setter is protected -- clients can't use it. The
		 * only way to change this object's string is to build a change
		 * descriptor!
		 */
		protected void setString(String string) {
			String oldString = this.string;
			this.string = string;
			/*
			 * The property change notification is one of the most important
			 * reasons for the existence of property scratchpads -- we need to
			 * be able to track speculative changes to properties without
			 * sending any notifications (because, as a consequence of the
			 * validation process, any given change might not actually stick).
			 */
			firePropertyChange(PROPERTY_STRING, oldString, string);
		}
		
		/*
		 * This version of the getProperty method maps property names to
		 * property values. (It's really just a helper method for the other,
		 * more useful getProperty method.)
		 */
		@Override
		protected Object getProperty(String name) {
			if (PROPERTY_STRING.equals(name)) {
				return getString();
			} else return null;
		}
		
		/*
		 * Change descriptors represent modifications to model objects; they
		 * store a reference to an object and the details of the change. Those
		 * references aren't Java object references, though -- they're
		 * Identifiers, like this one here, which are then mapped to real
		 * model objects by Resolvers.
		 */
		private static final class Identifier
				implements ModelObject.Identifier {
			@Override
			public Dummy lookup(PropertyScratchpad context, Resolver r) {
				return ModelObject.require(
						r.lookup(context, this), Dummy.class);
			}
			
			/*
			 * (Although providing a toString implementation isn't strictly
			 * necessary, it makes the error messages clearer!)
			 */
			@Override
			public String toString() {
				return "Dummy";
			}
		}
		
		/*
		 * For convenience, Dummy is also a Resolver -- it returns itself
		 * whenever it's asked to look up a Dummy.Identifier.
		 */
		@Override
		public Object lookup(PropertyScratchpad context,
				IObjectIdentifier identifier) {
			if (identifier instanceof Dummy.Identifier) {
				return this;
			} else return null;
		}
		
		/*
		 * This class is a change descriptor: it represents a modification to
		 * a Dummy's string.
		 */
		private static class ChangeString
				extends ModelObjectChangeDescriptor {
			/*
			 * Classes that handle the execution and validation of change
			 * descriptors are called "handlers". As they can execute
			 * privileged code, they're not supposed to be visible to arbitrary
			 * clients -- instead, they're normally registered with a
			 * descriptor executor manager, which goes to great lengths not to
			 * leak references to them to the outside world.
			 * 
			 * The handler will be explained in a moment, but for now we're
			 * just going to make sure that it's guaranteed to be installed
			 * into our descriptor executor manager whenever we reference the
			 * change descriptor's class.
			 */
			static {
				MANAGER.addParticipant(new DummyHandler());
			}
			
			private final Identifier target;
			/*
			 * Change descriptors need to be reversible, so they must also
			 * store the old value of the string. (Sometimes the "old value"
			 * can be implicitly derived from the new one, of course!)
			 */
			private final String oldString, newString;
			
			public ChangeString(
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
				return new ChangeString(
						getTarget(), getNewString(), getOldString());
			}
			
			/*
			 * Simulating a change descriptor writes the change that it
			 * represents into a property scratchpad. (Although this method is
			 * normally used internally by the validation process, you can also
			 * call it yourself.)
			 */
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
	
	/*
	 * Here's the handler for our change descriptor.
	 * 
	 * The descriptor validation and descriptor execution processes both work
	 * in the same way: the manager dispatches the operation to all of its
	 * participants, and raises an exception if none of them could do anything.
	 */
	private static final class DummyHandler
			implements IDescriptorStepValidator, IDescriptorStepExecutor {
		@Override
		public void setHost(IParticipantHost host) {
			/* do nothing */
		}
		
		/*
		 * This method is called by the validation process; it returns true
		 * to indicate that it was able to validate the descriptor provided,
		 * false if it doesn't implement any behaviour for that kind of
		 * descriptor, and raises an exception in the event of a problem.
		 */
		@Override
		public boolean tryValidateChange(
				Process context, IChangeDescriptor change_)
				throws ChangeCreationException {
			/*
			 * The context object contains a property scratchpad, representing
			 * the changes to the universe made so far by the validation
			 * process, and a resolver.
			 * 
			 * (When it's passed to this method, the descriptor hasn't been
			 * simulated yet.)
			 */
			final PropertyScratchpad scratch = context.getScratch();
			final Resolver resolver = context.getResolver();
			if (change_ instanceof Dummy.ChangeString) {
				Dummy.ChangeString cd = (Dummy.ChangeString)change_;
				
				Dummy d = cd.getTarget().lookup(scratch, resolver);
				/*
				 * If our resolver can't actually find a Dummy for us, then
				 * we should give up with an appropriate error message.
				 */
				if (d == null)
					throw new ChangeCreationException(cd,
							"" + cd.getTarget() + ": lookup failed");
				
				/*
				 * In order to give us something to test later, here's some
				 * more sophisticated behaviour: if the string property is
				 * ever set to "LOCKED", then it can never be changed again.
				 * 
				 * (Notice that we use the special accessor to retrieve the
				 * string value -- perhaps a previous descriptor has already
				 * changed it!)
				 */
				if ("LOCKED".equals(d.getString(scratch)))
					throw new ChangeCreationException(cd,
							"" + cd.getTarget() + "'s string is locked");
			} else return false;
			return true;
		}
		
		/*
		 * The execution method is only called once the validation process has
		 * completed successfully, so it doesn't need to perform any extra
		 * validation. The meaning of the return value is just like that of the
		 * validation method.
		 */
		@Override
		public boolean executeChange(Resolver r, IChangeDescriptor change_) {
			if (change_ instanceof Dummy.ChangeString) {
				Dummy.ChangeString cd = (Dummy.ChangeString)change_;
				cd.getTarget().lookup(null, r).setString(cd.getNewString());
			} else return false;
			/*
			 * Descriptor executor managers raise an error if a descriptor was
			 * validated, but no executor was able to handle it, so it's
			 * important to return true at this point!
			 */
			return true;
		}
	}
	
	/*
	 * When this method returns, the Java Language Specification guarantees
	 * that our descriptor executor manager will have had a DummyHandler
	 * installed into it (due to Dummy.ChangeString's static initializer).
	 */
	private static final Dummy.ChangeString makeDescriptor(
			String oldS, String newS) {
		return new Dummy.ChangeString(new Dummy.Identifier(), oldS, newS);
	}
	
	private static Dummy go(
			DescriptorExecutorManager em) throws ChangeCreationException {
		Dummy d = new Dummy();
		assertEquals(null, d.getString());
		em.tryApplyChange(d, makeDescriptor(null, "value"));
		assertEquals("value", d.getString());
		return d;
	}
	
	@Test
	public void basicApply() throws ChangeCreationException {
		go(MANAGER);
	}
	
	/*
	 * This won't work -- if we create a new descriptor executor manager, then
	 * it won't have a DummyHandler, so it won't know how to handle a
	 * ChangeString.
	 */
	@Test(expected = ChangeCreationException.class)
	public void blankExecutor() throws ChangeCreationException {
		DescriptorExecutorManager em = new DescriptorExecutorManager();
		go(em);
	}
	
	/*
	 * On the other hand, this will work: we can create a new descriptor
	 * validator manager and add our previous instance to it as a participant.
	 * 
	 * We haven't mentioned descriptor validator managers yet, but their
	 * job is fairly obvious -- they implement the validation process. (In
	 * fact, DescriptorExecutorManager subclasses DescriptorValidatorManager in
	 * order to inherit that implementation.)
	 */
	@Test
	public void stackedValidation() throws ChangeCreationException {
		DescriptorValidatorManager vm = new DescriptorValidatorManager();
		vm.addParticipant(MANAGER);
		vm.tryValidateChange(new Dummy(), makeDescriptor(null, "value"));
	}
	
	/*
	 * We can even add the previous instance to a new descriptor executor
	 * manager. (By doing this, we could create a manager capable of executing
	 * descriptors for many different model objects.)
	 */
	@Test
	public void stackedExecution() throws ChangeCreationException {
		DescriptorExecutorManager em = new DescriptorExecutorManager();
		em.addParticipant(MANAGER);
		go(em);
	}
	
	/*
	 * As participants are added by reference, we can even retroactively add
	 * new functionality to old participants.
	 */
	@Test
	public void veryStackedExecution() throws ChangeCreationException {
		DescriptorExecutorManager
			em1 = new DescriptorExecutorManager(),
			em2 = new DescriptorExecutorManager();
		
		em2.addParticipant(em1);
		
		/*
		 * After this call, em2 will be able to handle ChangeStrings.
		 */
		em1.addParticipant(MANAGER);
		
		go(em2);
	}
	
	/*
	 * Locking the string property of our Dummy object means that validating
	 * the second change descriptor should fail.
	 */
	@Test(expected = ChangeCreationException.class)
	public void testLock() throws ChangeCreationException {
		Dummy d = new Dummy();
		MANAGER.tryApplyChange(d, makeDescriptor(null, "LOCKED"));
		MANAGER.tryApplyChange(d, makeDescriptor("LOCKED", "value"));
	}
	
	/*
	 * (And, because the validation method uses the special accessor for the
	 * string property, we should be able to cause the same rejection with a
	 * single call to the manager.)
	 */
	@Test(expected = ChangeCreationException.class)
	public void testLockScratch() throws ChangeCreationException {
		Dummy d = new Dummy();
		MANAGER.tryApplyChange(d, DescriptorTestRunner.cdg(
				makeDescriptor(null, "LOCKED"),
				makeDescriptor("LOCKED", "value")));
	}
}
