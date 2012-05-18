package dk.itu.big_red.model.load_save.savers;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.PointList;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.editors.assistants.ExtendedDataUtilities;
import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.Container;
import dk.itu.big_red.model.Control;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.InnerName;
import dk.itu.big_red.model.Layoutable;
import dk.itu.big_red.model.Link;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Node;
import dk.itu.big_red.model.OuterName;
import dk.itu.big_red.model.Port;
import dk.itu.big_red.model.Root;
import dk.itu.big_red.model.Site;
import dk.itu.big_red.model.assistants.Colour;
import dk.itu.big_red.model.assistants.Ellipse;
import dk.itu.big_red.model.load_save.SaveFailedException;
import dk.itu.big_red.model.load_save.Saver;

public class BigraphTikZSaver extends Saver {
	private BufferedWriter writer;
	
	@Override
	public Bigraph getModel() {
		return (Bigraph)super.getModel();
	}
	
	@Override
	public BigraphTikZSaver setModel(ModelObject model) {
		if (model == null || model instanceof Bigraph)
			super.setModel(model);
		return this;
	}
	
	private boolean completeDocument = false;
	
	public static final String OPTION_COMPLETE_DOCUMENT =
		"BigraphTikZExportWholeDocument";
	
	{
		addOption(OPTION_COMPLETE_DOCUMENT,
				"Produce a complete LaTeX document");
	}
	
	@Override
	public Object getOption(String id) {
		if (id.equals(OPTION_COMPLETE_DOCUMENT)) {
			return completeDocument;
		} else return super.getOption(id);
	}
	
	@Override
	public void setOption(String id, Object value) {
		if (id.equals(OPTION_COMPLETE_DOCUMENT)) {
			completeDocument = (Boolean)value;
		} else super.setOption(id, value);
	}
	
	@Override
	public void exportObject() throws SaveFailedException {
		writer = new BufferedWriter(new OutputStreamWriter(getOutputStream()));
		
		processBigraph(getModel());
		
		try {
			writer.close();
		} catch (IOException e) {
			throw new SaveFailedException(e);
		}
	}

	int scope = 0;
	
	private void line(String line) throws SaveFailedException {
		try {
			for (int i = 0; i < scope; i++)
				writer.write("  ");
			writer.write("\\" + line + "\n");
		} catch (Exception e) {
			throw new SaveFailedException(e);
		}
	}
	
	private void newLine() throws SaveFailedException {
		try {
			writer.write("\n");
		} catch (Exception e) {
			throw new SaveFailedException(e);
		}
	}
	
	private void beginScope(Container context) throws SaveFailedException {
		line("begin{scope} % " + getNiceName(context));
		scope++;
	}
	
	private void endScope() throws SaveFailedException {
		scope--;
		line("end{scope}");
	}
	
	private Point translate = null;
	
	private void processBigraph(Bigraph b) throws SaveFailedException {
		if (completeDocument) {
			line("documentclass{article}");
			line("usepackage{tikz}");
			line("begin{document}");
			
			newLine();
		}
		
		line("pgfdeclarelayer{connection}");
		line("pgfsetlayers{main,connection}");
		
		newLine();
		
		for (Control c : b.getSignature().getControls()) {
			String cN = c.getName();
			line("tikzset{" + cN + "/.style={}}");
		}
		
		newLine();
		
		line("tikzset{internal edge/.style={curve to,relative=false,fill=none}}");
		line("tikzset{internal port/.style={circle,fill=red,draw=none,minimum size=6,inner sep=0}}");
		line("tikzset{internal root/.style={dash pattern=on 2pt off 2pt}}");
		line("tikzset{internal site/.style={dash pattern=on 2pt off 2pt,fill=black!25}}");
		line("tikzset{internal inner name/.style={draw=none,rectangle}}");
		line("tikzset{internal outer name/.style={draw=none,rectangle}}");
		line("tikzset{internal name/.style={text=white,font=\\itshape}}");
		
		List<Layoutable> ch = b.getChildren();
		if (ch.size() > 0) {
			Rectangle bounding = ExtendedDataUtilities.getLayout(ch.get(0)).getCopy();
			for (Layoutable i : ch)
				bounding.union(ExtendedDataUtilities.getLayout(i));
			translate = bounding.getTopLeft().getNegated();
			System.out.println("(translating everything by " + translate + ")");
		}
		
		newLine();
		
		line("begin{tikzpicture}[x=0.02cm,y=-0.02cm]");
		
		beginScope(b);
		process(b.getEdges());
		process(b.getOuterNames());
		process(b.getInnerNames());
		endScope();
		
		line("end{tikzpicture}");
		
		if (completeDocument)
			line("end{document}");
	}
	
