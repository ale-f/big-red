package dk.itu.big_red.model;

import dk.itu.big_red.model.interfaces.IParent;
import dk.itu.big_red.model.interfaces.ISite;

/**
 * @author alec
 * @see ISite
 */
public class Site extends Layoutable implements ISite {
	protected abstract class SiteChange extends LayoutableChange {
		@Override
		public Site getCreator() {
			return Site.this;
		}
	}
	
	public class ChangeAlias extends SiteChange {
		public String alias;
		
		protected ChangeAlias(String alias) {
			this.alias = alias;
		}

		private String oldAlias;
		
		@Override
		public void beforeApply() {
			oldAlias = getCreator().getAlias();
		}
		
		@Override
		public boolean canInvert() {
			return true;
		}
		
		@Override
		public ModelObjectChange inverse() {
			return getCreator().changeAlias(oldAlias);
		}

	}
	
	@Override
	public IParent getIParent() {
		return (IParent)getParent();
	}
	
	/**
	 * The property name fired when the alias changes. The values are {@link
	 * String}s.
	 */
	public static final String PROPERTY_ALIAS = "SiteAlias";
	
	private String alias = null;
	
	/**
	 * Gets this {@link Site}'s current alias.
	 * @return the current alias (can be <code>null</code>)
	 */
	public String getAlias() {
		return alias;
	}
	
	@Override
	public Object getProperty(String name) {
		if (name.equals(PROPERTY_ALIAS)) {
			return getAlias();
		} else return super.getProperty(name);
	}
	
	/**
	 * Sets this {@link Site}'s alias.
	 * @param alias the new alias
	 */
	protected void setAlias(String alias) {
		String oldAlias = this.alias;
		this.alias = alias;
		firePropertyChange(PROPERTY_ALIAS, oldAlias, alias);
	}
	
	public ChangeAlias changeAlias(String alias) {
		return new ChangeAlias(alias);
	}
}
