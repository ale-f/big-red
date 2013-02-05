package org.bigraph.model.tests;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.InnerName;
import org.bigraph.model.NamedModelObject;
import org.bigraph.model.OuterName;
import org.bigraph.model.Point;
import org.bigraph.model.Root;
import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.junit.Test;

import static org.bigraph.model.tests.BigraphTests.cg;

public class BigraphDescriptorTests extends DescriptorTestRunner {
	@Test
	public void addInnerAndOuterNames() throws ChangeCreationException {
		run(new Bigraph(),
				new Container.ChangeAddChildDescriptor(
						new Bigraph.Identifier(),
						new InnerName.Identifier("a")),
				new Container.ChangeAddChildDescriptor(
						new Bigraph.Identifier(),
						new OuterName.Identifier("a")));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void doubleRemoveInnerName() throws ChangeCreationException {
		run(new Bigraph(),
				new Container.ChangeAddChildDescriptor(
						new Bigraph.Identifier(),
						new InnerName.Identifier("a")),
				new Container.ChangeRemoveChildDescriptor(
						new Bigraph.Identifier(),
						new InnerName.Identifier("a")),
				new Container.ChangeRemoveChildDescriptor(
						new Bigraph.Identifier(),
						new InnerName.Identifier("a")));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void removeNotPresentInnerName() throws ChangeCreationException {
		run(new Bigraph(),
				new Container.ChangeRemoveChildDescriptor(
						new Bigraph.Identifier(),
						new InnerName.Identifier("a")));
	}
	
	@Test
	public void connectInnerToOuterDescriptor()
			throws ChangeRejectedException, ChangeCreationException {
		Bigraph b = new Bigraph();
		InnerName in = new InnerName();
		OuterName on = new OuterName();
		ExecutorManager.getInstance().tryApplyChange(cg(
				b.changeAddChild(in, "a"),
				b.changeAddChild(on, "b")));
		run(b,
				new Point.ChangeConnectDescriptor(
						in.getIdentifier(), on.getIdentifier()));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void connectInnerToOuterDescriptorTwice()
			throws ChangeRejectedException, ChangeCreationException {
		Bigraph b = new Bigraph();
		InnerName in = new InnerName();
		OuterName on = new OuterName();
		ExecutorManager.getInstance().tryApplyChange(cg(
				b.changeAddChild(in, "a"),
				b.changeAddChild(on, "b")));
		run(b,
				new Point.ChangeConnectDescriptor(
						in.getIdentifier(), on.getIdentifier()),
				new Point.ChangeConnectDescriptor(
						in.getIdentifier(), on.getIdentifier()));
	}
	
	@Test
	public void connectAndDisconnectInnerFromOuterDescriptor()
			throws ChangeRejectedException, ChangeCreationException {
		Bigraph b = new Bigraph();
		InnerName in = new InnerName();
		OuterName on = new OuterName();
		ExecutorManager.getInstance().tryApplyChange(cg(
				b.changeAddChild(in, "a"),
				b.changeAddChild(on, "b")));
		run(b,
				new Point.ChangeConnectDescriptor(
						in.getIdentifier(), on.getIdentifier()),
				new Point.ChangeDisconnectDescriptor(
						in.getIdentifier(), on.getIdentifier()));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void disconnectInnerFromOuterDescriptor()
			throws ChangeRejectedException, ChangeCreationException {
		Bigraph b = new Bigraph();
		InnerName in = new InnerName();
		OuterName on = new OuterName();
		ExecutorManager.getInstance().tryApplyChange(cg(
				b.changeAddChild(in, "a"),
				b.changeAddChild(on, "b")));
		run(b,
				new Point.ChangeDisconnectDescriptor(
						in.getIdentifier(), on.getIdentifier()));
	}
	
	@Test
	public void removeAddedRoot()
			throws ChangeRejectedException, ChangeCreationException {
		Bigraph b = new Bigraph();
		Root r = new Root();
		ExecutorManager.getInstance().tryApplyChange(b.changeAddChild(r, "0"));
		run(b,
				new Container.ChangeRemoveChildDescriptor(
						b.getIdentifier(), r.getIdentifier()));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void removeAbsentRoot()
			throws ChangeRejectedException, ChangeCreationException {
		Bigraph b = new Bigraph();
		run(new Bigraph(),
				new Container.ChangeRemoveChildDescriptor(
						b.getIdentifier(), new Root.Identifier("0")));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void removeAddedRootTwice()
			throws ChangeRejectedException, ChangeCreationException {
		Bigraph b = new Bigraph();
		Root r = new Root();
		ExecutorManager.getInstance().tryApplyChange(b.changeAddChild(r, "0"));
		run(b,
				new Container.ChangeRemoveChildDescriptor(
						b.getIdentifier(), r.getIdentifier()),
				new Container.ChangeRemoveChildDescriptor(
						b.getIdentifier(), r.getIdentifier()));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void removeConnectedPoint()
			throws ChangeRejectedException, ChangeCreationException {
		Bigraph b = new Bigraph();
		InnerName in = new InnerName();
		OuterName on = new OuterName();
		ExecutorManager.getInstance().tryApplyChange(cg(
				b.changeAddChild(in, "a"),
				b.changeAddChild(on, "a")));
		run(b,
				new Point.ChangeConnectDescriptor(
						new InnerName.Identifier("a"),
						new OuterName.Identifier("a")),
				new Container.ChangeRemoveChildDescriptor(
						b.getIdentifier(), in.getIdentifier()));
	}
	
	@Test
	public void renameInnerName() throws ChangeCreationException {
		run(new Bigraph(),
				new Container.ChangeAddChildDescriptor(
						new Bigraph.Identifier(),
						new InnerName.Identifier("a")),
				new NamedModelObject.ChangeNameDescriptor(
						new InnerName.Identifier("a"),
						"b"));
	}
	
	@Test
	public void renameAndRemoveInnerName() throws ChangeCreationException {
		run(new Bigraph(),
				new Container.ChangeAddChildDescriptor(
						new Bigraph.Identifier(),
						new InnerName.Identifier("a")),
				new NamedModelObject.ChangeNameDescriptor(
						new InnerName.Identifier("a"),
						"b"),
				new Container.ChangeRemoveChildDescriptor(
						new Bigraph.Identifier(),
						new InnerName.Identifier("b")));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void addButRenameAbsentInnerName() throws ChangeCreationException {
		run(new Bigraph(),
				new Container.ChangeAddChildDescriptor(
						new Bigraph.Identifier(),
						new InnerName.Identifier("a")),
				new NamedModelObject.ChangeNameDescriptor(
						new InnerName.Identifier("b"),
						"a"));
	}
}
