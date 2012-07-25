package dk.itu.big_red.model.load_save.savers;

import java.io.IOException;
import java.io.OutputStreamWriter;

import org.bigraph.model.Bigraph;
import org.bigraph.model.ModelObject;
import org.bigraph.model.interfaces.IBigraph;
import org.bigraph.model.interfaces.IControl;
import org.bigraph.model.interfaces.IEdge;
import org.bigraph.model.interfaces.IEntity;
import org.bigraph.model.interfaces.IInnerName;
import org.bigraph.model.interfaces.INode;
import org.bigraph.model.interfaces.IOuterName;
import org.bigraph.model.interfaces.IPoint;
import org.bigraph.model.interfaces.IPort;
import org.bigraph.model.interfaces.IRoot;
import org.bigraph.model.interfaces.ISite;
import org.bigraph.model.savers.SaveFailedException;
import org.bigraph.model.savers.Saver;

public class BigraphTraverseSaver extends Saver {
	private OutputStreamWriter osw = null;
	
	@Override
	public Bigraph getModel() {
		return (Bigraph)super.getModel();
	}
	
	@Override
	public BigraphTraverseSaver setModel(ModelObject model) {
		if (model == null || model instanceof Bigraph)
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
			return "port " + ((IPort)e).getName() + " on " + niceName(((IPort)e).getNode());
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
		for (IPoint p : edge.getPoints())
			line(niceName(p));
	}
	
	public void process(IOuterName outerName) throws SaveFailedException {
		for (IPoint p : outerName.getPoints())
			line(niceName(p));
	}
	
	public void process(INode node) throws SaveFailedException {
		line("control " + node.getControl().getName());
		for (INode p : node.getNodes()) {
			line(niceName(p));
			scope++;
			process(p);
			scope--;
		}
		for (ISite s : node.getSites())
			line(niceName(s));
	}
	
	public void process(IRoot root) throws SaveFailedException {
		for (INode n : root.getNodes()) {
			line(niceName(n));
			scope++;
			process(n);
			scope--;
		}
		for (ISite s : root.getSites())
			line(niceName(s));
	}
	
	public void process(IControl c) throws SaveFailedException {
		line("control " + c.getName());
		scope++;
		for (IPort p : c.getPorts())
			line("port " + p.getName());
		scope--;
	}
	
	public void process(IBigraph bigraph) throws SaveFailedException {
		line("signature");
		scope++;
		for (IControl c : bigraph.getSignature().getControls())
			process(c);
		scope--;
		for (IEdge e : bigraph.getEdges()) {
			line(niceName(e));
			scope++;
			process(e);
			scope--;
		}
		for (IOuterName o : bigraph.getOuterNames()) {
			line(niceName(o));
			scope++;
			process(o);
			scope--;
		}
		for (IRoot r : bigraph.getRoots()) {
			line(niceName(r));
			scope++;
			process(r);
			scope--;
		}
		for (IInnerName i : bigraph.getInnerNames())
			line(niceName(i));
	}
}
