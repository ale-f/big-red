package dk.itu.big_red.model.load_save.savers;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.SimulationSpec;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.interfaces.IChild;
import dk.itu.big_red.model.interfaces.IEdge;
import dk.itu.big_red.model.interfaces.ILink;
import dk.itu.big_red.model.interfaces.INode;
import dk.itu.big_red.model.interfaces.IOuterName;
import dk.itu.big_red.model.interfaces.IPort;
import dk.itu.big_red.model.interfaces.IRoot;
import dk.itu.big_red.model.interfaces.ISite;
import dk.itu.big_red.model.load_save.Saver;
import dk.itu.big_red.model.load_save.SaveFailedException;

public class SimulationSpecBigMCSaver extends Saver {
	private OutputStreamWriter osw = null;
	
	@Override
	public SimulationSpec getModel() {
		return (SimulationSpec)super.getModel();
	}
	
	@Override
	public SimulationSpecBigMCSaver setModel(ModelObject model) {
		if (model instanceof SimulationSpec)
			super.setModel(model);
		return this;
	}
	
	private void newline() throws SaveFailedException {
		write("\n");
	}
	
	private void write(String str) throws SaveFailedException {
		try {
			osw.write(str);
		} catch (IOException e) {
			throw new SaveFailedException(e);
		}
	}
	
	@Override
	public void exportObject() throws SaveFailedException {
		osw = new OutputStreamWriter(getOutputStream());
		processSimulationSpec(getModel());
		try {
			osw.close();
		} catch (IOException e) {
			throw new SaveFailedException(e);
		}
	}

	private void processSignature(Signature s) throws SaveFailedException {
		write("# Controls"); newline();
		for (Control c : s.getControls()) {
			switch (c.getKind()) {
			case ACTIVE:
			case ATOMIC:
			default:
				write("%active ");
				break;
			case PASSIVE:
				write("%passive ");
				break;
			}
			write(c.getName() + " : ");
			write(c.getPorts().size() + ";"); newline();
		}
		newline();
	}
	
	private void namesFrom(Bigraph b, ArrayList<String> bucket) {
		for (IOuterName o : b.getIOuterNames())
			if (!bucket.contains(o.getName()))
				bucket.add(o.getName());
		for (IEdge e : b.getIEdges())
			if (!bucket.contains(e.getName()))
				bucket.add(e.getName());
	}
	
	private void processNames(SimulationSpec s) throws SaveFailedException {
		ArrayList<String> names = new ArrayList<String>();
		for (ReactionRule r : s.getRules()) {
			namesFrom(r.getRedex(), names);
			namesFrom(r.getReactum(), names);
		}
		namesFrom(s.getModel(), names);
		Collections.sort(names);
		
		if (names.size() == 0)
			return;
		write("# Names"); newline();
		for (String name : names) {
			write("%name " + name + ";"); newline();
		}
		newline();
	}
	
	private String getPortString(ILink l) {
		return (l != null ? "n_" + l.getName() : "-");
	}
	
	private void processChild(IChild i) throws SaveFailedException {
		if (i instanceof ISite) {
			processSite((ISite)i);
		} else if (i instanceof INode) {
			processNode((INode)i);
		}
	}
	
	private void processSite(ISite i) throws SaveFailedException {
		Site s = (Site)i; /* XXX!! */
		write("$" + (s.getAlias() == null ? s.getName() : s.getAlias()));
	}
	
	private void processNode(INode i) throws SaveFailedException {
		write(i.getIControl().getName());
		
		Iterator<? extends IPort> it = i.getIPorts().iterator();
		if (it.hasNext()) {
			write("[" + getPortString(it.next().getILink()));
			while (it.hasNext())
				write("," + getPortString(it.next().getILink()));
			write("]");
		}
		
		Iterator<? extends IChild> in = i.getIChildren().iterator();
		if (in.hasNext()) {
			write(".");
			IChild firstChild = in.next();
			if (in.hasNext()) {
				write("(");
				processChild(firstChild);
				while (in.hasNext()) {
					write(" | ");
					processChild(in.next());
				}
				write(")");
			} else processChild(firstChild);
		}
	}
	
	private void processRoot(IRoot i) throws SaveFailedException {
		Iterator<? extends INode> in = i.getINodes().iterator();
		if (in.hasNext()) {
			processNode(in.next());
			while (in.hasNext()) {
				write(" | ");
				processNode(in.next());
			}
		}
	}
	
	private void processBigraph(Bigraph b) throws SaveFailedException {
		Iterator<? extends IRoot> ir = b.getIRoots().iterator();
		if (ir.hasNext()) {
			processRoot(ir.next());
			while (ir.hasNext()) {
				write(" || ");
				processRoot(ir.next());
			}
		}
	}
	
	private void processRule(ReactionRule r) throws SaveFailedException {
		processBigraph(r.getRedex());
		write(" -> ");
		processBigraph(r.getReactum());
		write(";"); newline();
	}
	
	private void processModel(Bigraph b) throws SaveFailedException {
		processBigraph(b);
		write(";"); newline();
	}
	
	private void processSimulationSpec(SimulationSpec s) throws SaveFailedException {
		processSignature(s.getSignature());
		processNames(s);
		
		for (ReactionRule r : s.getRules())
			processRule(r);
		newline();
		
		processModel(s.getModel());
	}
}
