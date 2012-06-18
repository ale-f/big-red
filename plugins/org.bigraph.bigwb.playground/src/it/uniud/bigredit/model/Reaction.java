package it.uniud.bigredit.model;


import java.util.HashMap;

import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.ModelObject;
import org.bigraph.model.OuterName;
import org.bigraph.model.Root;
import org.bigraph.model.Signature;
import org.bigraph.model.Site;
import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.ChangeRejectedException;
import org.bigraph.model.changes.IChangeExecutor;
import org.bigraph.model.changes.IChangeValidator;
import org.eclipse.draw2d.geometry.Rectangle;

import it.uniud.bigredit.policy.ReactionChangeValidator;






public class Reaction  extends ModelObject  implements IChangeExecutor{
	
	public static final int GAP_WIDTH = 96;
	public static final int MIN_WIDTH_BIG= 100;
	public static final int MIN_HIGHT_BIG = 100;
	public static int SEPARATOR_WIDTH = 300;
	
	public static final String PROPERTY_RULE = "Reaction_Rule_Change";
	public static final String PROPERTY_RULE_LAYOUT = "Reaction_Rule_Change_Layout";
	
	private Bigraph redex = null;
	private Bigraph reactum = null;
	private Rectangle redexLayout = new Rectangle(0,0,100,300);
	private Rectangle reactumLayout = new Rectangle(100,0,100,300);
	
	private HashMap <Site,Site> mapRedexSiteToReactum;
	private HashMap <Root, Root> mapRedexRootToReactum;
	
	private HashMap <String, Layoutable> mapRedex;
	
	
	private Signature sign;
	
	
	public Signature getSign() {
		return sign;
	}



	public void setSign(Signature sign) {
		this.sign = sign;
	}

	public Bigraph getRedex()
	{
		return redex;
	}
	
	public Bigraph getReactum()
	{
		return reactum;
	}
	
	
	public class ChangeAddRedex extends ModelObjectChange {
		public Bigraph child;
		public Bigraph oldchild;
		
		public ChangeAddRedex(Bigraph child) {
			System.out.println("new ChangeAddRedex");
			this.child = child;
		}
		
		@Override
		public ChangeAddRedex inverse() {
			return new ChangeAddRedex(oldchild);
		}
		
		@Override
		public boolean isReady() {
			return (child != null);// && name != null);
		}
		
		@Override
		public String toString() {
			return "Change(add redex " + child + " to parent " + getCreator() + "\")";
		}
	}
	
	public class ChangeAddReactum extends ModelObjectChange {
		public Bigraph child;
		public Bigraph oldchild;
		
		public ChangeAddReactum(Bigraph child) {
			System.out.println("new ChangeAddReactum");
			this.child = child;
		}
		
		@Override
		public ChangeAddReactum inverse() {
			return new ChangeAddReactum(oldchild);
		}
		
		@Override
		public boolean isReady() {
			return (child != null);// && name != null);
		}
		
		@Override
		public String toString() {
			return "Change(add reactum" + child + " to parent " + getCreator() + "\")";
		}
	}
	
	
	public class ChangeLayoutChild extends ModelObjectChange {
		public Bigraph child;
		public Rectangle layout;
		
		public ChangeLayoutChild(Bigraph child, Rectangle layout) {
			this.child = child;
			this.layout=layout;
		}
		
		private Rectangle oldLayout = null;
		
		@Override
		public void beforeApply() {
			//oldName = child.getName();
		}
		
		@Override
		public boolean canInvert() {
			return (oldLayout != null);
		}
		
		@Override
		public ChangeLayoutChild inverse() {
			return new ChangeLayoutChild(child, oldLayout);
		}
		
		@Override
		public boolean isReady() {
			return (child != null);
		}
		
		@Override
		public String toString() {
			return "Change(change layout of: " + child + " in " + getCreator() + ")";
		}
	}
	
	
	

	
	private void changeRedex(Bigraph redex ){
		Bigraph oldRedex = this.redex;
		this.redex= redex;
		firePropertyChange(Reaction.PROPERTY_RULE,oldRedex, redex);
	}
	
	private void changeReactum(Bigraph reactum){
		Bigraph oldReactum= this.reactum;
		this.reactum=reactum;
		firePropertyChange(Reaction.PROPERTY_RULE,oldReactum, reactum);
	}
	
	@Override
	public void tryApplyChange(Change b) throws ChangeRejectedException {
		
		tryValidateChange(b);
		doChange(b);
		
	}

	private IChangeValidator validator = new ReactionChangeValidator(this);
	
	@Override
	public void tryValidateChange(Change b) throws ChangeRejectedException {
		
		validator.tryValidateChange(b);
	}
	
