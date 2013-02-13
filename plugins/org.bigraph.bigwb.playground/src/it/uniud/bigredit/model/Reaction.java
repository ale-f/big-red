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
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.IChange;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.bigraph.model.changes.descriptors.ChangeDescriptorGroup;
import org.bigraph.model.changes.descriptors.DescriptorExecutorManager;
import org.eclipse.draw2d.geometry.Rectangle;

import dk.itu.big_red.model.ExtendedDataUtilities;

public class Reaction  extends ModelObject{
	
	public static final int GAP_WIDTH = 96;
	public static final int MIN_WIDTH_BIG= 100;
	public static final int MIN_HIGHT_BIG = 40;
	public static int SEPARATOR_WIDTH = 300;
	
	public static final String PROPERTY_RULE = "Reaction_Rule_Change";
	public static final String PROPERTY_RULE_LAYOUT = "Reaction_Rule_Change_Layout";
	
	private Bigraph redex = null;
	private Bigraph reactum = null;
	private Rectangle redexLayout = new Rectangle(15,40,150,200);
	private Rectangle reactumLayout = new Rectangle(315,40,150,200);
	
	public HashMap <Site,Site> mapReactumSiteToRedex= new HashMap <Site,Site>();
	public HashMap <Site, Integer> mapRedexSiteSon= new HashMap<Site,Integer>();
	
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
	
	abstract class ReactionChange implements IChange {
		public Reaction getCreator() {
			return Reaction.this;
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver resolver) {
			/* do nothing */
		}
	}
	
	public class ChangeAddRedex extends ReactionChange {
		public Bigraph child;
		public Bigraph oldchild;
		
		public ChangeAddRedex(Bigraph child) {
			this.child = child;
		}
		
		@Override
		public ChangeAddRedex inverse() {
			return new ChangeAddRedex(oldchild);
		}
		
		@Override
		public String toString() {
			return "Change(add redex " + child + " to parent " + getCreator() + "\")";
		}
	}
	
	public class ChangeInsideModel extends ReactionChange{
		
		public ModelObject target;
		public IChange change;
		
		
		@Override
		public IChange inverse() {
			// TODO Auto-generated method stub
			return null;
		}
		
		public ChangeInsideModel(ModelObject target, IChange change){
			this.target=target;
			this.change=change;
		}
		
	}
	
	
	public class ChangeAddReactum extends ReactionChange {
		public Bigraph child;
		public Bigraph oldchild;
		
		public ChangeAddReactum(Bigraph child) {
			this.child = child;
		}
		
		@Override
		public ChangeAddReactum inverse() {
			return new ChangeAddReactum(oldchild);
		}
		
		@Override
		public String toString() {
			return "Change(add reactum" + child + " to parent " + getCreator() + "\")";
		}
	}
	
	public Reaction(){
		super();
		redex= new Bigraph();
		reactum= new Bigraph();
		
		_changeLayoutChild(redex, redexLayout);
		_changeLayoutChild(reactum, reactumLayout);
		
	}
	
	public ChangeDescriptorGroup cgAux = new ChangeDescriptorGroup();
	
	
	public class ChangeLayoutChild extends ReactionChange {
		public Bigraph child;
		public Rectangle layout;
		
		public ChangeLayoutChild(Bigraph child, Rectangle layout) {
			this.child = child;
			this.layout=layout;
		}
		
		private Rectangle oldLayout = null;
		
		@Override
		public ChangeLayoutChild inverse() {
			return new ChangeLayoutChild(child, oldLayout);
		}
		
		@Override
		public String toString() {
			return "Change(change layout of: " + child + " in " + getCreator() + ")";
		}
	}
	
	
	

	
	void changeRedex(Bigraph redex ){
		Bigraph oldRedex = this.redex;
		this.redex= redex;
		firePropertyChange(Reaction.PROPERTY_RULE,oldRedex, redex);
	}
	
	void changeReactum(Bigraph reactum){
		Bigraph oldReactum= this.reactum;
		this.reactum=reactum;
		firePropertyChange(Reaction.PROPERTY_RULE,oldReactum, reactum);
	}
	
	static {
		DescriptorExecutorManager.getInstance().addParticipant(new ReactionHandler());
	}
	
	public void _changeInsideModel(ModelObject target, IChange change){
		cgAux.clear();
		
		cgAux.add(change);
		try {
			DescriptorExecutorManager.getInstance().tryApplyChange(null, cgAux);
		} catch (ChangeCreationException e) {
			e.printStackTrace();
		}
	}
	
	public IChange changeAddRedex(Bigraph node) {
		return new ChangeAddRedex(node);
	}
	
	public IChange changeInsideModel(ModelObject target, IChange change){
		return new ChangeInsideModel(target,change);
	}
	
	public IChange changeAddReactum(Bigraph node) {
		return new ChangeAddReactum(node);
	}
	
	public IChange changeLayoutChild(Bigraph node, Rectangle rectangle) {
		return new ChangeLayoutChild(node,rectangle);
	}
	
	public void _changeLayoutChild(ModelObject child, Rectangle rectangle) {
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
		
		mapRedexRootToReactum=new HashMap<Root, Root> ();
		for (Root root : redex.getRoots()){
			String name=root.getName();
			for(Root root2: reactum.getRoots()){
				if(name.equals(root2.getName())){
					mapRedexRootToReactum.put(root, root2);
				}
			}
		}
		mapRedexSiteSon= new HashMap<Site,Integer>();
		
		for (Site siteReactum: reactum.getSites()){
			String alias=ExtendedDataUtilities.getAlias(siteReactum);
			for (Site siteRedex: redex.getSites()){
				if(siteRedex.getName().equals(alias)){
					mapReactumSiteToRedex.put(siteReactum, siteRedex);
					if(mapRedexSiteSon.get(siteRedex)==null){
						mapRedexSiteSon.put(siteRedex, 1);
					}else{
						mapRedexSiteSon.put(siteRedex,mapRedexSiteSon.get(siteRedex)+1);
					}
					
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
	 	
	 	
	 	public void initializeMap(){
	 		mapRedex= new HashMap <String, Layoutable> ();
	 		exploreBigraph(mapRedex, redex);
	 		mapLinks();
	 	}
	 	
	 	public HashMap<String, Layoutable> getRedexMapName() {
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