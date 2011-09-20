package dk.itu.big_red.model.import_export;

import java.io.IOException;
import java.io.OutputStreamWriter;

import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.interfaces.IBigraph;
import dk.itu.big_red.model.interfaces.IChild;
import dk.itu.big_red.model.interfaces.IControl;
import dk.itu.big_red.model.interfaces.IEdge;
import dk.itu.big_red.model.interfaces.IInnerName;
import dk.itu.big_red.model.interfaces.INode;
import dk.itu.big_red.model.interfaces.IOuterName;
import dk.itu.big_red.model.interfaces.IParent;
import dk.itu.big_red.model.interfaces.IPort;
import dk.itu.big_red.model.interfaces.IRoot;
import dk.itu.big_red.model.interfaces.ISite;

/**
 * An exporter to the BPL Tool term language (which actually in some cases should produce proper BPL Terms!).
 */
public class BigraphBPLToolExport extends ModelExport<Bigraph> {
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
	
	int scope = 0;
	boolean indented = false;

	private void print(Object line) throws ExportFailedException {
		try {
			if (!indented) {
				for (int i = 0; i < scope; i++)
					osw.write("  ");
				indented = true;
			}
			osw.write(line.toString());
		} catch (IOException e) {
			throw new ExportFailedException(e);
		}
	}

	private void printLine(Object line) throws ExportFailedException {
		print(line);
		print("\n");
		indented = false;
	}
	
	private String SMLify(String s) {
		// FIXME do this properly...
		return s.replace(' ', '_');
	}
	
	private void printStringDef(String s) throws ExportFailedException {
		print("val ");
		print(s);
		print(" = \"");
		print(s);
		printLine("\";");
	}
	
	interface Processor<T> {
		public void proc(T t) throws ExportFailedException;
	}
	private <T> void processIterable(Iterable<? extends T> i, String empty, String left, String sep, String right, Processor<T> p) throws ExportFailedException {
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

	public void process(INode node) throws ExportFailedException {
		print(SMLify(node.getIControl().getName()));
		processIterable(node.getIPorts(), "[]", "[", ", ", "]", new Processor<IPort>() {
			public void proc(IPort p) throws ExportFailedException {
				print(p.getName());
				print(" == ");
				print(SMLify(p.getILink().getName()));
			}
		});
		if (node.getINodes().iterator().hasNext() || node.getISites().iterator().hasNext()) {
			printLine(" o");
			scope++;
			process((IParent) node);
			scope--;
		}
	}

	public void process(ISite site) throws ExportFailedException {
		print("[(* ");
		print(site.getName());
		print(" *)]");
	}
	
	public void process(IParent parent) throws ExportFailedException {
		processIterable(parent.getIChildren(), "<->", "(", " | ", ")", new Processor<IChild>() {
			public void proc(IChild c) throws ExportFailedException {
				if (c instanceof INode)
					process((INode) c);
				else {
					process((ISite) c);
				}
			}
		});
	}
	
	public void process(IBigraph bigraph) throws ExportFailedException {
		printLine("(* string definitions *)");
		for (IControl c : bigraph.getISignature().getIControls()) {
			printStringDef(SMLify(c.getName()));
			for (IPort p : c.getIPorts())
				printStringDef(SMLify(p.getName()));
		}
		for (IEdge e : bigraph.getIEdges())
			printStringDef(SMLify(e.getName()));
		for (IOuterName o : bigraph.getIOuterNames())
			printStringDef(SMLify(o.getName()));
		for (IInnerName i : bigraph.getIInnerNames())
			printStringDef(SMLify(i.getName()));

		printLine("");
		printLine("(* signature *)");
		processIterable(bigraph.getISignature().getIControls(), null, "", "\n", "\n", new Processor<IControl>() {
			public void proc(IControl c) throws ExportFailedException {
				String name = SMLify(c.getName());
				print("val ");
				print(name);
				print(" = active(");
				print(name);
				print(" --: ");
				processIterable(c.getIPorts(), null, "[", ", ", "]", new Processor<IPort>() {
					public void proc(IPort p) throws ExportFailedException {
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
		processIterable(bigraph.getIOuterNames(), "idw[]", "idw[", ", ", "]", new Processor<IOuterName>() {
			public void proc(IOuterName o) throws ExportFailedException {
				print(SMLify(o.getName()));
			}
		});
		processIterable(bigraph.getIEdges(), "", " * -//[", ", ", "]", new Processor<IEdge>() {
			public void proc(IEdge e) throws ExportFailedException {
				print(SMLify(e.getName()));
			}
		});
		printLine(")");
		print("o (");
		scope++;

		processIterable(bigraph.getIInnerNames(), "", "", " || ", " || ", new Processor<IInnerName>() {
			public void proc(IInnerName i) throws ExportFailedException {
				print(SMLify(i.getILink().getName()) + "/" + SMLify(i.getName()));
			}
		});
		
		processIterable(bigraph.getIRoots(), "idp(0)", "", "|| ", "", new Processor<IRoot>() {
			public void proc(IRoot r) throws ExportFailedException {
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
