package org.bigraph.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.bigraph.model.ModelObject;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.assistants.validators.BigraphIntegrityValidator;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.IChangeExecutor;
import org.bigraph.model.changes.IChangeValidator;
import org.bigraph.model.interfaces.IBigraph;
import org.bigraph.model.names.Namespace;
import org.bigraph.model.names.NamespaceGroup;
import org.bigraph.model.names.policies.PositiveIntegerNamePolicy;
import org.bigraph.model.names.policies.StringNamePolicy;

/**
 * The Bigraph is the root of any agent, and contains {@link Root}s, {@link
 * InnerName}s, and {@link OuterName}s.
 * @author alec
 * @see IBigraph
 */
public class Bigraph extends Container
		implements IBigraph, IChangeExecutor, ModelObject.Identifier.Resolver {
	private Signature signature = null;

	private NamespaceGroup<Layoutable> nsg = new NamespaceGroup<Layoutable>();
	
	{
		nsg.createNamespace(Link.class).setPolicy(new StringNamePolicy());
		nsg.createNamespace(Node.class).setPolicy(new StringNamePolicy());
		nsg.createNamespace(InnerName.class).setPolicy(new StringNamePolicy());
		
		nsg.createNamespace(Root.class).setPolicy(new PositiveIntegerNamePolicy());
		nsg.createNamespace(Site.class).setPolicy(new PositiveIntegerNamePolicy());
	}
	
	/**
	 * Returns the <i>namespace identifier</i> for the given {@link
	 * Layoutable}, which identifies the scope in which its name must be
	 * unique.
	 * <p>{@link InnerName}s, {@link Root}s, {@link Site}s and {@link Node}s
	 * all have separate namespaces, all {@link Link}s occupy the same
	 * namespace, and all other objects have no restrictions on their names.
	 * @param object a {@link Layoutable}
	 * @return the scope within which <code>object</code>'s name must be
	 * unique, or <code>object</code> if there are no restrictions on its name
	 */
	public static Class<? extends Layoutable> getNSI(Layoutable object) {
		return getNSI(object.getType());
	}
	
	/**
	 * @see #getNSI(Layoutable)
	 */
	public static Class<? extends Layoutable> getNSI(String objectType) {
		objectType = objectType.toLowerCase(Locale.ENGLISH);
		if (objectType.equals("edge") || objectType.equals("outername") || objectType.equals("link")) {
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
		return nsg.getNamespace(nsi);
	}
	
	/**
	 * Gets the first unused name suitable for the given {@link Layoutable}.
	 * @param l a {@link Layoutable}
	 * @return a {@link String} suitable for a {@link BigraphChangeName}
	 */
	public String getFirstUnusedName(Layoutable l) {
		return getNamespace(getNSI(l)).getNextName();
	}
	
	private IChangeValidator validator = new BigraphIntegrityValidator(this);

	public static final String CONTENT_TYPE = "dk.itu.big_red.bigraph";
	
	/**
	 * Creates and returns a complete copy of this {@link Bigraph}, complete
	 * with {@link Point}s and {@link Link} connections.
	 * @param m a {@link CloneMap}; can be <code>null</code>
	 * @return an exact copy of this {@link Bigraph}
	 */
	@Override
	public Bigraph clone(Map<ModelObject, ModelObject> m) {
		if (m == null)
			m = new HashMap<ModelObject, ModelObject>();
		Bigraph b = (Bigraph)newInstance();
		
		b.setSignature(getSignature().clone(m));
		
		/* ModelObject.clone */
		m.put(this, b);
		b.setExtendedDataFrom(this);
		
		/* Container.clone */
		for (Layoutable child : getChildren())
			b.addChild(child.clone(m));
		
		for (Link i : only(null, Link.class)) {
			Link iClone = (Link)m.get(i);
			for (Point p : i.getPoints())
				iClone.addPoint((Point)m.get(p));
		}
		
		for (Entry<ModelObject, ModelObject> e : m.entrySet()) {
			ModelObject o = e.getKey();
			if (o instanceof Layoutable) {
				Layoutable l = (Layoutable)o,
						lClone = (Layoutable)e.getValue();
				lClone.setName(l.getName());
				Namespace<Layoutable> ns = b.getNamespace(getNSI(lClone));
				if (ns != null)
					ns.put(lClone.getName(), lClone);
			}
		}
		
		return b;
	}
	
	@Override
	public boolean canContain(Layoutable child) {
		Class<? extends Layoutable> c = child.getClass();
		return (c == Root.class || c == InnerName.class || c == OuterName.class || c == Edge.class);
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
	public List<Edge> getEdges() {
		return only(null, Edge.class);
	}

	@Override
	public List<Root> getRoots() {
		return only(null, Root.class);
	}

	@Override
	public List<InnerName> getInnerNames() {
		return only(null, InnerName.class);
	}
	
	@Override
	public List<Site> getSites() {
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
	public List<OuterName> getOuterNames() {
		return only(null, OuterName.class);
	}
	
	@Override
	public void tryValidateChange(IChange b) throws ChangeRejectedException {
		validator.tryValidateChange(b);
	}
	
	@Override
	public void tryApplyChange(IChange b) throws ChangeRejectedException {
		tryValidateChange(b);
		doChange(b);
	}
	
	@Override
	protected boolean doChange(IChange b) {
		if (super.doChange(b)) {
			/* do nothing */
		} else if (b instanceof Point.ChangeConnect) {
			Point.ChangeConnect c = (Point.ChangeConnect)b;
			c.link.addPoint(c.getCreator());
		} else if (b instanceof Point.ChangeDisconnect) {
			Point.ChangeDisconnect c = (Point.ChangeDisconnect)b;
			c.getCreator().getLink().removePoint(c.getCreator());
		} else if (b instanceof Container.ChangeAddChild) {
			Container.ChangeAddChild c = (Container.ChangeAddChild)b;
			c.child.setName(
					getNamespace(getNSI(c.child)).put(c.name, c.child));
			c.getCreator().addChild(c.child);
		} else if (b instanceof Layoutable.ChangeRemove) {
			Layoutable.ChangeRemove c = (Layoutable.ChangeRemove)b;
			Layoutable ch = c.getCreator();
			ch.getParent().removeChild(ch);
			getNamespace(getNSI(ch)).remove(ch.getName());
		} else if (b instanceof Layoutable.ChangeName) {
			Layoutable.ChangeName c = (Layoutable.ChangeName)b;
			getNamespace(getNSI(c.getCreator())).remove(c.getCreator().getName());
			c.getCreator().setName(
					getNamespace(getNSI(c.getCreator())).put(
							c.newName, c.getCreator()));
		} else return false;
		return true;
	}
	
	@Override
	public void dispose() {
		signature.dispose();
		signature = null;
		
		super.dispose();
	}
	
	public static final class Identifier extends Container.Identifier {
		public Identifier() {
			super(null);
		}
		
		@Override
		public Bigraph lookup(PropertyScratchpad context, Resolver r) {
			return require(r, Bigraph.class);
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

	@Override
	public Object lookup(
			PropertyScratchpad context, Object type_, String name) {
		if (type_ instanceof Class<?>) {
			Class<?> type = (Class<?>)type_;
			if (Layoutable.class.isAssignableFrom(type)) {
				Namespace<Layoutable> ns = nsg.getNamespace(type);
				return (ns != null ? ns.get(context, name) : null);
			} else if (Control.class.equals(type)) {
				return getSignature().lookup(context, type, name);
			}
		}
		return null;
	}
}
