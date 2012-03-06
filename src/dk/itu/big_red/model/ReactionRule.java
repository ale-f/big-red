package dk.itu.big_red.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;

import dk.itu.big_red.model.Container.ChangeAddChild;
import dk.itu.big_red.model.Container.ChangeRemoveChild;
import dk.itu.big_red.model.Edge.ChangeReposition;
import dk.itu.big_red.model.Layoutable.ChangeLayout;
import dk.itu.big_red.model.Layoutable.ChangeName;
import dk.itu.big_red.model.Point.ChangeConnect;
import dk.itu.big_red.model.Point.ChangeDisconnect;
import dk.itu.big_red.model.Site.ChangeAlias;
import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.namespaces.INamespace;

public class ReactionRule extends ModelObject {
	private Bigraph redex, reactum;
	private Map<ModelObject, ModelObject> redexToReactum =
		new HashMap<ModelObject, ModelObject>();
	private ChangeGroup changes;
	public static final String CONTENT_TYPE = "dk.itu.big_red.rule";
	
	public static final String PROPERTY_REDEX = "ReactionRuleRedex";
	
	public Bigraph getRedex() {
		return redex;
	}
	
	public void setRedex(Bigraph redex) {
		Bigraph old = this.redex;
		this.redex = redex;
		
		reactum = null;
		redexToReactum.clear();
		
		if (redex != null)
			redex.setFile(getFile());
		
		firePropertyChange(PROPERTY_REDEX, old, redex);
	}

	public Bigraph getReactum() {
		if (reactum == null)
			reactum = redex.clone(redexToReactum);
		return reactum;
	}
	
	public Map<ModelObject, ModelObject> getRedexToReactumMap() {
		return redexToReactum;
	}
	
	public static Change translateChange(
			Map<ModelObject, ModelObject> oldToNew, Change change) {
		if (change instanceof ChangeGroup) {
			ChangeGroup cg_ = (ChangeGroup)change,
				cg = new ChangeGroup();
			for (Change i : cg_) {
				Change iP = translateChange(oldToNew, i);
				if (iP != null) {
					cg.add(iP);
				} else {
					cg.clear();
					return null;
				}
			}
			
			return cg;
		} else if (change instanceof ChangeAddChild) {
			ChangeAddChild ch = (ChangeAddChild)change;
			
			Container reactumParent = (Container)oldToNew.get(ch.getCreator());
			Layoutable reactumChild = (Layoutable)oldToNew.get(ch.child);
			
			if (reactumParent == null)
				return null;
			if (reactumChild == null)
				reactumChild = ch.child.clone(oldToNew);
			
			/*
			 * XXX: a BigraphScratchpad should really be used here so that
			 * ChangeGroups will actually work properly
			 */
			String reactumName;
			INamespace<Layoutable> reactumNamespace =
				reactumParent.getBigraph().
				getNamespace(Bigraph.getNSI(reactumChild));
			if (reactumNamespace.get(ch.name) == null) {
				reactumName = ch.name;
			} else reactumName = reactumNamespace.getNextName();
			
			return reactumParent.changeAddChild(reactumChild, reactumName);
		} else if (change instanceof ChangeLayout) {
			ChangeLayout ch = (ChangeLayout)change;
			
			Layoutable reactumModel = (Layoutable)oldToNew.get(ch.getCreator());
			
			if (reactumModel == null)
				return null;
			
			return reactumModel.changeLayout(ch.newLayout.getCopy());
		} else if (change instanceof ChangeRemoveChild) {
			ChangeRemoveChild ch = (ChangeRemoveChild)change;
			
			Container reactumParent = (Container)oldToNew.get(ch.getCreator());
			Layoutable reactumChild = (Layoutable)oldToNew.get(ch.child);
			
			if (reactumParent == null || reactumChild == null)
				return null;
			
			oldToNew.remove(ch.child);
			return reactumParent.changeRemoveChild(reactumChild);
		} else if (change instanceof ChangeName) {
			ChangeName ch = (ChangeName)change;
			
			Layoutable reactumModel = (Layoutable)oldToNew.get(ch.getCreator());
			if (reactumModel == null)
				return null;
			
			return reactumModel.changeName(ch.newName);
		} else if (change instanceof ChangeConnect) {
			ChangeConnect ch = (ChangeConnect)change;
			
			Point reactumPoint = (Point)oldToNew.get(ch.getCreator());
			Link reactumLink = (Link)oldToNew.get(ch.link);
			if (reactumPoint == null || reactumLink == null)
				return null;
			
			return reactumPoint.changeConnect(reactumLink);
		} else if (change instanceof ChangeDisconnect) {
			ChangeDisconnect ch = (ChangeDisconnect)change;
			
			Point reactumPoint = (Point)oldToNew.get(ch.getCreator());
			Link reactumLink = (Link)oldToNew.get(ch.link);
			if (reactumPoint == null || reactumLink == null)
				return null;
			
			return reactumPoint.changeDisconnect(reactumLink);
		} else if (change instanceof ChangeAlias) {
			ChangeAlias ch = (ChangeAlias)change;
			
			Site reactumSite = (Site)oldToNew.get(ch.getCreator());
			if (reactumSite == null)
				return null;
			
			return reactumSite.changeAlias(ch.alias);
		} else if (change instanceof ChangeReposition) {
			ChangeReposition ch = (ChangeReposition)change;
			
			Edge reactumEdge = (Edge)oldToNew.get(ch.getCreator());
			if (reactumEdge == null)
				return null;
			
			return reactumEdge.changeReposition();
		} else if (change instanceof ChangeComment) {
			ChangeComment ch = (ChangeComment)change;
			
			ModelObject reactumObject = oldToNew.get(ch.getCreator());
			if (reactumObject == null)
				return null;
			
			return reactumObject.changeComment(ch.comment);
		} else throw new Error(change + " unrecognised");
	}
	
