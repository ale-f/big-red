package it.uniud.bigredit.model;

import it.uniud.bigredit.PlayEditor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bigraph.model.Bigraph;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Signature;
import org.bigraph.model.assistants.ExecutorManager;
import org.bigraph.model.assistants.IObjectIdentifier.Resolver;
import org.bigraph.model.assistants.PropertyScratchpad;
import org.bigraph.model.changes.Change;
import org.bigraph.model.changes.ChangeGroup;
import org.bigraph.model.changes.descriptors.ChangeCreationException;
import org.eclipse.draw2d.geometry.Rectangle;


//import it.uniud.bigredit.PlayEditor;




public class BRS extends ModelObject {
	
	private static ArrayList< Bigraph > diagrams = new ArrayList< Bigraph >();
	private HashMap<ModelObject,Rectangle> children = new HashMap<ModelObject,Rectangle>();
	
	
	

	public static final String PROPERTY_NAME = "BRS_Name";
	
	/**
	 * The property name fired when the layout changes.
	 */

	public static final String PROPERTY_LAYOUT = "BRS_Layout";
	
	/**
	 * The property name fired when the parent changes.
	 */
	public static final String PROPERTY_PARENT = "BRS_Parent";
	
	private Signature signature = new Signature();
	private String name;
	
	public String getName(){
		return name;
	}
	
	abstract class BRSChange extends ModelObjectChange {
		@Override
		public BRS getCreator() {
			return BRS.this;
		}
		
		@Override
		public void simulate(PropertyScratchpad context, Resolver resolver) {
			/* do nothing */
		}
	}
	
	public class ChangeAddChild extends BRSChange {
		public ModelObject child;
		public String name;
		
		public ChangeAddChild(ModelObject child, String name) {
			this.child = child;
			this.name = name;
		}
		
		@Override
		public ChangeRemoveChild inverse() {
			return new ChangeRemoveChild(child);
		}
		
		@Override
		public String toString() {
			return "Change(add child " + child + " to parent " + getCreator() + " with name \"" + name + "\")";
		}
	}
	
	public class ChangeRemoveChild extends BRSChange {
		public ModelObject child;
		
		public ChangeRemoveChild(ModelObject child) {
			this.child = child;
		}
		
		private String oldName = null;
		
		@Override
		public ChangeAddChild inverse() {
			return new ChangeAddChild(child, oldName);
		}
		
		@Override
		public String toString() {
			return "Change(remove child " + child + " from " + getCreator() + ")";
		}
	}
	
	public class ChangeLayoutChild extends BRSChange {
		public ModelObject child;
		public Rectangle layout;
		
		public ChangeLayoutChild(ModelObject child, Rectangle layout) {
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
	
	public class ChangeInsideModel extends BRSChange{
		
		public ModelObject target;
		public Change change;
		
		
		@Override
		public Change inverse() {
			// TODO Auto-generated method stub
			return null;
		}
		
		public ChangeInsideModel(ModelObject target, Change change){
			this.target=target;
			this.change=change;
		}
		
	}
	
	
	
	
	private PlayEditor editor = null;
	
	public BRS( PlayEditor editor )
	{
		this.editor = editor;
		//this.setLayout( new Rectangle( 0, 0, 1000000, 100000 ) );
//		//diagrams.add( this );
//		
	}

	
	
	
//	public void setEditor (PlayEditor editor){
//		this.editor = editor;
//	}
	
	/*@Override
	public BRS getBRS() {
		return this;
	}*/
	
	public BRS()
	{
		//setLayout( new Rectangle( 0, 0,  1000000, 100000 ) );
		//diagrams.add( this );
		//Bigraph big= new Bigraph();
		//big.setLayout(new Rectangle (100,100,50,50));
		//this.addBigraph(big);
	}
	
//	public PlayEditor getEditor()
//	{
//		return editor;
//	}
	
	@Override
	public void finalize()
	{
		diagrams.remove( this );
	}
	
	public static ArrayList< Bigraph > getBigraphs()
	{
		return diagrams;
	}
	

	public List<ModelObject> getChildren(){
		
		List <ModelObject> meta=  new ArrayList<ModelObject>();
		meta.addAll(children.keySet());
		return meta;
	}
	
	public void addChild(ModelObject child) {
		//addChild(child);
		children.put(child, new Rectangle(100,100,200,300));
		firePropertyChange(BRS.PROPERTY_PARENT,null , child);
		
	}
	
	public void _changeLayoutChild(ModelObject child, Rectangle rectangle) {
		//addChild(child);
		Rectangle oldRect = children.get(child);
		children.put(child, rectangle);
		firePropertyChange(BRS.PROPERTY_LAYOUT,oldRect, rectangle);//children.get(child), rectangle);
		
	}
	
	public void _changeRemoveChild(ModelObject child){
		children.remove(child);
		firePropertyChange(BRS.PROPERTY_PARENT,child , null);
		
	}
	
	
	ChangeGroup cgAux= new ChangeGroup();
	public void _changeInsideModel(ModelObject target, Change change){
		
		
		cgAux.clear();
		
		cgAux.add(change);
		try {
			ExecutorManager.getInstance().tryApplyChange(cgAux);
		} catch (ChangeCreationException e) {
			e.printStackTrace();
		}
	}

	public void addBigraph( Bigraph bigraph )
	{
		diagrams.add(bigraph);
		//bigraph.setBRS(this);
	}
	
	public Signature getSignature() {
		return signature;
	}

	public void setSignature(Signature signature) {
		this.signature = signature;
	}
	
	
	public Change changeInsideModel(ModelObject target, Change change){
		return new ChangeInsideModel(target,change);
	}


	public Change changeLayoutChild(ModelObject node, Rectangle rectangle) {
		// TODO Auto-generated method stub
		return new ChangeLayoutChild(node,rectangle);
	}


	public Change changeAddChild(ModelObject node, String string) {
		
		return new ChangeAddChild(node, string);
	}

	public Change changeRemoveChild(ModelObject node) {
		
		return new ChangeRemoveChild(node);
	}
	
	static {
		ExecutorManager.getInstance().addParticipant(new BRSHandler());
	}
	
	public Rectangle getChildrenConstraint(ModelObject child){
		
		return children.get(child);
	}
	

    public List<Reaction> getRules(){
    	List<Reaction> list=new ArrayList<Reaction>();
    	for(ModelObject mo: getChildren()){
    		if(mo instanceof Reaction){
    			list.add((Reaction)mo);
 
    		}
    	}
    	return list;
    }
    
    public List<Bigraph> getModels(){
    	List<Bigraph> list=new ArrayList<Bigraph>();
    	for(ModelObject mo: getChildren()){
    		if(mo instanceof Bigraph){
    			list.add((Bigraph)mo);
    		}
    	}
    	return list;
    }
    



	
}
