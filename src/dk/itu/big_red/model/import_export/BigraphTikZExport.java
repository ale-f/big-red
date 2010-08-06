package dk.itu.big_red.model.import_export;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
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
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.OuterName;
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
	
	private void beginScope(Thing context) throws ExportFailedException {
		line("begin{scope} % " + getNiceName(context));
		scope++;
	}
	
	private void endScope() throws ExportFailedException {
		scope--;
		line("end{scope}");
	}
	
	private Point translate = null;
	
	private void process(Bigraph b) throws ExportFailedException {
		line("pgfdeclarelayer{connection}");
		line("pgfsetlayers{main,connection}");
		
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
		line("tikzset{internal edge/.style={curve to,relative=false,draw=green!50!black,fill=none}}");
		line("tikzset{internal port/.style={circle,fill=red,draw=none,minimum size=6,inner sep=0}}");
		line("tikzset{internal root/.style={dash pattern=on 2pt off 2pt}}");
		line("tikzset{internal site/.style={dash pattern=on 2pt off 2pt,fill=black!25}}");
		line("tikzset{internal inner name/.style={fill=blue!30,draw=none}}");
		line("tikzset{internal outer name/.style={fill=red!30,draw=none}}");
		line("tikzset{internal name/.style={text=white,font=\\itshape}}");
		
		List<ILayoutable> ch = b.getChildren();
		if (ch.size() > 0) {
			Rectangle bounding = ch.get(0).getLayout().getCopy();
			for (ILayoutable i : ch)
				bounding.union(i.getLayout());
			translate = bounding.getTopLeft().getNegated();
			System.out.println("(translating everything by " + translate + ")");
		}
		
		newLine();
		
		line("begin{tikzpicture}[x=0.02cm,y=-0.02cm]");
		
		process((Thing)b);
		
		line("end{tikzpicture}");
	}
	
	private String getNiceName(Object o) {
		if (o instanceof Node) {
			return "node " + ((Node)o).getName();
		} if (o instanceof Site) {
			return "site " + ((Site)o).getName();
		} else if (o instanceof Root) {
			return "root " + ((Root)o).getName();
		} else if (o instanceof InnerName) {
			return "inner name " + ((InnerName)o).getName();
		} else if (o instanceof Edge) {
			return "edge " + ((Edge)o).getName();
		} else if (o instanceof OuterName) {
			return "outer name " + ((OuterName)o).getName();
		} else if (o instanceof Port) {
			Port p = (Port)o;
			return "port " + p.getName() + " on " + getNiceName(p.getParent());
		} else return o.toString();
	}
	
	private void process(Node n) throws ExportFailedException {
		Control con = n.getControl();
		Rectangle rl = n.getRootLayout().translate(translate);
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
		Rectangle rl = p.getRootLayout().translate(translate);
		Point tmp =
			rl.getCenter();
		line("node [internal port] (" + getNiceName(p) + ") at (" + tmp.x + "," + tmp.y + ") {};");
		process((dk.itu.big_red.model.Point)p);
	}
	
	private void process(Link e) throws ExportFailedException {
		Rectangle rl = e.getRootLayout().translate(translate);
		Point
			tl = rl.getTopLeft(),
			br = rl.getBottomRight(),
			c = rl.getCenter();
		if (e instanceof OuterName) {
			line("draw [internal outer name] (" + tl.x + "," + tl.y + ") rectangle (" + br.x + "," + br.y + ");");
			line("node [internal name] (" + getNiceName(e) + ") at (" + c.x + "," + c.y + ") {" + e.getName() + "};");
		} else if (e instanceof Edge) {
			line("node (" + getNiceName(e) + ") at (" + c.x + "," + c.y + ") {};");
		}
	}
	
	private void process(InnerName i) throws ExportFailedException {
		Rectangle rl = i.getRootLayout().translate(translate);
		Point
			tl = rl.getTopLeft(),
			br = rl.getBottomRight(),
			c = rl.getCenter();
		line("draw [internal inner name] (" + tl.x + "," + tl.y + ") rectangle (" + br.x + "," + br.y + ");");
		line("node [internal name] (" + getNiceName(i) + ") at (" + c.x + "," + c.y + ") {" + i.getName() + "};");
		process((dk.itu.big_red.model.Point)i);
	}
	
	private void process(dk.itu.big_red.model.Point p) throws ExportFailedException {
		List<EdgeConnection> x = p.getConnections();
		if (x.size() == 1) {
			EdgeConnection co = x.get(0);
			Dimension d =
				co.getParent().getLayout().getCenter().getDifference(co.getSource().getLayout().getCenter());
			String in = (d.height > 0 ? "90" : "270"),
			       out = (d.width > 0 ? "0" : "180");
			line("begin{pgfonlayer}{connection}");
			scope++;
			line("draw [internal edge,in=" + in + ",out=" + out + "] (" + getNiceName(p) + ") to (" + getNiceName(co.getParent()) + "); % " + d);
			scope--;
			line("end{pgfonlayer}");
		}
	}
	
	private void process(Site r) throws ExportFailedException {
		Rectangle rl = r.getRootLayout().translate(translate);
		Point
			ptl = rl.getTopLeft(),
			ptr = rl.getBottomRight();
		
		line("draw [internal site] (" + ptl.x + "," +
				ptl.y + ") rectangle(" + ptr.x + "," + ptr.y + ");");
		
		process((Thing)r);
	}
	
	private void process(Root r) throws ExportFailedException {
		Rectangle rl = r.getRootLayout().translate(translate);
		Point
			ptl = rl.getTopLeft(),
			ptr = rl.getBottomRight();
		
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
		BigraphXMLExport.sortChildrenIntoSchemaOrder(obj);
		
		if (obj instanceof Bigraph) {
			process((Bigraph)obj);
		} else if (obj instanceof Node) {
			process((Node)obj);
		} else if (obj instanceof Port) {
			process((Port)obj);
		} else if (obj instanceof Link) {
			process((Link)obj);
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
