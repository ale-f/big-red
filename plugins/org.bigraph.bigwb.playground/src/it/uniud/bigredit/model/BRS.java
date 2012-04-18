package it.uniud.bigredit.model;

import it.uniud.bigredit.PlayEditor;
import it.uniud.bigredit.policy.BRSChangeValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.draw2d.geometry.Rectangle;


//import it.uniud.bigredit.PlayEditor;

import dk.itu.big_red.model.Bigraph;
import dk.itu.big_red.model.ModelObject;
import dk.itu.big_red.model.Signature;

import dk.itu.big_red.model.changes.Change;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;
import dk.itu.big_red.model.changes.IChangeExecutor;
import dk.itu.big_red.model.changes.IChangeValidator;


public class BRS extends ModelObject implements IChangeExecutor{
	
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
	
	public class ChangeAddChild extends ModelObjectChange {
		public ModelObject child;
		public String name;
		
		public ChangeAddChild(ModelObject child, String name) {
			System.out.println("new ChangeAddChild");
			this.child = child;
			this.name = name;
		}
		
		@Override
		public ChangeRemoveChild inverse() {
			return new ChangeRemoveChild(child);
		}
		
		@Override
		public boolean isReady() {
			System.out.println("is READy called in changeAddChild Called in BRS");
			return (child != null);// && name != null);
		}
		
		@Override
		public String toString() {
			return "Change(add child " + child + " to parent " + getCreator() + " with name \"" + name + "\")";
		}
	}
	
	public class ChangeRemoveChild extends ModelObjectChange {
		public ModelObject child;
		
		public ChangeRemoveChild(ModelObject child) {
			this.child = child;
		}
		
		private String oldName = null;
		
		@Override
		public void beforeApply() {
			//oldName = child.getName();
		}
		
		@Override
		public boolean canInvert() {
			return (oldName != null);
		}
		
		@Override
		public ChangeAddChild inverse() {
			return new ChangeAddChild(child, oldName);
		}
		
		@Override
		public boolean isReady() {
			return (child != null);
		}
		
		@Override
		public String toString() {
			return "Change(remove child " + child + " from " + getCreator() + ")";
		}
	}
	
	public class ChangeLayoutChild extends ModelObjectChange {
		public ModelObject child;
		public Rectangle layout;
		
		public ChangeLayoutChild(ModelObject child, Rectangle layout) {
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
			return "Change(remove child " + child + " from " + getCreator() + ")";
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
		System.out.println("size of BRS vector:" + meta.size());
		return meta;
	}
	
	public void addChild(ModelObject child) {
		//addChild(child);
		children.put(child, new Rectangle(100,100,100,100));
		firePropertyChange(BRS.PROPERTY_PARENT, null, child);
		
	}
	
	public void _changeLayoutChild(ModelObject child, Rectangle rectangle) {
		//addChild(child);
		System.out.println("changedLayout");
		children.put(child, rectangle);
		firePropertyChange(BRS.PROPERTY_LAYOUT, children.get(child), rectangle);
		
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
	
	
	private void doChange(Change b) {
		
		b.beforeApply();
		if (b instanceof ChangeGroup) {
			for (Change c : (ChangeGroup)b)
				doChange(c);
				System.out.println("here");
		} else if (b instanceof BRS.ChangeAddChild) {
			
			BRS.ChangeAddChild c = (BRS.ChangeAddChild)b;
			((BRS)c.getCreator()).addChild(c.child);
			System.out.println("Add child");
			//c.child.setName(c.name);
			//getNamespace(getNSI(c.child)).put(c.name, c.child);
		} else if(b instanceof BRS.ChangeLayoutChild){
			BRS.ChangeLayoutChild c = (BRS.ChangeLayoutChild)b;
			((BRS)c.getCreator())._changeLayoutChild(c.child, c.layout);
			System.out.println("change layout child");
			
		}
				
				
		 /*else if (b instanceof Point.ChangeConnect) {
			Point.ChangeConnect c = (Point.ChangeConnect)b;
			c.link.addPoint(c.getCreator());
		} else if (b instanceof Point.ChangeDisconnect) {
			Point.ChangeDisconnect c = (Point.ChangeDisconnect)b;
			c.link.removePoint(c.getCreator());
		 else if (b instanceof Container.ChangeRemoveChild) {
			Container.ChangeRemoveChild c = (Container.ChangeRemoveChild)b;
			c.getCreator().removeChild(c.child);
			//getNamespace(getNSI(c.child)).remove(c.child.getName());
		} else if (b instanceof Layoutable.ChangeLayout) {
			Layoutable.ChangeLayout c = (Layoutable.ChangeLayout)b;
			c.getCreator().setLayout(c.newLayout);
			if (c.getCreator().getParent() instanceof Bigraph)
				((Bigraph)c.getCreator().getParent()).updateBoundaries();
		} else if (b instanceof Edge.ChangeReposition) {
			Edge.ChangeReposition c = (Edge.ChangeReposition)b;
			c.getCreator().averagePosition();
		} else if (b instanceof Colourable.ChangeOutlineColour) {
			Colourable.ChangeOutlineColour c = (Colourable.ChangeOutlineColour)b;
			//c.getCreator().setOutlineColour(c.newColour);
		} else if (b instanceof Colourable.ChangeFillColour) {
			Colourable.ChangeFillColour c = (Colourable.ChangeFillColour)b;
			//c.getCreator().setFillColour(c.newColour);
		} else if (b instanceof Layoutable.ChangeName) {
			Layoutable.ChangeName c = (Layoutable.ChangeName)b;
			//getNamespace(getNSI(c.getCreator())).remove(c.getCreator().getName());
			c.getCreator().setName(c.newName);
			//getNamespace(getNSI(c.getCreator())).put(c.newName, c.getCreator());
		} else if (b instanceof ModelObject.ChangeComment) {
			ModelObject.ChangeComment c = (ModelObject.ChangeComment)b;
			c.getCreator().setComment(c.comment);
		} else if (b instanceof Site.ChangeAlias) {
			Site.ChangeAlias c = (Site.ChangeAlias)b;
			//c.getCreator().setAlias(c.alias);
		}*/
		
		
	}
	

	@Override
	public IFile getFile() {
		// TODO Auto-generated method stub
		return null;
	}


	public Change changeLayoutChild(ModelObject node, Rectangle rectangle) {
		// TODO Auto-generated method stub
		return new ChangeLayoutChild(node,rectangle);
	}


	public Change changeAddChild(ModelObject node, String string) {
		System.out.println("changeAddChild Called in BRS");
		return new ChangeAddChild(node, string);
	}

	private IChangeValidator validator = new BRSChangeValidator(this);
	
	@Override
	public void tryValidateChange(Change b) throws ChangeRejectedException {
		System.out.println("tryValidateChange in BrS");
		validator.tryValidateChange(b);
	}


	@Override
	public void tryApplyChange(Change b) throws ChangeRejectedException {
		System.out.println("try apply change in BRS");
		tryValidateChange(b);
		doChange(b);
		
	}
	







	
}