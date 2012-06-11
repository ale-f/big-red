package org.bigraph.bigmc.red;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.bigraph.model.ModelObject;
import org.bigraph.model.names.policies.INamePolicy;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.ReactionRule;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.SimulationSpec;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.interfaces.IChild;
import dk.itu.big_red.model.interfaces.ILink;
import dk.itu.big_red.model.interfaces.INode;
import dk.itu.big_red.model.interfaces.IOuterName;
import dk.itu.big_red.model.interfaces.IPort;
import dk.itu.big_red.model.interfaces.IRoot;
import dk.itu.big_red.model.interfaces.ISite;
import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.model.load_save.Saver;

public class SimulationSpecBigMCSaver extends Saver {
	private OutputStreamWriter osw = null;
	
	private boolean namedRules = true;
	
	{
		addOption("NameRules", "Export named rules");
	}
	
	@Override
	public Object getOption(String id) {
		if (id.equals("NameRules")) {
			return namedRules;
		} else return super.getOption(id);
	}
	
	@Override
	public void setOption(String id, Object value) {
		if (id.equals("NameRules")) {
			namedRules = (Boolean)value;
		} else super.setOption(id, value);
	}
	
	private static Pattern p = Pattern.compile("[^a-zA-Z0-9_]");
	
	private String normaliseName(String name) {
		return p.matcher(name.trim()).replaceAll("_");
	}
	
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

	private final ArrayList<String> controlNames = new ArrayList<String>();
	
	private void writeControl(Control c, String param)
			throws SaveFailedException {
		String name = normaliseName(c.getName());
		if (param != null)
			name += "_P__" + normaliseName(param);
		
		if (!controlNames.contains(name)) {
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
			
			write(name + " : ");
			write(c.getPorts().size() + ";\n");
			
			controlNames.add(name);
		}
	}
	
	private void recHandleParams(Layoutable l) throws SaveFailedException {
		if (l instanceof Node) {
			Node n = (Node)l;
			String param = ExtendedDataUtilities.getParameter(n);
			if (param != null)
				writeControl(n.getControl(), param);
		}
		
		if (l instanceof Container)
			for (Layoutable i : ((Container)l).getChildren())
				recHandleParams(i);
	}
	
	private void processSignature(SimulationSpec ss) throws SaveFailedException {
		Signature s = ss.getSignature();
		write("# Controls\n");
		boolean parameterised = false;
		for (Control c : s.getControls()) {
			INamePolicy policy = ExtendedDataUtilities.getParameterPolicy(c);
			if (policy == null) {
				writeControl(c, null);
			} else parameterised = true;
		}
		if (parameterised) {
			recHandleParams(ss.getModel());
			for (ReactionRule r : ss.getRules()) {
				recHandleParams(r.getRedex());
				recHandleParams(r.getReactum());
			}
		}
		write("\n");
	}
	
	private void processNames(SimulationSpec s) throws SaveFailedException {
		ArrayList<String> names = new ArrayList<String>();
		for (IOuterName o : s.getModel().getOuterNames())
			names.add(normaliseName(o.getName()));
		Collections.sort(names);
		
		if (names.size() == 0)
			return;
		write("# Names\n");
		for (String name : names)
			write("%name " + name + ";\n");
		write("\n");
	}
	
	private String getPortString(ILink l) {
		return (l != null ? normaliseName(l.getName()) : "-");
	}
	
	private void processChild(IChild i) throws SaveFailedException {
		if (i instanceof ISite) {
			processSite((ISite)i);
		} else if (i instanceof INode) {
			processNode((INode)i);
		}
	}
	
	private void processSite(ISite i) throws SaveFailedException {
		String alias = ExtendedDataUtilities.getAlias((Site)i); /* XXX!! */
		write("$" + (alias == null ? i.getName() : alias));
	}
	
	private void processNode(INode i) throws SaveFailedException {
		String
			name = normaliseName(i.getControl().getName()),
			param = ExtendedDataUtilities.getParameter((Node)i); /* XXX!! */
		if (param != null)
			name += "_P__" + normaliseName(param);
		write(name);
		
		Iterator<? extends IPort> it = i.getPorts().iterator();
		if (it.hasNext()) {
			write("[" + getPortString(it.next().getLink()));
			while (it.hasNext())
				write("," + getPortString(it.next().getLink()));
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
		Iterator<? extends INode> in = i.getNodes().iterator();
		boolean anyNodes = in.hasNext();
		if (anyNodes) {
			processNode(in.next());
			while (in.hasNext()) {
				write(" | ");
				processNode(in.next());
			}
		}
		
		Iterator<? extends ISite> is = i.getSites().iterator();
		if (is.hasNext()) {
			if (anyNodes)
				write(" | ");
			processSite(is.next());
			while (is.hasNext()) {
				write(" | ");
				processSite(is.next());
			}
		}
	}
	
	private void processBigraph(Bigraph b) throws SaveFailedException {
		Iterator<? extends IRoot> ir = b.getRoots().iterator();
		if (ir.hasNext()) {
			processRoot(ir.next());
			while (ir.hasNext()) {
				write(" || ");
				processRoot(ir.next());
			}
		}
	}
	
	private static <T, V>
	boolean iteratorsMatched(Iterator<T> i, Iterator<V> j) {
		while (i.hasNext() && j.hasNext()) {
			i.next(); j.next();
		}
		return (i.hasNext() == j.hasNext());
	}
	
	private int i = 0;
	
	private void processRule(ReactionRule r) throws SaveFailedException {
		if (!iteratorsMatched(
				r.getRedex().getRoots().iterator(),
				r.getReactum().getRoots().iterator()))
			throw new SaveFailedException("Bananas");
		if (namedRules)
			write("%rule r_" + (i++) + " "); /* XXX FIXME */
		processBigraph(r.getRedex());
		write(" -> ");
		processBigraph(r.getReactum());
		write(";\n");
	}
	
	private void processModel(Bigraph b) throws SaveFailedException {
		processBigraph(b);
		write(";\n");
	}
	
	private void processSimulationSpec(SimulationSpec s) throws SaveFailedException {
		processSignature(s);
		processNames(s);
		
		List<ReactionRule> rules = s.getRules();
		if (rules.size() != 0) {
			write("# Rules\n");
			for (ReactionRule r : s.getRules())
				processRule(r);
			write("\n");
		}
		
		write("# Model\n");
		processModel(s.getModel());
		
		write("\n# Go!\n%check;\n");
	}
}