	private String getNiceName(Layoutable o) {
		String objectName = o.getName();
		if (objectName == null)
			objectName = o.toString();
		objectName = objectName.replaceAll("[^a-zA-Z0-9 ]", "_");
		if (o instanceof Node) {
			return "node " + objectName;
		} if (o instanceof Site) {
			return "site " + objectName;
		} else if (o instanceof Root) {
			return "root " + objectName;
		} else if (o instanceof InnerName) {
			return "inner name " + objectName;
		} else if (o instanceof Edge) {
			return "edge " + objectName;
		} else if (o instanceof OuterName) {
			return "outer name " + objectName;
		} else if (o instanceof Port) {
			Port p = (Port)o;
			return "port " + p.getName() + " on " + getNiceName(p.getParent());
		} else return objectName;
	}
	
	private void processNode(Node n) throws SaveFailedException {
		Control con = n.getControl();
		Rectangle rl = ExtendedDataUtilities.getRootLayout(n).translate(translate);
		Point rltl = rl.getTopLeft();
		
		Point tmp;
		String shapeDescriptor = "";
		Object shape = ExtendedDataUtilities.getShape(con);
		if (shape instanceof Ellipse) {
			tmp = rl.getCenter();
			shapeDescriptor += "(" + tmp.x + "," + tmp.y + ") ellipse (" +
				(rl.width() / 2) + " and " + (rl.height() / 2) + ")";
		} else if (shape instanceof PointList) {
			PointList fp = n.getFittedPolygon();
			fp.translate(rltl);
			tmp = new Point();
			for (int i = 0; i < fp.size(); i++) {
				fp.getPoint(tmp, i);
				shapeDescriptor += "(" + tmp.x + "," + tmp.y + ") -- ";
			}
			fp.getPoint(tmp, 0);
			shapeDescriptor += "(" + tmp.x + "," + tmp.y + ")";
		}
		
		Colour
			fillColour = ExtendedDataUtilities.getFill(n),
		    outlineColour = ExtendedDataUtilities.getOutline(n);
		
		line("definecolor{" + n.getName() + " fill}{RGB}{" + fillColour.getRed() + "," + fillColour.getGreen() + "," + fillColour.getBlue() + "}");
		line("definecolor{" + n.getName() + " outline}{RGB}{" + outlineColour.getRed() + "," + outlineColour.getGreen() + "," + outlineColour.getBlue() + "}");
		line("draw [" + n.getControl().getName() + ",fill=" + n.getName() + " fill,draw=" + n.getName()+ " outline] " + shapeDescriptor + ";");
		line("node at (" + rltl.x + "," + rltl.y + ") {" + ExtendedDataUtilities.getLabel(con) + "};");
		
		beginScope(n);
		process(n.getNodes());
		process(n.getSites());
		process(n.getPorts());
		endScope();
	}
	
	private void processPort(Port p) throws SaveFailedException {
		Rectangle rl = ExtendedDataUtilities.getRootLayout(p).translate(translate);
		Point tmp =
			rl.getCenter();
		line("node [internal port] (" + getNiceName(p) + ") at (" + tmp.x + "," + tmp.y + ") {};");
		processPoint(p);
	}
	
