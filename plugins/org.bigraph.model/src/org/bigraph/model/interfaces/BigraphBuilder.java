package org.bigraph.model.interfaces;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Control;
import org.bigraph.model.Edge;
import org.bigraph.model.InnerName;
import org.bigraph.model.Link;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.Point;
import org.bigraph.model.Root;
import org.bigraph.model.Signature;
import org.bigraph.model.Site;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;

public class BigraphBuilder {
	private Bigraph b = new Bigraph();
	private ChangeGroup cg = new ChangeGroup();
	private PropertyScratchpad scratch = new PropertyScratchpad();
	
	private void addChange(IChange ch) {
		if (ch == null)
			return;
		cg.add(ch);
		getScratch().executeChange(ch);
	}
	
	private PropertyScratchpad getScratch() {
		return scratch;
	}
	
	public BigraphBuilder(ISignature s) {
		b.setSignature((Signature)s);
	}
	
	public IEdge newEdge(String name) {
		Edge e = new Edge();
		addChange(b.changeAddChild(e, name));
		return e;
	}
	
	public IOuterName newOuterName(String name) {
		OuterName o = new OuterName();
		addChange(b.changeAddChild(o, name));
		return o;
	}
	
	public IInnerName newInnerName(String name) {
		InnerName i = new InnerName();
		addChange(b.changeAddChild(i, name));
		return i;
	}
	
	public IRoot newRoot(String name) {
		Root r = new Root();
		addChange(b.changeAddChild(r, name));
		return r;
	}
	
	public INode newNode(IParent parent, IControl c, String name) {
		Node n = new Node((Control)c);
		addChange(((Container)parent).changeAddChild(n, name));
		return n;
	}
	
	public void newConnection(IPoint p, ILink l) {
		addChange(((Point)p).changeConnect((Link)l));
	}
	
	public ISite newSite(IParent parent, String name) {
		Site s = new Site();
		addChange(((Container)parent).changeAddChild(s, name));
		return s;
	}
	
	public Bigraph finish() {
		try {
			b.tryApplyChange(cg);
			return b;
		} catch (ChangeRejectedException e) {
			return null;
		}
	}
}