	@Override
	protected boolean doChange(Change b) {
		if (super.doChange(b)) {
			/* do nothing */
		} else if (b instanceof Reaction.ChangeAddReactum) {

			Reaction.ChangeAddReactum c = (Reaction.ChangeAddReactum) b;
			((Reaction) c.getCreator()).changeReactum(c.child);
			
		} else if (b instanceof Reaction.ChangeAddRedex) {
			Reaction.ChangeAddRedex c = (Reaction.ChangeAddRedex) b;
			((Reaction) c.getCreator()).changeRedex(c.child);
		}else if(b instanceof Reaction.ChangeLayoutChild){
			Reaction.ChangeLayoutChild c = (Reaction.ChangeLayoutChild)b;
			((Reaction)c.getCreator())._changeLayoutChild(c.child, c.layout);
		} else return false;
		return true;
	}
	
	
	public Change changeAddRedex(Bigraph node) {
		return new ChangeAddRedex(node);
	}
	
	public Change changeAddReactum(Bigraph node) {
		return new ChangeAddReactum(node);
	}
	
	public Change changeLayoutChild(Bigraph node, Rectangle rectangle) {
		return new ChangeLayoutChild(node,rectangle);
	}
	
	public void _changeLayoutChild(ModelObject child, Rectangle rectangle) {
		
		System.out.println("change child layout Reaction -> Redex || Reactum");
		//addChild(child);
		Rectangle oldRect= null;
		if (child.equals(redex)){
			oldRect=redexLayout;
			redexLayout=rectangle;
		}else if (child.equals(reactum)){
			oldRect=reactumLayout;
			reactumLayout=rectangle;
		}else{
			System.out.println("not a child");
		}
		firePropertyChange(Reaction.PROPERTY_RULE_LAYOUT,oldRect, rectangle);//children.get(child), rectangle);
	}

	

	public Rectangle getChildConstraint(Bigraph child){
		if (child.equals(redex)){
			return redexLayout;
		}else if (child.equals(reactum)){
			return reactumLayout;
		}
		return new Rectangle(100,100,100,100);
		
	}
	
	
	public void analyzeReaction(){
		for (Root root : redex.getRoots()){
			String name=root.getName();
			for(Root root2: reactum.getRoots()){
				if(name.equals(root2.getName())){
					mapRedexRootToReactum.put(root, root2);
				}
			}
		}
	}
	
	public HashMap<Root, Root> getMapRedexRootToReactum() {
		mapRedexRootToReactum= new HashMap<Root, Root> ();
		analyzeReaction();
		return mapRedexRootToReactum;
	}
	
	
	/**
	+	 * this store the object Name(string), object(layotable), used to map the object of the redex.
	 * this store the object Name(string), object(layotable), used to map the object of the redex.
	 +	 * 
	 +	 */
	 	
	 	public void exploreBigraph(HashMap <String, Layoutable> map, Bigraph b){
	 		for (Root r: b.getRoots() ){
	 			for (Layoutable l: r.getChildren()){
	 				exploreElement(map, l);
	 			}
	 		}
	 		
	 	}
	 	
	 	/** 
	 	 * explore the container and stores the name in a map 
	 	 * 
	 	 * @param map
	 	 */
	 	
	 	private void exploreElement(HashMap <String, Layoutable> map, Layoutable l){
	 		map.put(l.getName(), l);
	 		if(l instanceof Container){
	 			for (Layoutable son: ((Container) l).getChildren()){
	 				exploreElement(map, son );
	 			}
	 		}
	 	}
	 	
	 	public HashMap<String, Layoutable> getRedexMapName() {
	 		exploreBigraph(mapRedex, redex);
	 		return mapRedex;
	 	}
	 	
	 	
	 	private HashMap<Link,Link> mapLinksReactumRedex;
	 	
	 	private void mapLinks(){
	 		mapLinksReactumRedex= new HashMap<Link,Link> ();
	 		for(Link lReactum: reactum.getEdges()){
	 			for(Link lRedex: redex.getEdges()){
	 				if (lReactum.getName().equals(lRedex.getName())){
	 					mapLinksReactumRedex.put(lReactum, lRedex);
	 				}
	 			}
	 			
	 		}
	 		
	 		for (OuterName nameReactum: reactum.getOuterNames()){
	 			for (OuterName nameRedex: redex.getOuterNames() ){
	 				if (nameReactum.getName().equals(nameRedex.getName())){
	 					mapLinksReactumRedex.put(nameReactum, nameRedex);
	 				}
	 			}
	 			
	 		}
	 		
	 	}
	 
	 
	 
	 	public HashMap<Link, Link> getMapLinksReactumRedex() {
	 		return mapLinksReactumRedex;
	 	}
	 
	 
	 
	 	public void setMapLinksReactumRedex(HashMap<Link, Link> mapLinksReactumRedex) {
	 		this.mapLinksReactumRedex = mapLinksReactumRedex;
	 	}
	
}