package dk.itu.big_red.editors.bigraph.figures;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.swt.SWT;

import dk.itu.big_red.editors.bigraph.figures.assistants.CurvyConnectionRouter;

public class LinkConnectionFigure extends PolylineConnection {
	public LinkConnectionFigure() {
		setAntialias(SWT.ON);
        setLineStyle(org.eclipse.swt.SWT.LINE_SOLID);
        setConnectionRouter(new CurvyConnectionRouter());
	}
	
	public void setToolTip(String content) {
		String labelText = "Edge";
		if (content != null)
			labelText += "\n\n" + content;
		
		Label label = new Label(labelText);
		label.setBorder(new MarginBorder(4));
		super.setToolTip(label);
	}
}
