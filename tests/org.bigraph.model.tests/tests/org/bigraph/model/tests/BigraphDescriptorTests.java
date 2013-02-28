package org.bigraph.model.tests;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.InnerName;
import org.bigraph.model.NamedModelObject;
import org.bigraph.model.OuterName;
import org.bigraph.model.Point;
import org.bigraph.model.Root;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.junit.Test;

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
			throws ChangeCreationException {
		Bigraph.Identifier b = new Bigraph.Identifier();
		InnerName.Identifier in = new InnerName.Identifier("a");
		OuterName.Identifier on = new OuterName.Identifier("b");
		run(new Bigraph(),
				new Container.ChangeAddChildDescriptor(b, in),
				new Container.ChangeAddChildDescriptor(b, on),
				new Point.ChangeConnectDescriptor(in, on));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void connectInnerToOuterDescriptorTwice()
			throws ChangeCreationException {
		Bigraph.Identifier b = new Bigraph.Identifier();
		InnerName.Identifier in = new InnerName.Identifier("a");
		OuterName.Identifier on = new OuterName.Identifier("b");
		run(new Bigraph(),
				new Container.ChangeAddChildDescriptor(b, in),
				new Container.ChangeAddChildDescriptor(b, on),
				new Point.ChangeConnectDescriptor(in, on),
				new Point.ChangeConnectDescriptor(in, on));
	}
	
	@Test
	public void connectAndDisconnectInnerFromOuterDescriptor()
			throws ChangeCreationException {
		Bigraph.Identifier b = new Bigraph.Identifier();
		InnerName.Identifier in = new InnerName.Identifier("a");
		OuterName.Identifier on = new OuterName.Identifier("b");
		run(new Bigraph(),
				new Container.ChangeAddChildDescriptor(b, in),
				new Container.ChangeAddChildDescriptor(b, on),
				new Point.ChangeConnectDescriptor(in, on),
				new Point.ChangeDisconnectDescriptor(in, on));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void disconnectInnerFromOuterDescriptor()
			throws ChangeCreationException {
		Bigraph.Identifier b = new Bigraph.Identifier();
		InnerName.Identifier in = new InnerName.Identifier("a");
		OuterName.Identifier on = new OuterName.Identifier("b");
		run(new Bigraph(),
				new Container.ChangeAddChildDescriptor(b, in),
				new Container.ChangeAddChildDescriptor(b, on),
				new Point.ChangeDisconnectDescriptor(in, on));
	}
	
	@Test
	public void removeAddedRoot() throws ChangeCreationException {
		Bigraph.Identifier b = new Bigraph.Identifier();
		Root.Identifier r = new Root.Identifier("0");
		run(new Bigraph(),
				new Container.ChangeAddChildDescriptor(b, r),
				new Container.ChangeRemoveChildDescriptor(b, r));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void removeAbsentRoot() throws ChangeCreationException {
		run(new Bigraph(),
				new Container.ChangeRemoveChildDescriptor(
						new Bigraph.Identifier(), new Root.Identifier("0")));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void removeAddedRootTwice() throws ChangeCreationException {
		Bigraph.Identifier b = new Bigraph.Identifier();
		Root.Identifier r = new Root.Identifier("0");
		run(new Bigraph(),
				new Container.ChangeAddChildDescriptor(b, r),
				new Container.ChangeRemoveChildDescriptor(b, r),
				new Container.ChangeRemoveChildDescriptor(b, r));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void removeConnectedPoint() throws ChangeCreationException {
		Bigraph.Identifier b = new Bigraph.Identifier();
		InnerName.Identifier in = new InnerName.Identifier("a");
		OuterName.Identifier on = new OuterName.Identifier("a");
		run(new Bigraph(),
				new Container.ChangeAddChildDescriptor(b, in),
				new Container.ChangeAddChildDescriptor(b, on),
				new Point.ChangeConnectDescriptor(in, on),
				new Container.ChangeRemoveChildDescriptor(b, in));
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
