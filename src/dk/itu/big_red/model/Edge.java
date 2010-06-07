package dk.itu.big_red.model;

import java.beans.PropertyChangeSupport;


import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;

import dk.itu.big_red.part.EdgePart;
import dk.itu.big_red.propertysources.EdgePropertySource;

public class Edge implements IAdaptable {
	private PropertyChangeSupport listeners = new PropertyChangeSupport(this);
	
	public static final String PROPERTY_SOURCE = "EdgeSource";
	public static final String PROPERTY_TARGET = "EdgeTarget";
	public static final String PROPERTY_COMMENT = "EdgeComment";
	public static final String PROPERTY_SOURCE_KEY = "EdgeSourceKey";
	public static final String PROPERTY_TARGET_KEY = "EdgeTargetKey";
	
	private String sourceKey, targetKey;
	private Thing source, target;
	
	private IPropertySource propertySource = null;

	private String comment = null;
	
	public void setSource(Thing source, String key) {
		if (source != null) {
			Thing oldSource = this.source;
			String oldSourceKey = this.sourceKey;
			this.source = source;
			this.sourceKey = key;
			listeners.firePropertyChange(PROPERTY_SOURCE, oldSource, source);
			listeners.firePropertyChange(PROPERTY_SOURCE_KEY, oldSourceKey, key);
		}
	}
	public Thing getSource() {
		return source;
	}
	public String getSourceKey() {
		return sourceKey;
	}
	
	public void setTarget(Thing target, String key) {
		if (target != null) {
			Thing oldTarget = this.target;
			String oldTargetKey = this.targetKey;
			this.target = target;
			this.targetKey = key;
			listeners.firePropertyChange(PROPERTY_TARGET, oldTarget, target);
			listeners.firePropertyChange(PROPERTY_SOURCE_KEY, oldTargetKey, key);
		}
	}
	public Thing getTarget() {
		return target;
	}
	public String getTargetKey() {
		return targetKey;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			if (propertySource == null) {
				propertySource = new EdgePropertySource(this);
			}
			return propertySource;
		}
		return null;
	}

	public void addPropertyChangeListener(EdgePart edgePart) {
		listeners.addPropertyChangeListener(edgePart);
	}
	
	public void removePropertyChangeListener(EdgePart edgePart) {
		listeners.addPropertyChangeListener(edgePart);
	}
	
	public String getComment() {
		return this.comment;
	}
	
	public void setComment(String comment) {
		String oldComment = getComment();
		this.comment = comment;
		listeners.firePropertyChange(PROPERTY_COMMENT, oldComment, comment);
	}
	
	public boolean sourceOK(Thing source) {
		return (source != null && source != this.source && source != this.target);
	}
	
	public boolean targetOK(Thing target) {
		return (target != null && target != this.source && target != this.target);
	}
}
