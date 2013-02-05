package org.bigraph.model.tests;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Control;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.Link;
import org.bigraph.model.NamedModelObject;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.Point;
import org.bigraph.model.PortSpec;
import org.bigraph.model.Root;
import org.bigraph.model.Signature;
import org.bigraph.model.Site;
import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.BoundDescriptor;
import org.bigraph.model.changes.descriptors.ChangeCreationException;

public class BigraphTests {
	private Signature signature;
	private Control control0;
	
	static ChangeGroup cg(IChange... changes) {
		return new ChangeGroup(Arrays.asList(changes));
	}
	
	@Before
	public void createSignature()
			throws ChangeCreationException, ChangeRejectedException {
		signature = new Signature();
		
		Control.Identifier
			cid0 = new Control.Identifier("c0"),
			cid1 = new Control.Identifier("c1");
		DescriptorTestRunner.run(signature,
				new Signature.ChangeAddControlDescriptor(
						new Signature.Identifier(), cid0),
				new Signature.ChangeAddControlDescriptor(
						new Signature.Identifier(), cid1),
				new Control.ChangeAddPortSpecDescriptor(
						new PortSpec.Identifier("p0", cid1)),
				new Control.ChangeAddPortSpecDescriptor(
						new PortSpec.Identifier("p1", cid1)));
		control0 = cid0.lookup(null, signature);
		assertNotNull(signature.getControl("c1"));
	}
	
	@Test
	public void basicAdd() throws ChangeRejectedException {
		Bigraph b = new Bigraph();
		Root r = new Root();
		ExecutorManager.getInstance().tryApplyChange(b.changeAddChild(r, "0"));
		
		assertTrue("Root addition failed", b.getChildren().size() == 1 &&
				b.getChildren().contains(r) && r.getName().equals("0"));
	}
	
	@Test
	public void basicAddHierarchy() throws ChangeRejectedException {
		Bigraph b = new Bigraph();
		b.setSignature(signature);
		
		Root r = new Root();
		try {
			ExecutorManager.getInstance().tryApplyChange(b.changeAddChild(r, "0"));
		} catch (ChangeRejectedException e) {
			fail(e.getRationale());
		}
		
		ChangeGroup cg = new ChangeGroup();
		Container c = r;
		for (int i = 0; i < 1000; i++) {
			Node n = new Node(control0);
			cg.add(
					c.changeAddChild(n, Integer.toString(i)));
			c = n;
		}
		
		ExecutorManager.getInstance().tryApplyChange(cg);
	}
	
	@Test
	public void removeRoot() throws ChangeRejectedException {
		Bigraph b = new Bigraph();
		Root r = new Root();
		try {
			ExecutorManager.getInstance().tryApplyChange(b.changeAddChild(r, "0"));
			assertTrue("Root addition failed",
					b.getChildren().size() == 1 &&
							b.getChildren().contains(r) &&
							r.getName().equals("0"));
		} catch (ChangeRejectedException e) {
			fail(e.getRationale());
		}
		
		ExecutorManager.getInstance().tryApplyChange(r.changeRemove());
		
		assertTrue("Root removal failed", b.getChildren().size() == 0);
	}
	
	@Test(expected = ChangeRejectedException.class)
	public void removeAbsentRoot() throws ChangeRejectedException {
		ExecutorManager.getInstance().tryApplyChange(new Root().changeRemove());
	}
	
	private static void tryAddAndConnect(Bigraph b, InnerName in, Link l)
			throws ChangeRejectedException {
		ExecutorManager.getInstance().tryApplyChange(cg(
				b.changeAddChild(l, "a"),
				b.changeAddChild(in, "a"),
				new BoundDescriptor(b,
						new Point.ChangeConnectDescriptor(
								new InnerName.Identifier("a"),
								l.getIdentifier().getRenamed("a")))));
		assertTrue(in.getLink().equals(l) && l.getPoints().contains(in));
	}
	
	@Test
	public void connectInnerToEdge() throws ChangeRejectedException {
		tryAddAndConnect(new Bigraph(), new InnerName(), new Edge());
	}
	
	@Test
	public void connectInnerToOuter() throws ChangeRejectedException {
		tryAddAndConnect(new Bigraph(), new InnerName(), new OuterName());
	}
	
