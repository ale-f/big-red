package dk.itu.big_red.editors.bigraph.parts;

import org.eclipse.gef.EditPart;

import dk.itu.big_red.model.Bigraph;

/**
 * Objects implementing <strong>IBigraphPart</strong> are {@link EditPart}s
 * representing objects on a {@link Bigraph}.
 * @author alec
 *
 */
public interface IBigraphPart extends EditPart {
	/**
	 * Gets the {@link Bigraph} containing this {@link IBigraphPart}'s model
	 * object.
	 * @return a {@link Bigraph}
	 */
	public Bigraph getBigraph();
}
