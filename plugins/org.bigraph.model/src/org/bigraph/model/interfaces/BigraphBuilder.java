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
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.bigraph.model.changes.descriptors.IChangeDescriptor;

public class BigraphBuilder {
	private Bigraph b = new Bigraph();
	
	private void change(IChangeDescriptor ch) {
		try {
			DescriptorExecutorManager.getInstance().tryApplyChange(b, ch);
		} catch (ChangeCreationException cce) {
			/* do nothing */
		}
	}
	
	public BigraphBuilder(ISignature s) {
		b.setSignature((Signature)s);
	}
	
	public IEdge newEdge(String name) {
		Edge.Identifier eid = new Edge.Identifier(name);
		change(new Container.ChangeAddChildDescriptor(
				new Bigraph.Identifier(), eid));
		return eid.lookup(null, b);
	}
	
	public IOuterName newOuterName(String name) {
		OuterName.Identifier oid = new OuterName.Identifier(name);
		change(new Container.ChangeAddChildDescriptor(
				new Bigraph.Identifier(), oid));
		return oid.lookup(null, b);
	}
	
	public IInnerName newInnerName(String name) {
		InnerName.Identifier iid = new InnerName.Identifier(name);
		change(new Container.ChangeAddChildDescriptor(
				new Bigraph.Identifier(), iid));
		return iid.lookup(null, b);
	}
	
	public IRoot newRoot(String name) {
		Root.Identifier rid = new Root.Identifier(name);
		change(new Container.ChangeAddChildDescriptor(
				new Bigraph.Identifier(), rid));
		return rid.lookup(null, b);
	}
	
	public INode newNode(IParent parent, IControl c, String name) {
		Node.Identifier nid = new Node.Identifier(
				name, ((Control)c).getIdentifier());
		change(new Container.ChangeAddChildDescriptor(
				((Container)parent).getIdentifier(), nid));
		return nid.lookup(null, b);
	}
	
	public void newConnection(IPoint p, ILink l) {
		change(new Point.ChangeConnectDescriptor(
				((Point)p).getIdentifier(), ((Link)l).getIdentifier()));
	}
	
	public ISite newSite(IParent parent, String name) {
		Site.Identifier sid = new Site.Identifier(name);
		change(new Container.ChangeAddChildDescriptor(
				((Container)parent).getIdentifier(), sid));
		return sid.lookup(null, b);
	}
	
	public Bigraph finish() {
		return b;
	}
}