package dk.itu.big_red.model.import_export;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Iterator;

import dk.itu.big_red.import_export.Export;
import dk.itu.big_red.import_export.ExportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Signature;
import dk.itu.big_red.model.interfaces.IEdge;
import dk.itu.big_red.model.interfaces.IInnerName;
import dk.itu.big_red.model.interfaces.ILink;
import dk.itu.big_red.model.interfaces.INode;
import dk.itu.big_red.model.interfaces.IOuterName;
import dk.itu.big_red.model.interfaces.IPort;
import dk.itu.big_red.model.interfaces.IRoot;

public class BigraphBigMCExport extends Export<Bigraph> {
	private OutputStreamWriter osw = null;
	
	private int indentation = -1;
	
	private void newline() throws ExportFailedException {
		write("\n");
		for (int i = 0; i < indentation; i++)
			write("\t");
	}
	
	private void write(String str) throws ExportFailedException {
		try {
			osw.write(str);
		} catch (IOException e) {
			throw new ExportFailedException(e);
		}
	}
	
	@Override
	public void exportObject() throws ExportFailedException {
		osw = new OutputStreamWriter(getOutputStream());
		process(getModel());
		try {
			osw.close();
		} catch (IOException e) {
			throw new ExportFailedException(e);
		}
	}

	private void processSignature(Signature s) throws ExportFailedException {
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
	
	private String getPortString(ILink l) {
		return (l != null ? "n_" + l.getName() : "-");
	}
	
	private void processNode(INode i) throws ExportFailedException {
		indentation++;
		newline();
		
		write(i.getIControl().getName());
		
		Iterator<? extends IPort> it = i.getIPorts().iterator();
		if (it.hasNext()) {
			write("[" + getPortString(it.next().getILink()));
			while (it.hasNext())
				write("," + getPortString(it.next().getILink()));
			write("]");
		}
		
		Iterator<? extends INode> in = i.getINodes().iterator();
		if (in.hasNext()) {
			write(".(");
			processNode(in.next());
			while (in.hasNext()) {
				write(" | ");
				processNode(in.next());
				newline();
			}
			write(")");
		}
		
		indentation--;
	}
	
	private void processRoot(IRoot i) throws ExportFailedException {
		Iterator<? extends INode> in = i.getINodes().iterator();
		if (in.hasNext()) {
			processNode(in.next());
			while (in.hasNext()) {
				write(" | ");
				processNode(in.next());
			}
		}
	}
	
	private void process(Bigraph b) throws ExportFailedException {
		processSignature(b.getSignature());
		boolean any = false;
		
		for (IOuterName i : b.getIOuterNames()) {
			if (!any) {
				write("# Outer names"); newline();
				any = true;
			}
			write("%name n_" + i.getName() + ";"); newline();
		}
		
		if (any)
			newline();
		
		any = false;
		for (IEdge e : b.getIEdges()) {
			if (!any) {
				write("# Edges"); newline();
				any = true;
			}
			write("%name n_" + e.getName() + ";"); newline();
		}
		
		if (any)
			newline();
		
		any = false;
		for (IInnerName i : b.getIInnerNames()) {
			if (!any) {
				write("# Inner names"); newline();
				any = true;
			}
			write("%inner in_" + i.getName() + ";"); newline();
		}
		
		Iterator<? extends IRoot> ir = b.getIRoots().iterator();
		if (ir.hasNext()) {
			processRoot(ir.next());
			while (ir.hasNext()) {
				write(" || ");
				processRoot(ir.next());
			}
		}
		write(";"); newline();
		
		newline(); write("%check;"); newline();
	}

	@Override
	public Class<?> getType() {
		return Bigraph.class;
	}
}
