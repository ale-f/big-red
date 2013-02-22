package org.bigraph.model.tests;

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
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;

import static org.bigraph.model.tests.DescriptorTestRunner.cdg;

public class BigraphTests {
	private Signature signature;
	private Control control0;
	
	@Before
	public void createSignature()
			throws ChangeCreationException, ChangeCreationException {
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
	public void basicAdd() throws ChangeCreationException {
		Bigraph b = new Bigraph();
		Root.Identifier rid = new Root.Identifier("0");
		DescriptorExecutorManager.getInstance().tryApplyChange(b,
				new Container.ChangeAddChildDescriptor(
						new Bigraph.Identifier(), rid));
		
		assertNotNull("Root addition failed", rid.lookup(null, b));
	}
	
	@Test
	public void basicAddHierarchy() throws ChangeCreationException {
		Bigraph b = new Bigraph();
		b.setSignature(signature);
		
		Root.Identifier rid = new Root.Identifier("0");
		try {
			DescriptorExecutorManager.getInstance().tryApplyChange(b,
					new Container.ChangeAddChildDescriptor(
							new Bigraph.Identifier(), rid));
		} catch (ChangeCreationException e) {
			fail(e.getRationale());
		}
		
		ChangeDescriptorGroup cdg = new ChangeDescriptorGroup();
		Container.Identifier cid = rid;
		for (int i = 0; i < 1000; i++) {
			Node.Identifier nid = new Node.Identifier(
					Integer.toString(i), control0.getIdentifier());
			cdg.add(new Container.ChangeAddChildDescriptor(cid, nid));
			cid = nid;
		}
		
		DescriptorExecutorManager.getInstance().tryApplyChange(b, cdg);
	}
	
	@Test
	public void removeRoot() throws ChangeCreationException {
		Bigraph b = new Bigraph();
		Root.Identifier rid = new Root.Identifier("0");
		try {
			DescriptorExecutorManager.getInstance().tryApplyChange(b,
					new Container.ChangeAddChildDescriptor(
							new Bigraph.Identifier(), rid));
			assertNotNull("Root addition failed", rid.lookup(null, b));
		} catch (ChangeCreationException e) {
			fail(e.getRationale());
		}
		
		DescriptorExecutorManager.getInstance().tryApplyChange(b,
				new Container.ChangeRemoveChildDescriptor(
						new Bigraph.Identifier(), rid));
		
		assertNull("Root removal failed", rid.lookup(null, b));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void removeAbsentRoot() throws ChangeCreationException {
		DescriptorExecutorManager.getInstance().tryApplyChange(new Bigraph(),
				new Container.ChangeRemoveChildDescriptor(
						new Bigraph.Identifier(), new Root.Identifier("0")));
	}
	
	private static void tryAddAndConnect(
			Bigraph b, InnerName.Identifier in, Link.Identifier l)
			throws ChangeCreationException {
		DescriptorExecutorManager.getInstance().tryApplyChange(b, cdg(
				new Container.ChangeAddChildDescriptor(
						new Bigraph.Identifier(), in),
				new Container.ChangeAddChildDescriptor(
						new Bigraph.Identifier(), l),
				new Point.ChangeConnectDescriptor(in, l)));
		assertTrue(in.lookup(null, b).getLink() == l.lookup(null, b));
	}
	
	@Test
	public void connectInnerToEdge() throws ChangeCreationException {
		tryAddAndConnect(new Bigraph(),
				new InnerName.Identifier("a"), new Edge.Identifier("a"));
	}
	
	@Test
	public void connectInnerToOuter() throws ChangeCreationException {
		tryAddAndConnect(new Bigraph(),
				new InnerName.Identifier("a"), new OuterName.Identifier("a"));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void connectPointTwice() throws ChangeCreationException {
		Bigraph b = new Bigraph();
		try {
			DescriptorExecutorManager.getInstance().tryApplyChange(b, cdg(
					new Container.ChangeAddChildDescriptor(
							new Bigraph.Identifier(),
							new Edge.Identifier("e1")),
					new Container.ChangeAddChildDescriptor(
							new Bigraph.Identifier(),
							new Edge.Identifier("e2")),
					new Container.ChangeAddChildDescriptor(
							new Bigraph.Identifier(),
							new InnerName.Identifier("in")),
					new Point.ChangeConnectDescriptor(
							new InnerName.Identifier("in"),
							new Edge.Identifier("e1"))));
		} catch (ChangeCreationException e) {
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
		try {
			DescriptorExecutorManager.getInstance().tryApplyChange(b, cdg(
					new Container.ChangeAddChildDescriptor(
							new Bigraph.Identifier(),
							new Edge.Identifier("e1")),
					new Container.ChangeAddChildDescriptor(
							new Bigraph.Identifier(),
							new InnerName.Identifier("in"))));
		} catch (ChangeCreationException e) {
			fail(e.getRationale());
		}
		
		DescriptorTestRunner.run(b,
				new Point.ChangeDisconnectDescriptor(
						new InnerName.Identifier("in"),
						new Edge.Identifier("e1")));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void addDuplicateName() throws ChangeCreationException {
		Bigraph b = new Bigraph();
		DescriptorExecutorManager.getInstance().tryApplyChange(b, cdg(
				new Container.ChangeAddChildDescriptor(
						new Bigraph.Identifier(),
						new Root.Identifier("0")),
				new Container.ChangeAddChildDescriptor(
						new Bigraph.Identifier(),
						new Root.Identifier("0"))));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void addInvalidRootName() throws ChangeCreationException {
		DescriptorExecutorManager.getInstance().tryApplyChange(new Bigraph(),
				new Container.ChangeAddChildDescriptor(
						new Bigraph.Identifier(),
						new Root.Identifier("test")));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void setInvalidRootName() throws ChangeCreationException {
		Bigraph b = new Bigraph();
		Root.Identifier rid = new Root.Identifier("0");
		try {
			DescriptorExecutorManager.getInstance().tryApplyChange(b,
					new Container.ChangeAddChildDescriptor(
							new Bigraph.Identifier(), rid));
		} catch (ChangeCreationException e) {
			fail("Root insertion failed: " + e.getRationale());
		}
		DescriptorExecutorManager.getInstance().tryApplyChange(b,
				new NamedModelObject.ChangeNameDescriptor(rid, "test"));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void addInvalidSiteName() throws ChangeCreationException {
		Bigraph b = new Bigraph();
		Root.Identifier rid = new Root.Identifier("0");
		try {
			DescriptorExecutorManager.getInstance().tryApplyChange(b,
					new Container.ChangeAddChildDescriptor(
							new Bigraph.Identifier(), rid));
		} catch (ChangeCreationException e) {
			fail(e.getRationale());
		}
		DescriptorExecutorManager.getInstance().tryApplyChange(b,
				new Container.ChangeAddChildDescriptor(
						new Bigraph.Identifier(),
						new Site.Identifier("test")));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void setInvalidSiteName() throws ChangeCreationException {
		Bigraph b = new Bigraph();
		try {
			DescriptorExecutorManager.getInstance().tryApplyChange(b, cdg(
					new Container.ChangeAddChildDescriptor(
							new Bigraph.Identifier(),
							new Root.Identifier("0")),
					new Container.ChangeAddChildDescriptor(
							new Root.Identifier("0"),
							new Site.Identifier("0"))));
		} catch (ChangeCreationException e) {
			fail(e.getRationale());
		}
		DescriptorExecutorManager.getInstance().tryApplyChange(b,
				new NamedModelObject.ChangeNameDescriptor(
						new Site.Identifier("0"), "test"));
	}
	
	@Test(expected = ChangeCreationException.class)
	public void addSiteToBigraph() throws ChangeCreationException {
		DescriptorExecutorManager.getInstance().tryApplyChange(new Bigraph(),
				new Container.ChangeAddChildDescriptor(
						new Bigraph.Identifier(), new Site.Identifier("0")));
	}
}
