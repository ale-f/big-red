package dk.itu.big_red.model.import_export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import dk.itu.big_red.exceptions.ExportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Point;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.Thing;
import dk.itu.big_red.model.interfaces.ILayoutable;

public class BigraphTikZExport extends ModelExport<Bigraph> {
	private BufferedWriter writer;
	
	@Override
	public void exportObject() throws ExportFailedException {
		writer = new BufferedWriter(new OutputStreamWriter(target));
		
		process((ILayoutable)model);
		
		try {
			writer.close();
		} catch (IOException e) {
			throw new ExportFailedException(e);
		}
	}

	int scope = 0;
	
	private void line(String line) throws ExportFailedException {
		try {
			for (int i = 0; i < scope; i++)
				writer.write(" ");
			writer.write("\\" + line + "\n");
		} catch (Exception e) {
			throw new ExportFailedException(e);
		}
	}
	
	private void beginScope(Object context) throws ExportFailedException {
		line("begin{scope} % " + context);
		scope++;
	}
	
	private void endScope() throws ExportFailedException {
		scope--;
		line("end{scope}");
	}
	
	private void process(Bigraph b) throws ExportFailedException {
		line("documentclass{article}");
		line("usepackage{tikz}");
		line("begin{document}");
		
		for (Control c : b.getSignature().getControls())
			line("tikzset{" + c.getLongName() + "/.style={shape=rectangle,fill=blue!50}}");
		line("tikzset{internal root/.style={shape=rectangle,dash pattern=on 2pt off 2pt}}");
		line("tikzset{internal site/.style={shape=rectangle,dash pattern=on 2pt off 2pt,fill=black!25}}");
		
		line("begin{tikzpicture}[x=0.02cm,y=-0.02cm]");
		
		process((Thing)b);
		
		line("end{tikzpicture}");
		line("end{document}");
	}
	
	private void process(Node n) throws ExportFailedException {
		org.eclipse.draw2d.geometry.Point
			ptl = n.getRootLayout().getTopLeft(),
			ptr = n.getRootLayout().getBottomRight();
		
		line("draw [" + n.getControl().getLongName() + "] (" + ptl.x + "," +
				ptl.y + ") rectangle(" + ptr.x + "," + ptr.y + ");");
		
		process((Thing)n);
	}
	
	private void process(Point p) {
		
	}
	
	private void process(Edge e) {
		
	}
	
	private void process(Site r) throws ExportFailedException {
		org.eclipse.draw2d.geometry.Point
			ptl = r.getRootLayout().getTopLeft(),
			ptr = r.getRootLayout().getBottomRight();
		
		line("draw [internal site] (" + ptl.x + "," +
				ptl.y + ") rectangle(" + ptr.x + "," + ptr.y + ");");
		
		process((Thing)r);
	}
	
	private void process(Root r) throws ExportFailedException {
		org.eclipse.draw2d.geometry.Point
			ptl = r.getRootLayout().getTopLeft(),
			ptr = r.getRootLayout().getBottomRight();
		
		line("draw [internal root] (" + ptl.x + "," +
				ptl.y + ") rectangle(" + ptr.x + "," + ptr.y + ");");
		
		process((Thing)r);
	}
	
	private void process(Thing t) throws ExportFailedException {
		beginScope(t);
		for (ILayoutable c : t.getChildren())
			process(c);
		endScope();
	}
	
	private void process(ILayoutable obj) throws ExportFailedException {
		System.out.println("process(" + obj + ")");
		if (obj instanceof Bigraph) {
			process((Bigraph)obj);
		} else if (obj instanceof Node) {
			process((Node)obj);
		} else if (obj instanceof Point) {
			process((Point)obj);
		} else if (obj instanceof Edge) {
			process((Edge)obj);
		} else if (obj instanceof Site) {
			process((Site)obj);
		} else if (obj instanceof Root) {
			process((Root)obj);
		} else if (obj instanceof Thing) {
			process((Thing)obj);
		} else {
			/* do nothing */
		}
	}
}
