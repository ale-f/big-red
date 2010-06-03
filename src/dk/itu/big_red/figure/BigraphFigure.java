package dk.itu.big_red.figure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.XYLayout;

public class BigraphFigure extends AbstractFigure {
	private Label labelName = new Label();    
       
	public BigraphFigure() {
		XYLayout layout = new XYLayout();
		setLayoutManager(layout);
		
		labelName.setForegroundColor(ColorConstants.black);
		add(labelName, 0);
	}

	public void setName(String text) {
		labelName.setText(text);
	}

	@Override
	protected void fillShape(Graphics graphics) {
		return;
	}

	@Override
	protected void outlineShape(Graphics graphics) {
		return;
	}
}
