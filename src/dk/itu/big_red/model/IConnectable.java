package dk.itu.big_red.model;

/**
 * Objects implementing IConnectable are those which can be connected to by an
 * {@link Edge}.
 * @author alec
 *
 */
public interface IConnectable {
	void connect(Edge e);
	void disconnect(Edge e);
}
