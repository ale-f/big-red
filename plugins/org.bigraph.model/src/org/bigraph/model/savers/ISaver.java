package org.bigraph.model.savers;

import java.util.List;

import org.bigraph.model.resources.IFileWrapper;

/**
 * Classes implementing <strong>ISaver</strong> are <em>savers</em>.
 * @author alec
 */
public interface ISaver {
	/**
	 * Classes implementing <strong>Participant</strong> participate in a
	 * {@link ISaver}'s saving process.
	 * @author alec
	 */
	interface Participant {
		void setSaver(ISaver saver);
	}
	
	/**
	 * Classes implementing <strong>InheritableParticipant</strong> are {@link
	 * Participant}s that should be propagated down to sub-savers.
	 * @author alec
	 */
	interface InheritableParticipant extends Participant {
		InheritableParticipant newInstance();
	}
	
	ISaver getParent();
	
	void addParticipant(Participant p);
	Iterable<? extends Participant> getParticipants();
	
	/**
	 * Classes implementing <strong>Option</strong> are <em>options</em>: they
	 * influence the behaviour of an {@link ISaver}.
	 * @author alec
	 */
	interface Option {
		/**
		 * Returns the (human-readable) name of this {@link Option}
		 * @return the name
		 */
		String getName();
		
		/**
		 * Returns the (human-readable) extended description of this {@link
		 * Option}.
		 * @return the extended description; can be {@code null}
		 */
		String getDescription();
		
		/**
		 * Returns the current value of this {@link Option}.
		 * @return the current value
		 * @see #set(Object)
		 */
		Object get();
		
		/**
		 * Sets the value of this {@link Option}.
		 * @param value the new value
		 * @see #get()
		 */
		void set(Object value);
		
		/**
		 * Returns this {@link Option}'s <i>cookie</i>.
		 * <p>(Two Options with {@link Object#equals(Object) equal} cookies are
		 * considered to represent the same option.)
		 * @return an opaque, comparable object
		 */
		Object getCookie();
	}

	/**
	 * Adds an {@link Option} to this {@link ISaver}.
	 * @param o an {@link Option}
	 */
	void addOption(Option o);
	
	/**
	 * Returns this {@link ISaver}'s list of {@link Option}s.
	 * @return an (unmodifiable) list of options
	 */
	List<? extends Option> getOptions();
	
	/**
	 * Returns the {@link IFileWrapper} associated with this {@link ISaver}.
	 * @return an {@link IFileWrapper}; can be {@code null}
	 */
	IFileWrapper getFile();
}
