package dk.itu.big_red.model.interfaces;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;

public class BigraphBuilder {
	private Bigraph b = new Bigraph();
	private ChangeGroup cg = new ChangeGroup();
	
	public BigraphBuilder() {
		
	}
	
	private final String FUN(Layoutable l) {
		return b.getFirstUnusedName(l);
	}
	
	public IEdge newEdge() {
		Edge e = new Edge();
		cg.add(b.changeAddChild(e),
				e.changeName(FUN(e)));
		return e;
	}
	
	public IOuterName newOuterName() {
		OuterName o = new OuterName();
		cg.add(b.changeAddChild(o),
				o.changeName(FUN(o)));
		return o;
	}
	
	public IInnerName newInnerName() {
		InnerName i = new InnerName();
		cg.add(b.changeAddChild(i),
				i.changeName(FUN(i)));
		return i;
	}
	
	public IRoot newRoot() {
		Root r = new Root();
		cg.add(b.changeAddChild(r),
				r.changeName(FUN(r)));
		return r;
	}
	
	public INode newNode(IParent parent, IControl c) {
		Node n = new Node((Control)c);
		cg.add(((Container)parent).changeAddChild(n),
				n.changeName(FUN(n)));
		return n;
	}
	
	public void newConnection(IPoint p, ILink l) {
		cg.add(((Point)p).changeConnect((Link)l));
	}
	
	public ISite newSite(IParent parent) {
		Site s = new Site();
		cg.add(((Container)parent).changeAddChild(s),
				s.changeName(FUN(s)));
		return s;
	}
	
	public IBigraph finish() {
		try {
			b.tryApplyChange(cg);
			b.tryApplyChange(b.relayout());
			return b;
		} catch (ChangeRejectedException e) {
			return null;
		}
	}
}