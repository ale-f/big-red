package org.bigraph.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.bigraph.model.assistants.IObjectIdentifier;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.interfaces.IBigraph;
import org.bigraph.model.names.HashMapNamespace;
import org.bigraph.model.names.Namespace;
import org.bigraph.model.names.policies.BoundedIntegerNamePolicy;
import org.bigraph.model.names.policies.StringNamePolicy;

/**
 * The Bigraph is the root of any agent, and contains {@link Root}s, {@link
 * InnerName}s, and {@link OuterName}s.
 * @author alec
 * @see IBigraph
 */
public class Bigraph extends Container
		implements IBigraph, IObjectIdentifier.Resolver {
	private Signature signature = null;

	private Namespace<Layoutable>
		linkNS = new HashMapNamespace<Layoutable>(new StringNamePolicy()),
		nodeNS = new HashMapNamespace<Layoutable>(new StringNamePolicy()),
		innerNameNS = new HashMapNamespace<Layoutable>(new StringNamePolicy()),
		rootNS = new HashMapNamespace<Layoutable>(
				new BoundedIntegerNamePolicy(0)),
		siteNS = new HashMapNamespace<Layoutable>(
				new BoundedIntegerNamePolicy(0));
	
	/**
	 * Returns the <i>namespace identifier</i> for a {@link Layoutable}, which
	 * identifies the scope in which its name must be unique.
	 * <p>{@link InnerName}s, {@link Root}s, {@link Site}s and {@link Node}s
	 * all have separate namespaces, all {@link Link}s occupy the same
	 * namespace, and all other objects have no restrictions on their names.
	 * @param objectType a {@link String} returned by {@link
	 * Layoutable#getType()}
	 * @return the scope within which <code>object</code>'s name must be
	 * unique, or <code>null</code> if there are no restrictions on its name
	 */
	public static Class<? extends Layoutable> getNSI(String objectType) {
		objectType = objectType.toLowerCase(Locale.ENGLISH);
		if (objectType.equals("edge") || objectType.equals("outername")) {
			return Link.class;
		} else if (objectType.equals("innername")) {
			return InnerName.class;
		} else if (objectType.equals("root")) {
			return Root.class;
		} else if (objectType.equals("site")) {
			return Site.class;
		} else if (objectType.equals("node")) {
			return Node.class;
		} else return null;
	}
	
	/**
	 * Gets the namespace with the given namespace identifier.
	 * @param nsi a value returned from a call to {@link #getNSI(Layoutable)},
	 * unless you're <i>very</i> sure you know what you're doing
	 * @return the specified namespace
	 */
	public Namespace<Layoutable> getNamespace(
			Class<? extends Layoutable> nsi) {
		if (Link.class.equals(nsi)) {
			return linkNS;
		} else if (InnerName.class.equals(nsi)) {
			return innerNameNS;
		} else if (Root.class.equals(nsi)) {
			return rootNS;
		} else if (Site.class.equals(nsi)) {
			return siteNS;
		} else if (Node.class.equals(nsi)) {
			return nodeNS;
		} else return null;
	}
	
	public Namespace<Layoutable> getNamespace(Layoutable l) {
		return getNamespace(getNSI(l.getType()));
	}
	
	/**
	 * Gets the first unused name suitable for the given {@link Layoutable}.
	 * @param l a {@link Layoutable}
	 * @return a {@link String} suitable for a {@link BigraphChangeName}
	 */
	public String getFirstUnusedName(Layoutable l) {
		return getNamespace(l).getNextName();
	}

	public static final String CONTENT_TYPE = "dk.itu.big_red.bigraph";
	
	/**
	 * Creates and returns a complete copy of this {@link Bigraph}, complete
	 * with {@link Point}s and {@link Link} connections.
	 * @param m a {@link CloneMap}; can be <code>null</code>
	 * @return an exact copy of this {@link Bigraph}
	 */
	@Override
	public Bigraph clone() {
		Bigraph b = (Bigraph)newInstance();
		
		b.setSignature(getSignature().clone());
		
		/* ModelObject.clone */
		b.setExtendedDataFrom(this);
		
		/* Container.clone */
		for (Layoutable child : getChildren())
			b.addChild(child.clone(b));
		
		for (Link i : only(null, Link.class)) {
			Link iClone = i.getIdentifier().lookup(null, b);
			for (Point p : i.getPoints())
				iClone.addPoint(p.getIdentifier().lookup(null, b));
		}
		
		return b;
	}
	
	@Override
	public Bigraph getBigraph(PropertyScratchpad context) {
		return this;
	}
	
	public void setSignature(Signature signature) {
		this.signature = signature;
	}
	
	@Override
	public Signature getSignature() {
		return signature;
	}
	
	@Override
	void setParent(Container parent) {
		/* do nothing */
	}
	
	@Override
	public Container getParent() {
		return null;
	}

	@Override
	public Collection<? extends Edge> getEdges() {
		return only(null, Edge.class);
	}

	@Override
	public Collection<? extends Root> getRoots() {
		return only(null, Root.class);
	}

	@Override
	public Collection<? extends InnerName> getInnerNames() {
		return only(null, InnerName.class);
	}
	
	@Override
	public Collection<? extends Site> getSites() {
		ArrayList<Site> sites = new ArrayList<Site>();
		ArrayDeque<Container> queue = new ArrayDeque<Container>();
		queue.add(this);
		
		while (!queue.isEmpty()) {
			for (Layoutable l : queue.remove().getChildren()) {
				if (l instanceof Site) {
					sites.add((Site)l);
				} else if (l instanceof Container) {
					queue.add((Container)l);
				}
			}
		}
		
		return sites;
	}
	
	@Override
	public Collection<? extends OuterName> getOuterNames() {
		return only(null, OuterName.class);
	}
	
	@Override
	public void dispose() {
		if (signature != null) {
			signature.dispose();
			signature = null;
		}
		
		super.dispose();
	}
	
	public static final class Identifier extends Container.Identifier {
		public Identifier() {
			super(null);
		}
		
		@Override
		public Bigraph lookup(PropertyScratchpad context, Resolver r) {
			return require(r.lookup(context, this), Bigraph.class);
		}
		
		@Override
		public Identifier getRenamed(String name) {
			return new Identifier();
		}
		
		@Override
		public String toString() {
			return "bigraph";
		}
	}
	
	@Override
	public Identifier getIdentifier() {
		return getIdentifier(null);
	}
	
	@Override
	public Identifier getIdentifier(PropertyScratchpad context) {
		return new Identifier();
	}

	private final Store store = new Store();
	
	@Override
	public Object lookup(PropertyScratchpad context,
			IObjectIdentifier identifier) {
		if (identifier instanceof Bigraph.Identifier) {
			return this;
		} else if (identifier instanceof Layoutable.Identifier) {
			String name = ((Layoutable.Identifier)identifier).getName();
			if (identifier instanceof Node.Identifier) {
				return getNamespace(Node.class).get(context, name);
			} else if (identifier instanceof Root.Identifier) {
				return getNamespace(Root.class).get(context, name);
			} else if (identifier instanceof Site.Identifier) {
				return getNamespace(Site.class).get(context, name);
			} else if (identifier instanceof Link.Identifier) {
				return getNamespace(Link.class).get(context, name);
			} else if (identifier instanceof InnerName.Identifier) {
				return getNamespace(InnerName.class).get(context, name);
			} else return null;
		} else if (identifier instanceof Control.Identifier) {
			return getSignature().lookup(context, identifier);
		} else if (identifier instanceof Store.EntryIdentifier) {
			return store.lookup(context, identifier);
		} else return null;
	}
}
