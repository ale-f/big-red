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
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.PortSpec;
import org.bigraph.model.Root;
import org.bigraph.model.Signature;
import org.bigraph.model.Site;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;

public class BigraphTests {
	private Signature signature;
	private Control control0, control1;
	
	static ChangeGroup cg(IChange... changes) {
		return new ChangeGroup(Arrays.asList(changes));
	}
	
	@Before
	public void createSignature() throws ChangeRejectedException {
		signature = new Signature();
		
		control0 = new Control();
		signature.tryApplyChange(
				signature.changeAddControl(control0, "c0"));
		
		control1 = new Control();
		signature.tryApplyChange(cg(
				signature.changeAddControl(control1, "c1"),
				control1.changeAddPort(new PortSpec(), "p0"),
				control1.changeAddPort(new PortSpec(), "p1")));
	}
	
	@Test
	public void basicAdd() throws ChangeRejectedException {
		Bigraph b = new Bigraph();
		Root r = new Root();
		b.tryApplyChange(b.changeAddChild(r, "0"));
		
		assertTrue("Root addition failed", b.getChildren().size() == 1 &&
				b.getChildren().get(0).equals(r) && r.getName().equals("0"));
	}
	
	@Test
	public void basicAddHierarchy() throws ChangeRejectedException {
		Bigraph b = new Bigraph();
		b.setSignature(signature);
		
		Root r = new Root();
		try {
			b.tryApplyChange(b.changeAddChild(r, "0"));
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
		
		b.tryApplyChange(cg);
	}
	
	@Test
	public void removeRoot() throws ChangeRejectedException {
		Bigraph b = new Bigraph();
		Root r = new Root();
		try {
			b.tryApplyChange(b.changeAddChild(r, "0"));
			assertTrue("Root addition failed",
					b.getChildren().size() == 1 &&
							b.getChildren().get(0).equals(r) &&
							r.getName().equals("0"));
		} catch (ChangeRejectedException e) {
			fail(e.getRationale());
		}
		
		b.tryApplyChange(r.changeRemove());
		
		assertTrue("Root removal failed", b.getChildren().size() == 0);
	}
	
	@Test(expected = ChangeRejectedException.class)
	public void removeAbsentRoot() throws ChangeRejectedException {
		new Bigraph().tryApplyChange(new Root().changeRemove());
	}
	
	private void tryAddAndConnect(Bigraph b, InnerName in, Link l)
			throws ChangeRejectedException {
		b.tryApplyChange(cg(
				b.changeAddChild(l, "a"),
				b.changeAddChild(in, "a"),
				in.changeConnect(l)));
		assertTrue(in.getLink().equals(l) &&
				l.getPoints().contains(in));
	}
	
	@Test
	public void connectInnerToEdge() throws ChangeRejectedException {
		tryAddAndConnect(new Bigraph(), new InnerName(), new Edge());
	}
	
	@Test
	public void connectInnerToOuter() throws ChangeRejectedException {
		tryAddAndConnect(new Bigraph(), new InnerName(), new OuterName());
	}
	
	@Test(expected = ChangeRejectedException.class)
	public void connectPointTwice() throws ChangeRejectedException {
		Bigraph b = new Bigraph();
		Edge e1 = new Edge(), e2 = new Edge();
		InnerName in = new InnerName();
		try {
			b.tryApplyChange(cg(
					b.changeAddChild(e1, "a"),
					b.changeAddChild(e2, "b"),
					b.changeAddChild(in, "a"),
					in.changeConnect(e1)));
		} catch (ChangeRejectedException e) {
			fail(e.getRationale());
		}
		
		b.tryApplyChange(in.changeConnect(e2));
	}
	
	@Test(expected = ChangeRejectedException.class)
	public void disconnectUnconnectedPoint() throws ChangeRejectedException {
		Bigraph b = new Bigraph();
		InnerName in = new InnerName();
		try {
			b.tryApplyChange(
					b.changeAddChild(in, "a"));
		} catch (ChangeRejectedException e) {
			fail(e.getRationale());
		}
		
		b.tryApplyChange(in.changeDisconnect());
	}
	
	@Test(expected = ChangeRejectedException.class)
	public void addDuplicateName() throws ChangeRejectedException {
		Bigraph b = new Bigraph();
		Root
			r0 = new Root(),
			r1 = new Root();
		b.tryApplyChange(cg(
				b.changeAddChild(r0, "0"),
				b.changeAddChild(r1, "0")));
	}
	
	@Test(expected = ChangeRejectedException.class)
	public void addInvalidRootName() throws ChangeRejectedException {
		Bigraph b = new Bigraph();
		b.tryApplyChange(b.changeAddChild(new Root(), "test"));
	}
	
	@Test(expected = ChangeRejectedException.class)
	public void setInvalidRootName() throws ChangeRejectedException {
		Bigraph b = new Bigraph();
		Root r = new Root();
		try {
			b.tryApplyChange(b.changeAddChild(r, "0"));
		} catch (ChangeRejectedException e) {
			fail("Root insertion failed: " + e.getRationale());
		}
		b.tryApplyChange(r.changeName("test"));
	}
	
	@Test(expected = ChangeRejectedException.class)
	public void addInvalidSiteName() throws ChangeRejectedException {
		Bigraph b = new Bigraph();
		Root r = new Root();
		try {
			b.tryApplyChange(b.changeAddChild(r, "0"));
		} catch (ChangeRejectedException e) {
			fail(e.getRationale());
		}
		b.tryApplyChange(r.changeAddChild(new Site(), "test"));
	}
	
	@Test(expected = ChangeRejectedException.class)
	public void setInvalidSiteName() throws ChangeRejectedException {
		Bigraph b = new Bigraph();
		Root r = new Root();
		Site s = new Site();
		try {
			b.tryApplyChange(cg(
					b.changeAddChild(r, "0"),
					r.changeAddChild(s, "0")));
		} catch (ChangeRejectedException e) {
			fail(e.getRationale());
		}
		b.tryApplyChange(s.changeName("test"));
	}
	
	@Test(expected = ChangeRejectedException.class)
	public void addSiteToBigraph() throws ChangeRejectedException {
		Bigraph b = new Bigraph();
		b.tryApplyChange(b.changeAddChild(new Site(), "0"));
	}
}
