package dk.itu.big_red.model.interfaces.internal;

import java.util.List;

import dk.itu.big_red.editpolicies.EdgeCreationPolicy;
import dk.itu.big_red.model.Edge;
import dk.itu.big_red.model.LinkConnection;

/**
 * Objects implementing IConnectable are those which can be connected to by an
 * {@link Edge}. (This implies that they have a layout, so this interface
 * inherits from {@link ILayoutable}.)
 * 
 * <p>(This interface is chiefly intended for the use of instances of
 * {@link EdgeCreationPolicy} - it allows objects to be notified of connections
 * that <i>have</i> been established to them, but doesn't define policies for
 * when such a connection should be allowed.)
 * @author alec
 *
 */
public interface IConnectable extends ILayoutable {
	/**
	 * Registers the given {@link LinkConnection} as being connected to this
	 * object.
	 * @param e an LinkConnection
	 */
	public void addConnection(LinkConnection e);
	
	/**
	 * Unregisters the given {@link LinkConnection} from being connected to
	 * this object.
	 * @param e an LinkConnection
	 */
	public void removeConnection(LinkConnection e);
	
	/**
	 * Returns the set of {@link LinkConnection}s incident on this object.
	 * @return a {@link List} of EdgeConnections
	 */
	public List<LinkConnection> getConnections();
}
