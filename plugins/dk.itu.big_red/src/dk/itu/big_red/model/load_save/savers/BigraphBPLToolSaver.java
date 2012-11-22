package dk.itu.big_red.model.load_save.savers;

import java.io.IOException;
import java.io.OutputStreamWriter;

import org.bigraph.model.Bigraph;
import org.bigraph.model.ModelObject;
import org.bigraph.model.interfaces.IBigraph;
import org.bigraph.model.interfaces.IChild;
import org.bigraph.model.interfaces.IControl;
import org.bigraph.model.interfaces.IEdge;
import org.bigraph.model.interfaces.IInnerName;
import org.bigraph.model.interfaces.INode;
import org.bigraph.model.interfaces.IOuterName;
import org.bigraph.model.interfaces.IParent;
import org.bigraph.model.interfaces.IPort;
import org.bigraph.model.interfaces.IRoot;
import org.bigraph.model.interfaces.ISite;
import org.bigraph.model.savers.SaveFailedException;
import org.bigraph.model.savers.Saver;

/**
 * An exporter to the BPL Tool term language (which actually in some cases should produce proper BPL Terms!).
 */
public class BigraphBPLToolSaver extends Saver {
	private OutputStreamWriter osw = null;
	
	@Override
	public Bigraph getModel() {
		return (Bigraph)super.getModel();
	}
	
	@Override
	public BigraphBPLToolSaver setModel(ModelObject model) {
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
	
	int scope = 0;
	boolean indented = false;

	private void print(Object line) throws SaveFailedException {
		try {
			if (!indented) {
				for (int i = 0; i < scope; i++)
					osw.write("  ");
				indented = true;
			}
			osw.write(line.toString());
		} catch (IOException e) {
			throw new SaveFailedException(e);
		}
	}

	private void printLine(Object line) throws SaveFailedException {
		print(line);
		print("\n");
		indented = false;
	}
	
	private static String SMLify(String s) {
		// FIXME do this properly...
		return s.replace(' ', '_');
	}
	
	private void printStringDef(String s) throws SaveFailedException {
		print("val ");
		print(s);
		print(" = \"");
		print(s);
		printLine("\";");
	}
	
	interface Processor<T> {
		void proc(T t) throws SaveFailedException;
	}
	private <T> void processIterable(Iterable<? extends T> i, String empty, String left, String sep, String right, Processor<T> p) throws SaveFailedException {
		boolean first = true;
		for (T t : i) {
			if (first)
				print(left);
			else
				print(sep);
			p.proc(t);
			first = false;
		}
		if (first)
			print(empty);
		else
			print(right);
	}

	public void process(INode node) throws SaveFailedException {
		print(SMLify(node.getControl().getName()));
		processIterable(node.getPorts(), "[]", "[", ", ", "]", new Processor<IPort>() {
			@Override
			public void proc(IPort p) throws SaveFailedException {
				print(p.getName());
				print(" == ");
				print(SMLify(p.getLink().getName()));
			}
		});
		if (node.getNodes().iterator().hasNext() || node.getSites().iterator().hasNext()) {
			printLine(" o");
			scope++;
			process((IParent) node);
			scope--;
		}
	}

	public void process(ISite site) throws SaveFailedException {
		print("`[(* ");
		print(site.getName());
		print(" *)]`");
	}
	
	public void process(IParent parent) throws SaveFailedException {
		processIterable(parent.getIChildren(), "<->", "(", " `|` ", ")", new Processor<IChild>() {
			@Override
			public void proc(IChild c) throws SaveFailedException {
				if (c instanceof INode)
					process((INode) c);
				else {
					process((ISite) c);
				}
			}
		});
	}
	
	public void process(IBigraph bigraph) throws SaveFailedException {
		printLine("(* string definitions *)");
		for (IControl c : bigraph.getSignature().getControls()) {
			printStringDef(SMLify(c.getName()));
			for (IPort p : c.getPorts())
				printStringDef(SMLify(p.getName()));
		}
		for (IEdge e : bigraph.getEdges())
			printStringDef(SMLify(e.getName()));
		for (IOuterName o : bigraph.getOuterNames())
			printStringDef(SMLify(o.getName()));
		for (IInnerName i : bigraph.getInnerNames())
			printStringDef(SMLify(i.getName()));

		printLine("");
		printLine("(* signature *)");
		processIterable(bigraph.getSignature().getControls(), null, "", "\n", "\n", new Processor<IControl>() {
			@Override
			public void proc(IControl c) throws SaveFailedException {
				String name = SMLify(c.getName());
				print("val ");
				print(name);
				print(" = active(");
				print(name);
				print(" --: ");
				processIterable(c.getPorts(), null, "[", ", ", "]", new Processor<IPort>() {
					@Override
					public void proc(IPort p) throws SaveFailedException {
						print(SMLify(p.getName()));
					}
				});
				print(");");
			}
		});

		printLine("");
		printLine("(* agent *)");
		printLine("val agent = ");
		scope++;
		
		print("(");
		processIterable(bigraph.getOuterNames(), "idw[]", "idw[", ", ", "]", new Processor<IOuterName>() {
			@Override
			public void proc(IOuterName o) throws SaveFailedException {
				print(SMLify(o.getName()));
			}
		});
		processIterable(bigraph.getEdges(), "", " * -//[", ", ", "]", new Processor<IEdge>() {
			@Override
			public void proc(IEdge e) throws SaveFailedException {
				print(SMLify(e.getName()));
			}
		});
		printLine(")");
		print("o (");
		scope++;

		processIterable(bigraph.getInnerNames(), "", "", " || ", " || ", new Processor<IInnerName>() {
			@Override
			public void proc(IInnerName i) throws SaveFailedException {
				print(SMLify(i.getLink().getName()) + "/" + SMLify(i.getName()));
			}
		});
		
		processIterable(bigraph.getRoots(), "idp(0)", "", "|| ", "", new Processor<IRoot>() {
			@Override
			public void proc(IRoot r) throws SaveFailedException {
				scope++;
				process(r);
				printLine("");
				scope--;
			}
		});
		
		scope--;
		printLine(");");
	}
}
