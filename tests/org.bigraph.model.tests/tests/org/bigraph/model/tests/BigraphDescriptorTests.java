package org.bigraph.model.tests;

import java.util.Arrays;

import org.bigraph.model.Bigraph;
import org.bigraph.model.InnerName;
import org.bigraph.model.OuterName;
import org.bigraph.model.Point;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;
import org.bigraph.model.changes.descriptors.experimental.DescriptorExecutorManager;
import org.junit.Test;

import static org.bigraph.model.tests.BigraphTests.cg;

public class BigraphDescriptorTests {
	static ChangeDescriptorGroup cdg(IChangeDescriptor... changes) {
		return new ChangeDescriptorGroup(Arrays.asList(changes));
	}
	
	@Test
	public void connectInnerToOuterDescriptor()
			throws ChangeRejectedException, ChangeCreationException {
		Bigraph b = new Bigraph();
		InnerName in = new InnerName();
		OuterName on = new OuterName();
		b.tryApplyChange(cg(
				b.changeAddChild(in, "a"),
				b.changeAddChild(on, "b")));
		DescriptorExecutorManager.getInstance().tryApplyChange(b,
				new Point.ChangeConnectDescriptor(
						in.getIdentifier(), on.getIdentifier()));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void connectInnerToOuterDescriptorTwice()
			throws ChangeRejectedException, ChangeCreationException {
		Bigraph b = new Bigraph();
		InnerName in = new InnerName();
		OuterName on = new OuterName();
		b.tryApplyChange(cg(
				b.changeAddChild(in, "a"),
				b.changeAddChild(on, "b")));
		DescriptorExecutorManager.getInstance().tryApplyChange(b, cdg(
				new Point.ChangeConnectDescriptor(
						in.getIdentifier(), on.getIdentifier()),
				new Point.ChangeConnectDescriptor(
						in.getIdentifier(), on.getIdentifier())));
	}
	
	@Test
	public void connectAndDisconnectInnerFromOuterDescriptor()
			throws ChangeRejectedException, ChangeCreationException {
		Bigraph b = new Bigraph();
		InnerName in = new InnerName();
		OuterName on = new OuterName();
		b.tryApplyChange(cg(
				b.changeAddChild(in, "a"),
				b.changeAddChild(on, "b")));
		DescriptorExecutorManager.getInstance().tryApplyChange(b, cdg(
				new Point.ChangeConnectDescriptor(
						in.getIdentifier(), on.getIdentifier()),
				new Point.ChangeDisconnectDescriptor(
						in.getIdentifier(), on.getIdentifier())));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void disconnectInnerFromOuterDescriptor()
			throws ChangeRejectedException, ChangeCreationException {
		Bigraph b = new Bigraph();
		InnerName in = new InnerName();
		OuterName on = new OuterName();
		b.tryApplyChange(cg(
				b.changeAddChild(in, "a"),
				b.changeAddChild(on, "b")));
		DescriptorExecutorManager.getInstance().tryApplyChange(b,
				new Point.ChangeDisconnectDescriptor(
						in.getIdentifier(), on.getIdentifier()));
	}
}
