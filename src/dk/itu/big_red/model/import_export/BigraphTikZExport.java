package dk.itu.big_red.model.import_export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.RGB;

import dk.itu.big_red.exceptions.ExportFailedException;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Control.Shape;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.EdgeConnection;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.Port;
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
				writer.write("  ");
			writer.write("\\" + line + "\n");
		} catch (Exception e) {
			throw new ExportFailedException(e);
		}
	}
	
	private void newLine() throws ExportFailedException {
		try {
			writer.write("\n");
		} catch (Exception e) {
			throw new ExportFailedException(e);
		}
	}
	
	private void commentLine(String line) throws ExportFailedException {
		try {
			writer.write("% " + line);
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
		
		newLine();
		
		for (Control c : b.getSignature().getControls()) {
			String cN = c.getLongName();
			RGB fillColour = c.getFillColour(),
			    outlineColour = c.getOutlineColour();
			line("definecolor{" + cN + " fill}{RGB}{" + fillColour.red + "," + fillColour.green + "," + fillColour.blue + "}");
			line("definecolor{" + cN + " outline}{RGB}{" + outlineColour.red + "," + outlineColour.green + "," + outlineColour.blue + "}");
			line("tikzset{" + cN + "/.style={fill=" + cN + " fill,draw=" + cN + " outline}}");
			newLine();
		}
		line("tikzset{internal edge/.style={curve to,in=90,draw=green!50!black,fill=none}}");
		line("tikzset{internal port/.style={circle,fill=red,draw=none,minimum size=6,inner sep=0}}");
		line("tikzset{internal root/.style={dash pattern=on 2pt off 2pt}}");
		line("tikzset{internal site/.style={dash pattern=on 2pt off 2pt,fill=black!25}}");
		line("tikzset{internal inner name/.style={fill=blue!30,draw=none}}");
		
		newLine();
		
		line("begin{tikzpicture}[x=0.02cm,y=-0.02cm]");
		
		process((Thing)b);
		
		line("end{tikzpicture}");
		line("end{document}");
	}
	
	private String getPointName(Object o) {
		if (o instanceof InnerName) {
			return "inner name " + ((InnerName)o).getName();
		} else if (o instanceof Port) {
			Port p = (Port)o;
			return "port " + p.getName() + " on node " + p.getParent().hashCode();
		} else return null;
	}
	
	private void process(Node n) throws ExportFailedException {
		Control con = n.getControl();
		Rectangle rl = n.getRootLayout();
		Point rltl = rl.getTopLeft();
		
		Point tmp;
		String shapeDescriptor = "";
		if (con.getShape() == Shape.SHAPE_OVAL) {
			tmp = rl.getCenter();
			shapeDescriptor += "(" + tmp.x + "," + tmp.y + ") ellipse (" +
				(rl.width / 2) + " and " + (rl.height / 2) + ")";
		} else if (con.getShape() == Shape.SHAPE_POLYGON) {
			PointList fp = n.getFittedPolygon().getCopy();
			fp.translate(rltl);
			tmp = new Point();
			for (int i = 0; i < fp.size(); i++) {
				fp.getPoint(tmp, i);
				shapeDescriptor += "(" + tmp.x + "," + tmp.y + ") -- ";
			}
			fp.getPoint(tmp, 0);
			shapeDescriptor += "(" + tmp.x + "," + tmp.y + ")";
		}
		
		line("draw [" + n.getControl().getLongName() + "] " + shapeDescriptor + ";");
		line("node at (" + rltl.x + "," + rltl.y + ") {" + con.getLabel() + "};");
		
		beginScope(n);
		for (ILayoutable c : n.getChildren())
			process(c);
		for (ILayoutable c : n.getPorts())
			process(c);
		endScope();
	}
	
	private void process(Port p) throws ExportFailedException {
		Rectangle rl = p.getRootLayout();
		Point tmp =
			rl.getCenter();
		line("node [internal port] (" + getPointName(p) + ") at (" + tmp.x + "," + tmp.y + ") {};");
	}
	
	private void process(Edge e) throws ExportFailedException {
		Point rlc = e.getRootLayout().getCenter();
		line("node (edge " + e.getName() + ") at (" + rlc.x + "," + rlc.y + ") {};");
		for (EdgeConnection c : e.getConnections())
			line("draw [internal edge] (" + getPointName(c.getSource()) + ") to (edge " + e.getName() + ");");
	}
	
	private void process(InnerName i) throws ExportFailedException {
		Point
			tl = i.getRootLayout().getTopLeft(),
			br = i.getRootLayout().getBottomRight(),
			c = i.getRootLayout().getCenter();
		line("draw [internal inner name] (" + tl.x + "," + tl.y + ") rectangle (" + br.x + "," + br.y + ");");
		line("node (" + getPointName(i) + ") at (" + c.x + "," + c.y + ") {" + i.getName() + "};");
	}
	
	private void process(Site r) throws ExportFailedException {
		Point
			ptl = r.getRootLayout().getTopLeft(),
			ptr = r.getRootLayout().getBottomRight();
		
		line("draw [internal site] (" + ptl.x + "," +
				ptl.y + ") rectangle(" + ptr.x + "," + ptr.y + ");");
		
		process((Thing)r);
	}
	
	private void process(Root r) throws ExportFailedException {
		Point
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
		if (obj instanceof Bigraph) {
			process((Bigraph)obj);
		} else if (obj instanceof Node) {
			process((Node)obj);
		} else if (obj instanceof Port) {
			process((Port)obj);
		} else if (obj instanceof Edge) {
			process((Edge)obj);
		} else if (obj instanceof InnerName) {
			process((InnerName)obj);
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
