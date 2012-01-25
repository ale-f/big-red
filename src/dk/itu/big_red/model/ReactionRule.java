package dk.itu.big_red.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;

import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.utilities.resources.IFileBackable;

public class ReactionRule extends ModelObject implements IFileBackable {
	private Bigraph redex, reactum;
	private Map<ModelObject, ModelObject> redexToReactum =
		new HashMap<ModelObject, ModelObject>();
	private ChangeGroup changes;
	
	public static final String PROPERTY_REDEX = "ReactionRuleRedex";
	
	public Bigraph getRedex() {
		return redex;
	}
	
	public void setRedex(Bigraph redex) {
		Bigraph old = this.redex;
		this.redex = redex;
		
		reactum = null;
		redexToReactum.clear();
		
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
	
	protected IFile file = null;
	
	@Override
	public IFile getFile() {
		return file;
	}

	@Override
	public ReactionRule setFile(IFile file) {
		this.file = file;
		return this;
	}
	
	@SuppressWarnings("unchecked")
	private static final <T, V extends T> V ac(T o) {
		return (V)o;
	}
	
	@SuppressWarnings("unchecked")
	private static final <T extends ModelObject> T mg(Map<ModelObject, ModelObject> m, T k) {
		return (T)m.get(k);
	}
	
	protected static Change createReactumChange(
			Map<ModelObject, ModelObject> sourceToNew, Change redexChange) {
		Change reactumChange = null;
		if (redexChange instanceof ChangeGroup) {
			ChangeGroup cg_ = (ChangeGroup)redexChange,
				cg = new ChangeGroup();
			for (Change i : cg_)
				cg.add(createReactumChange(sourceToNew, i));
			
			reactumChange = cg;
		} else if (redexChange instanceof Container.ChangeAddChild) {
			Container.ChangeAddChild ch = ac(redexChange);
			
			Container reactumParent = mg(sourceToNew, ch.getCreator());
			Layoutable reactumChild = mg(sourceToNew, ch.child);
			
			if (reactumParent == null)
				return null;
			if (reactumChild == null)
				reactumChild = ch.child.clone(sourceToNew);
			
			/*
			 * XXX: a BigraphScratchpad should really be used here so that
			 * ChangeGroups will actually work properly
			 */
			String reactumName;
			Map<String, Layoutable> reactumNamespace =
				reactumParent.getBigraph().
				getNamespace(Bigraph.getNSI(reactumChild));
			if (reactumNamespace.get(ch.name) == null) {
				reactumName = ch.name;
			} else reactumName = Bigraph.getFirstUnusedName(reactumNamespace);
			
			reactumChange =
				reactumParent.changeAddChild(reactumChild, reactumName);
		} else if (redexChange instanceof Layoutable.ChangeLayout) {
			Layoutable.ChangeLayout ch = ac(redexChange);
			
			Layoutable reactumModel = mg(sourceToNew, ch.getCreator());
			
			if (reactumModel == null)
				return null;
			
			reactumChange =
				reactumModel.changeLayout(ch.newLayout.getCopy());
		} else if (redexChange instanceof Container.ChangeRemoveChild) {
			Container.ChangeRemoveChild ch = ac(redexChange);
			
			Container reactumParent = mg(sourceToNew, ch.getCreator());
			Layoutable reactumChild = mg(sourceToNew, ch.child);
			
			if (reactumParent == null || reactumChild == null)
				return null;
			
			reactumChange =
				reactumParent.changeRemoveChild(reactumChild);
			sourceToNew.remove(ch.child);
		} else if (redexChange instanceof Layoutable.ChangeName) {
			Layoutable.ChangeName ch = ac(redexChange);
			
			Layoutable reactumModel = mg(sourceToNew, ch.getCreator());
			if (reactumModel == null)
				return null;
			
			reactumChange = reactumModel.changeName(ch.newName);
		} else if (redexChange instanceof Point.ChangeConnect) {
			Point.ChangeConnect ch = ac(redexChange);
			
			Point reactumPoint = mg(sourceToNew, ch.getCreator());
			Link reactumLink = mg(sourceToNew, ch.link);
			if (reactumPoint == null || reactumLink == null)
				return null;
			
			reactumChange = reactumPoint.changeConnect(reactumLink);
		} else if (redexChange instanceof Point.ChangeDisconnect) {
			Point.ChangeDisconnect ch = ac(redexChange);
			
			Point reactumPoint = mg(sourceToNew, ch.getCreator());
			Link reactumLink = mg(sourceToNew, ch.link);
			if (reactumPoint == null || reactumLink == null)
				return null;
			
			reactumChange = reactumPoint.changeDisconnect(reactumLink);
		} else if (redexChange instanceof Site.ChangeAlias) {
			Site.ChangeAlias ch = ac(redexChange);
			
			Site reactumSite = mg(sourceToNew, ch.getCreator());
			if (reactumSite == null)
				return null;
			
			reactumChange = reactumSite.changeAlias(ch.alias);
		}
		return reactumChange;
	}
	
	@Override
	public ReactionRule clone(Map<ModelObject, ModelObject> m) {
		if (m == null)
			m = new HashMap<ModelObject, ModelObject>();
		ReactionRule rr = (ReactionRule)super.clone(m);
		
		rr.setFile(getFile());
		rr.setRedex(getRedex().clone(m));
		
		try {
			getReactum().tryApplyChange(getChanges().inverse());
		} catch (ChangeRejectedException cre) {
			/* very bad news */
			cre.printStackTrace();
		}
		rr.setReactum(getReactum().clone(m));
		
		ChangeGroup cg = rr.getChanges();
		for (Change c : getChanges()) {
			try {
				Change cP = createReactumChange(m, c);
				rr.getReactum().tryApplyChange(cP);
				cg.add(cP);
				
				getReactum().tryApplyChange(c);
			} catch (ChangeRejectedException cre) {
				/* very bad news */
				cre.printStackTrace();
			}
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
}
