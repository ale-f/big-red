package dk.itu.big_red.model.interfaces;

import dk.itu.big_red.model.Edge;

/**
 * Objects implementing IConnectable are those which can be connected to by an
 * {@link Edge}.
 * @author alec
 *
 */
public interface IConnectable {
	/**
	 * Attempts to connect the given {@link Edge} to this object. (Note that
	 * this is the <i>only</i> method you should have to call to establish a
	 * connection.)
	 * 
	 * <p>To see if the connection was established, compare the return values
	 * of {@link IConnectable#isConnected isConnected} before and after a call
	 * to this method.
	 * @param e an Edge
	 */
	void connect(Edge e);
	
	/**
	 * Attempts to disconnect the given {@link Edge} from this object.
	 * 
	 * <p>To see if the Edge was disconnected, compare the return values of
	 * {@link IConnectable#isConnected isConnected} before and after a call to
	 * this method.
	 * @param e an Edge
	 */
	void disconnect(Edge e);
	
	/**
	 * Indicates whether or not the given {@link Edge} is connected to this
	 * object.
	 * @param e an Edge
	 * @return <code>true</code> if the edge is valid and connected, or
	 *         <code>false</code> otherwise
	 */
	boolean isConnected(Edge e);
}
