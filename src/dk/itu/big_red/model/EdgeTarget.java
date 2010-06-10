package dk.itu.big_red.model;

import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.interfaces.IConnectable;

/**
 * An EdgeTarget is a small object used to keep {@link Edge}s as close to the
 * formal bigraphical model as possible. GEF/GMF requires that every connection
 * joins <i>two</i> objects; the EdgeTarget provides a target for multiple
 * connections, so multi-point bigraphical edges can be constructed quite
 * happily. 
 * @author alec
 *
 */
public class EdgeTarget implements IConnectable {

	@Override
	public void connect(Edge e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disconnect(Edge e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isConnected(Edge e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Rectangle getLayout() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setLayout(Rectangle layout) {
		// TODO Auto-generated method stub
		
	}

}
