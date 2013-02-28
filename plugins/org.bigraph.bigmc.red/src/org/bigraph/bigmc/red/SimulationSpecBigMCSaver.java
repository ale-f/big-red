package org.bigraph.bigmc.red;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Control;
import org.bigraph.model.Layoutable;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Node;
import org.bigraph.model.ReactionRule;
import org.bigraph.model.Signature;
import org.bigraph.model.SimulationSpec;
import org.bigraph.model.Site;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.interfaces.IChild;
import org.bigraph.model.interfaces.IEntity;
import org.bigraph.model.interfaces.ILink;
import org.bigraph.model.interfaces.INode;
import org.bigraph.model.interfaces.IOuterName;
import org.bigraph.model.interfaces.IPort;
import org.bigraph.model.interfaces.IRoot;
import org.bigraph.model.interfaces.ISite;
import org.bigraph.model.names.policies.INamePolicy;
import org.bigraph.model.savers.SaveFailedException;
import org.bigraph.model.savers.Saver;
import org.bigraph.model.utilities.CollectionUtilities;
import org.bigraph.model.utilities.comparators.ComparatorUtilities;
import org.bigraph.model.utilities.comparators.IntegerStringComparator;
import org.bigraph.model.utilities.comparators.LexicographicStringComparator;

import dk.itu.big_red.model.ExtendedDataUtilities;
import org.bigraph.extensions.param.ParameterUtilities;

public class SimulationSpecBigMCSaver extends Saver {
	private OutputStreamWriter osw = null;
	
	private boolean namedRules = true;
	
	{
		addOption(new SaverOption("Export named rules") {
			@Override
			public Object get() {
				return namedRules;
			}
			
			@Override
			public void set(Object value) {
				namedRules = (Boolean)value;
			}
		});
	}
	
	private static final class EntityNameRetriever
			implements ComparatorUtilities.Converter<IEntity, String> {
		private static final EntityNameRetriever INSTANCE =
				new EntityNameRetriever();
		
		@Override
		public String convert(IEntity object) {
			return object.getName();
		}
	}
	
	private static final <T extends IEntity> Collection<T> order(
			Iterable<? extends T> it, Comparator<String> cmp) {
		return CollectionUtilities.collect(it,
				ComparatorUtilities.convertComparator(
						EntityNameRetriever.INSTANCE, cmp));
	}
	
	private static final Comparator<String>
		C_INT = IntegerStringComparator.INSTANCE,
		C_LEX = LexicographicStringComparator.INSTANCE;
	
	private static Pattern p = Pattern.compile("[^a-zA-Z0-9_]");
	
	private static String normaliseName(String name) {
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
			String param = ParameterUtilities.getParameter(n);
			if (param != null)
				writeControl(n.getControl(), param);
		}
		
		if (l instanceof Container)
			for (Layoutable i : ((Container)l).getChildren())
				recHandleParams(i);
	}
	
	private boolean recProcessSignature(List<String> names, Signature s)
			throws SaveFailedException {
		boolean parameterised = false;
		for (Control c : s.getControls()) {
			if (names.contains(c.getName()))
				continue;
			INamePolicy policy = ParameterUtilities.getParameterPolicy(c);
			if (policy == null) {
				writeControl(c, null);
			} else parameterised = true;
		}
		for (Signature t : s.getSignatures())
			parameterised |= recProcessSignature(names, t);
		return parameterised;
	}
	
	private void processSignature(SimulationSpec ss) throws SaveFailedException {
		write("# Controls\n");
		if (recProcessSignature(
				new ArrayList<String>(), ss.getSignature())) {
			recHandleParams(ss.getModel());
			for (ReactionRule r : ss.getRules()) {
				recHandleParams(r.getRedex());
				try {
					recHandleParams(r.createReactum());
				} catch (ChangeCreationException e) {
					throw new SaveFailedException(e);
				}
			}
		}
		write("\n");
	}
	
	private void processNames(SimulationSpec s) throws SaveFailedException {
		Collection<? extends IOuterName> names = s.getModel().getOuterNames();
		
		if (names.size() == 0)
			return;
		write("# Names\n");
		for (IOuterName i : names)
			write("%name " + normaliseName(i.getName()) + ";\n");
		write("\n");
	}
	
	private static String getPortString(ILink l) {
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
			param = ParameterUtilities.getParameter((Node)i); /* XXX!! */
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
		
		Iterator<? extends IChild> in =
				order(i.getIChildren(), C_LEX).iterator();
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
		Iterator<? extends INode> in = order(i.getNodes(), C_LEX).iterator();
		boolean anyNodes = in.hasNext();
		if (anyNodes) {
			processNode(in.next());
			while (in.hasNext()) {
				write(" | ");
				processNode(in.next());
			}
		}
		
		Iterator<? extends ISite> is = order(i.getSites(), C_INT).iterator();
		boolean anySites = is.hasNext();
		if (anySites) {
			if (anyNodes)
				write(" | ");
			processSite(is.next());
			while (is.hasNext()) {
				write(" | ");
				processSite(is.next());
			}
		}
		
		if (!anyNodes && !anySites)
			write("nil");
	}
	
	private void processBigraph(Bigraph b) throws SaveFailedException {
		Iterator<? extends IRoot> ir = order(b.getRoots(), C_INT).iterator();
		if (ir.hasNext()) {
			processRoot(ir.next());
			while (ir.hasNext()) {
				write(" || ");
				processRoot(ir.next());
			}
		} else write("nil");
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
		Bigraph reactum;
		try {
			reactum = r.createReactum();
		} catch (ChangeCreationException e) {
			throw new SaveFailedException(e);
		}
		
		if (!iteratorsMatched(
				r.getRedex().getRoots().iterator(),
				reactum.getRoots().iterator()))
			throw new SaveFailedException(
					"Same number of roots required in redex and reactum");
		if (namedRules)
			write("%rule r_" + (i++) + " "); /* XXX FIXME */
		processBigraph(r.getRedex());
		write(" -> ");
		processBigraph(reactum);
		write(";\n");
	}
	
	private void processModel(Bigraph b) throws SaveFailedException {
		processBigraph(b);
		write(";\n");
	}
	
	private void processSimulationSpec(SimulationSpec s) throws SaveFailedException {
		processSignature(s);
		processNames(s);
		
		List<? extends ReactionRule> rules = s.getRules();
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