	@Test(expected = ChangeCreationException.class)
	public void connectPointTwice() throws ChangeCreationException {
		Bigraph b = new Bigraph();
		Edge e1 = new Edge(), e2 = new Edge();
		InnerName in = new InnerName();
		try {
			ExecutorManager.getInstance().tryApplyChange(cg(
					b.changeAddChild(e1, "e1"),
					b.changeAddChild(e2, "e2"),
					b.changeAddChild(in, "in"),
					new BoundDescriptor(b,
							new Point.ChangeConnectDescriptor(
									new InnerName.Identifier("in"),
									new Edge.Identifier("e1")))));
		} catch (ChangeRejectedException e) {
			fail(e.getRationale());
		}
		
		DescriptorTestRunner.run(b,
				new Point.ChangeConnectDescriptor(
						new InnerName.Identifier("in"),
						new Edge.Identifier("e2")));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void disconnectUnconnectedPoint() throws ChangeCreationException {
		Bigraph b = new Bigraph();
		Edge e1 = new Edge();
		InnerName in = new InnerName();
		try {
			ExecutorManager.getInstance().tryApplyChange(cg(
					b.changeAddChild(e1, "e1"),
					b.changeAddChild(in, "in")));
		} catch (ChangeRejectedException e) {
			fail(e.getRationale());
		}
		
		DescriptorTestRunner.run(b,
				new Point.ChangeDisconnectDescriptor(
						new InnerName.Identifier("in"),
						new Edge.Identifier("e1")));
	}
	
	@Test(expected = ChangeRejectedException.class)
	public void addDuplicateName() throws ChangeRejectedException {
		Bigraph b = new Bigraph();
		Root
			r0 = new Root(),
			r1 = new Root();
		ExecutorManager.getInstance().tryApplyChange(cg(
				b.changeAddChild(r0, "0"),
				b.changeAddChild(r1, "0")));
	}
	
	@Test(expected = ChangeRejectedException.class)
	public void addInvalidRootName() throws ChangeRejectedException {
		Bigraph b = new Bigraph();
		ExecutorManager.getInstance().tryApplyChange(b.changeAddChild(new Root(), "test"));
	}
	
	@Test(expected = ChangeRejectedException.class)
	public void setInvalidRootName() throws ChangeRejectedException {
		Bigraph b = new Bigraph();
		Root r = new Root();
		try {
			ExecutorManager.getInstance().tryApplyChange(b.changeAddChild(r, "0"));
		} catch (ChangeRejectedException e) {
			fail("Root insertion failed: " + e.getRationale());
		}
		ExecutorManager.getInstance().tryApplyChange(new BoundDescriptor(b,
				new NamedModelObject.ChangeNameDescriptor(
						r.getIdentifier(), "test")));
	}
	
	@Test(expected = ChangeRejectedException.class)
	public void addInvalidSiteName() throws ChangeRejectedException {
		Bigraph b = new Bigraph();
		Root r = new Root();
		try {
			ExecutorManager.getInstance().tryApplyChange(b.changeAddChild(r, "0"));
		} catch (ChangeRejectedException e) {
			fail(e.getRationale());
		}
		ExecutorManager.getInstance().tryApplyChange(r.changeAddChild(new Site(), "test"));
	}
	
	@Test(expected = ChangeRejectedException.class)
	public void setInvalidSiteName() throws ChangeRejectedException {
		Bigraph b = new Bigraph();
		Root r = new Root();
		Site s = new Site();
		try {
			ExecutorManager.getInstance().tryApplyChange(cg(
					b.changeAddChild(r, "0"),
					r.changeAddChild(s, "0")));
		} catch (ChangeRejectedException e) {
			fail(e.getRationale());
		}
		ExecutorManager.getInstance().tryApplyChange(new BoundDescriptor(b,
				new NamedModelObject.ChangeNameDescriptor(
						s.getIdentifier(), "test")));
	}
	
	@Test(expected = ChangeRejectedException.class)
	public void addSiteToBigraph() throws ChangeRejectedException {
		Bigraph b = new Bigraph();
		ExecutorManager.getInstance().tryApplyChange(b.changeAddChild(new Site(), "0"));
	}
}