	private void processLink(Link e) throws SaveFailedException {
		Rectangle rl = ExtendedDataUtilities.getRootLayout(e).translate(translate);
		Point
			tl = rl.getTopLeft(),
			br = rl.getBottomRight(),
			c = rl.getCenter();
		Colour outlineColour = ExtendedDataUtilities.getOutline(e);
		line("definecolor{" + getNiceName(e) + " color}{RGB}{" + outlineColour.getRed() + "," + outlineColour.getGreen() + "," + outlineColour.getBlue() + "}"); 
		if (e instanceof OuterName) {
			line("draw [internal outer name,fill=" + getNiceName(e) + " color!50] (" + tl.x + "," + tl.y + ") rectangle (" + br.x + "," + br.y + ");");
			line("node [internal name] (" + getNiceName(e) + ") at (" + c.x + "," + c.y + ") {" + e.getName() + "};");
		} else if (e instanceof Edge) {
			line("node [inner sep=0] (" + getNiceName(e) + ") at (" + c.x + "," + c.y + ") {};");
		}
	}
	
	private void processInnerName(InnerName i) throws SaveFailedException {
		Rectangle rl = ExtendedDataUtilities.getRootLayout(i).translate(translate);
		Point
			tl = rl.getTopLeft(),
			br = rl.getBottomRight(),
			c = rl.getCenter();
		System.out.println(rl);
		Colour fillColour =
			(i.getLink() == null ? dk.itu.big_red.model.Point.DEFAULT_COLOUR : ExtendedDataUtilities.getOutline(i.getLink()));
		line("definecolor{" + getNiceName(i) + " color}{RGB}{" + fillColour.getRed() + "," + fillColour.getGreen() + "," + fillColour.getBlue() + "}");
		line("draw [internal inner name,fill=" + getNiceName(i) + " color!50] (" + tl.x + "," + tl.y + ") rectangle (" + br.x + "," + br.y + ");");
		line("node [internal name] (" + getNiceName(i) + ") at (" + c.x + "," + c.y + ") {" + i.getName() + "};");
		processPoint(i);
	}
	
	private void processPoint(dk.itu.big_red.model.Point p) throws SaveFailedException {
		Link l = p.getLink();
		if (l != null) {
			String in, out;
			Point source = ExtendedDataUtilities.getRootLayout(p).getCenter(),
			      target = ExtendedDataUtilities.getRootLayout(l).getCenter();
			System.out.println(source);
			System.out.println(target);
			if (source.y < target.y)
				in = "90";
			else in = "270";
			if (!(p instanceof InnerName)) {
				if (source.x < target.x)
					out = "0";
				else out = "180";
			} else {
				out = "90";
			}
			line("begin{pgfonlayer}{connection}");
			scope++;
			line("draw [internal edge,draw=" + getNiceName(l) + " color,in=" + in + ",out=" + out + "] (" + getNiceName(p) + ") to (" + getNiceName(l) + ");");
			scope--;
			line("end{pgfonlayer}");
		}
	}
	
	private void processSite(Site r) throws SaveFailedException {
		Rectangle rl = ExtendedDataUtilities.getRootLayout(r).translate(translate);
		Point
			ptl = rl.getTopLeft(),
			ptr = rl.getBottomRight();
		
		line("draw [internal site] (" + ptl.x + "," +
				ptl.y + ") rectangle(" + ptr.x + "," + ptr.y + ");");
	}
	
	private void processRoot(Root r) throws SaveFailedException {
		Rectangle rl = ExtendedDataUtilities.getRootLayout(r).translate(translate);
		Point
			ptl = rl.getTopLeft(),
			ptr = rl.getBottomRight();
		
		line("draw [internal root] (" + ptl.x + "," +
				ptl.y + ") rectangle(" + ptr.x + "," + ptr.y + ");");
		
		beginScope(r);
		process(r.getNodes());
		process(r.getSites());
		endScope();
	}
	
	private void process(Iterable<? extends Layoutable> l)
			throws SaveFailedException {
		for (Layoutable i : l)
			process(i);
	}
	
	private void process(Layoutable obj) throws SaveFailedException {
		if (obj instanceof Node) {
			processNode((Node)obj);
		} else if (obj instanceof Port) {
			processPort((Port)obj);
		} else if (obj instanceof Link) {
			processLink((Link)obj);
		} else if (obj instanceof InnerName) {
			processInnerName((InnerName)obj);
		} else if (obj instanceof Site) {
			processSite((Site)obj);
		} else if (obj instanceof Root) {
			processRoot((Root)obj);
		} else if (obj instanceof Bigraph) {
			processBigraph((Bigraph)obj);
		} else {
			/* do nothing */
		}
	}
}