	@Override
	public ReactionRule clone(Map<ModelObject, ModelObject> m) {
		if (m == null)
			m = new HashMap<ModelObject, ModelObject>();
		ReactionRule rr = (ReactionRule)super.clone(m);
		
		Map<ModelObject, ModelObject>
			/* redex to reactum */
			rR = getRedexToReactumMap(),
			/* redex to cloned redex */
			rCr = new HashMap<ModelObject, ModelObject>(),
			/* reactum to cloned reactum */
			RCR = new HashMap<ModelObject, ModelObject>(),
			/* cloned redex to cloned reactum */
			CrCR = rr.getRedexToReactumMap();
		
		rr.setFile(getFile());
		rr.setRedex(getRedex().clone(rCr));
		
		try {
			getReactum().tryApplyChange(getChanges().inverse());
		} catch (ChangeRejectedException cre) {
			/* very bad news */
			cre.printStackTrace();
		}
		rr.setReactum(getReactum().clone(RCR));
		
		ChangeGroup cg = rr.getChanges();
		for (Change c : getChanges()) {
			try {
				Change cP = translateChange(RCR, c);
				rr.getReactum().tryApplyChange(cP);
				cg.add(cP);
				
				getReactum().tryApplyChange(c);
			} catch (ChangeRejectedException cre) {
				/* very bad news */
				cre.printStackTrace();
			}
		}
		
		for (Entry<ModelObject, ModelObject> e : rCr.entrySet())
			CrCR.put(e.getValue(), RCR.get(rR.get(e.getKey())));
		
		if (m != null) {
			m.putAll(rCr);
			m.putAll(RCR);
		}
		
		return rr;
	}
	
	private void setReactum(Bigraph b) {
		reactum = b;
	}
	
	public ChangeGroup getChanges() {
		if (changes == null)
			changes = new ChangeGroup();
		return changes;
	}
	
	@Override
	public void dispose() {
		redex.dispose();
		reactum.dispose();
		redex = reactum = null;
		redexToReactum.clear();
		redexToReactum = null;
		changes.clear();
		
		super.dispose();
	}
	
	@Override
	public ReactionRule setFile(IFile file) {
		if (redex != null)
			redex.setFile(file);
		return (ReactionRule)super.setFile(file);
	}
}
