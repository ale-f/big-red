package dk.itu.big_red.model.import_export;

import java.io.IOException;
import java.io.OutputStreamWriter;

import dk.itu.big_red.exceptions.ExportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.interfaces.pure.IBigraph;
import dk.itu.big_red.model.interfaces.pure.IControl;
import dk.itu.big_red.model.interfaces.pure.IEdge;
import dk.itu.big_red.model.interfaces.pure.IInnerName;
import dk.itu.big_red.model.interfaces.pure.INode;
import dk.itu.big_red.model.interfaces.pure.IOuterName;
import dk.itu.big_red.model.interfaces.pure.IParent;
import dk.itu.big_red.model.interfaces.pure.IPort;
import dk.itu.big_red.model.interfaces.pure.IRoot;
import dk.itu.big_red.model.interfaces.pure.ISite;

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
		printLine("val " + s + " = \"" + s + "\";");
	}
	
	interface Printer<T> {
		public void prnt(T t) throws ExportFailedException;
	}
	private <T> void printIterable(Iterable<T> i, String left, String sep, String right, Printer<T> p) throws ExportFailedException {
		print(left);
		boolean first = true;
		for (T t : i) {
			if (!first) print(sep);
			p.prnt(t);
			first = false;
		}
		print(right);
	}
	
	interface Processor<T> {
		public void proc(T t) throws ExportFailedException;
	}
	private <T> void processIterable(Iterable<T> i, String left, String sep, String right, Processor<T> p) throws ExportFailedException {
		print(left);
		boolean first = true;
		for (T t : i) {
			if (!first) print(sep);
			p.proc(t);
			first = false;
		}
		print(right);
	}
	
	public void process(INode node) throws ExportFailedException {
		print(SMLify(node.getIControl().getName()));
		printIterable(node.getIPorts(), "[", ", ", "]", new Printer<IPort>() {
			public void prnt(IPort p) throws ExportFailedException {
				print(SMLify(p.getILink().getName()));
			}
		});
		if (node.getINodes().iterator().hasNext() || node.getISites().iterator().hasNext()) {
			printLine(" o (");
			scope++;
			process((IParent) node);
			scope--;
			printLine(")");
		}
	}
	
	public void process(IParent parent) throws ExportFailedException {
		processIterable(parent.getINodes(), "", " | ", "", new Processor<INode>() {
			public void proc(INode n) throws ExportFailedException {
				process(n);
			}
		});
	
		printIterable(parent.getISites(), "", " | ", "", new Printer<ISite>() {
			public void prnt(ISite s) throws ExportFailedException {
				print("[(* " + s.getName() + " *)]");
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
		printIterable(bigraph.getISignature().getIControls(), "", "\n", "", new Printer<IControl>() {
			public void prnt(IControl c) throws ExportFailedException {
				String name = SMLify(c.getName());
				print("val " + name + " = active(" + name + " --: ");
				printIterable(c.getIPorts(), "[", ", ", "]", new Printer<IPort>() {
					public void prnt(IPort p) throws ExportFailedException {
						print(SMLify(p.getName()));
					}
				});
				printLine(");");
			}
		});

		printLine("");
		printLine("(* agent *)");
		printLine("val agent = ");
		scope++;
		
		print("(idw");
		printIterable(bigraph.getIOuterNames(), "[", ", ", "]", new Printer<IOuterName>() {
			public void prnt(IOuterName o) throws ExportFailedException {
				print(SMLify(o.getName()));
			}
		});
		print(" * -//");
		printIterable(bigraph.getIEdges(), "[", ", ", "]", new Printer<IEdge>() {
			public void prnt(IEdge e) throws ExportFailedException {
				print(SMLify(e.getName()));
			}
		});
		printLine(")");
		print("o (");
		scope++;

		printIterable(bigraph.getIInnerNames(), "", " || ", "", new Printer<IInnerName>() {
			public void prnt(IInnerName i) throws ExportFailedException {
				print(SMLify(i.getILink().getName()) + "/" + SMLify(i.getName()));
			}
		});
		
		if (bigraph.getIInnerNames().iterator().hasNext())
			printLine(" || ");
		
		printIterable(bigraph.getIRoots(), "", " || ", "", new Printer<IRoot>() {
			public void prnt(IRoot r) throws ExportFailedException {
				process(r);
			}
		});
		
		scope--;
		printLine(");");
	}
}
