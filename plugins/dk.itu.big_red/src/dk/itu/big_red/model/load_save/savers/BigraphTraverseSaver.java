package dk.itu.big_red.model.load_save.savers;

import java.io.IOException;
import java.io.OutputStreamWriter;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.interfaces.IBigraph;
import dk.itu.big_red.model.interfaces.IControl;
import dk.itu.big_red.model.interfaces.IEdge;
import dk.itu.big_red.model.interfaces.IEntity;
import dk.itu.big_red.model.interfaces.IInnerName;
import dk.itu.big_red.model.interfaces.INode;
import dk.itu.big_red.model.interfaces.IOuterName;
import dk.itu.big_red.model.interfaces.IPoint;
import dk.itu.big_red.model.interfaces.IPort;
import dk.itu.big_red.model.interfaces.IRoot;
import dk.itu.big_red.model.interfaces.ISite;
import dk.itu.big_red.model.load_save.Saver;
import dk.itu.big_red.model.load_save.SaveFailedException;

public class BigraphTraverseSaver extends Saver {
	private OutputStreamWriter osw = null;
	
	@Override
	public Bigraph getModel() {
		return (Bigraph)super.getModel();
	}
	
	@Override
	public BigraphTraverseSaver setModel(ModelObject model) {
		if (model instanceof Bigraph)
			super.setModel(model);
		return this;
	}
	
	@Override
	public void exportObject() throws SaveFailedException {
		osw = new OutputStreamWriter(getOutputStream());
		process(getModel());
		try {
			osw.close();
		} catch (IOException e) {
			throw new SaveFailedException(e);
		}
	}
	
	private String niceName(IEntity e) {
		if (e instanceof IRoot)
			return "root " + ((IRoot)e).getName();
		else if (e instanceof INode)
			return "node " + ((INode)e).getName();
		else if (e instanceof IOuterName)
			return "outer name " + ((IOuterName)e).getName();
		else if (e instanceof IEdge)
			return "edge " + ((IEdge)e).getName();
		else if (e instanceof IInnerName)
			return "inner name " + ((IInnerName)e).getName();
		else if (e instanceof IPort)
			return "port " + ((IPort)e).getName() + " on " + niceName(((IPort)e).getINode());
		else return e.toString();
	}
	
	int scope = 0;
	
	private void line(Object line) throws SaveFailedException {
		try {
			for (int i = 0; i < scope; i++)
				osw.write("  ");
			osw.write(line.toString() + "\n");
		} catch (IOException e) {
			throw new SaveFailedException(e);
		}
	}
	
	public void process(IEdge edge) throws SaveFailedException {
		for (IPoint p : edge.getIPoints())
			line(niceName(p));
	}
	
	public void process(IOuterName outerName) throws SaveFailedException {
		for (IPoint p : outerName.getIPoints())
			line(niceName(p));
	}
	
	public void process(INode node) throws SaveFailedException {
		line("control " + node.getIControl().getName());
		for (INode p : node.getINodes()) {
			line(niceName(p));
			scope++;
			process(p);
			scope--;
		}
		for (ISite s : node.getISites())
			line(niceName(s));
	}
	
	public void process(IRoot root) throws SaveFailedException {
		for (INode n : root.getINodes()) {
			line(niceName(n));
			scope++;
			process(n);
			scope--;
		}
		for (ISite s : root.getISites())
			line(niceName(s));
	}
	
	public void process(IControl c) throws SaveFailedException {
		line("control " + c.getName());
		scope++;
		for (IPort p : c.getIPorts())
			line("port " + p.getName());
		scope--;
	}
	
	public void process(IBigraph bigraph) throws SaveFailedException {
		line("signature");
		scope++;
		for (IControl c : bigraph.getISignature().getIControls())
			process(c);
		scope--;
		for (IEdge e : bigraph.getIEdges()) {
			line(niceName(e));
			scope++;
			process(e);
			scope--;
		}
		for (IOuterName o : bigraph.getIOuterNames()) {
			line(niceName(o));
			scope++;
			process(o);
			scope--;
		}
		for (IRoot r : bigraph.getIRoots()) {
			line(niceName(r));
			scope++;
			process(r);
			scope--;
		}
		for (IInnerName i : bigraph.getIInnerNames())
			line(niceName(i));
	}
}
