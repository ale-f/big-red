package dk.itu.big_red.model.import_export;

import java.io.IOException;
import java.io.OutputStreamWriter;

import dk.itu.big_red.exceptions.ExportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.interfaces.pure.IBigraph;
import dk.itu.big_red.model.interfaces.pure.IControl;
import dk.itu.big_red.model.interfaces.pure.IEdge;
import dk.itu.big_red.model.interfaces.pure.IEntity;
import dk.itu.big_red.model.interfaces.pure.IInnerName;
import dk.itu.big_red.model.interfaces.pure.INode;
import dk.itu.big_red.model.interfaces.pure.IOuterName;
import dk.itu.big_red.model.interfaces.pure.IPoint;
import dk.itu.big_red.model.interfaces.pure.IPort;
import dk.itu.big_red.model.interfaces.pure.IRoot;
import dk.itu.big_red.model.interfaces.pure.ISite;

public class BigraphTraverseExport extends ModelExport<Bigraph> {
	private OutputStreamWriter osw = null;
	
	@Override
	public void exportObject() throws ExportFailedException {
		osw = new OutputStreamWriter(target);
		process(model);
		try {
			osw.close();
		} catch (IOException e) {
			throw new ExportFailedException(e);
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
	
	private void line(Object line) throws ExportFailedException {
		try {
			for (int i = 0; i < scope; i++)
				osw.write("  ");
			osw.write(line.toString() + "\n");
		} catch (IOException e) {
			throw new ExportFailedException(e);
		}
	}
	
	public void process(IEdge edge) throws ExportFailedException {
		for (IPoint p : edge.getIPoints())
			line(niceName(p));
	}
	
	public void process(IOuterName outerName) throws ExportFailedException {
		for (IPoint p : outerName.getIPoints())
			line(niceName(p));
	}
	
	public void process(INode node) throws ExportFailedException {
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
	
	public void process(IRoot root) throws ExportFailedException {
		for (INode n : root.getINodes()) {
			line(niceName(n));
			scope++;
			process(n);
			scope--;
		}
		for (ISite s : root.getISites())
			line(niceName(s));
	}
	
	public void process(IControl c) throws ExportFailedException {
		line("control " + c.getName());
		scope++;
		for (IPort p : c.getIPorts())
			line("port " + p.getName());
		scope--;
	}
	
	public void process(IBigraph bigraph) throws ExportFailedException {
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
		for (IOuterName o : bigraph.getIOuterNames())
			line(niceName(o));
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
