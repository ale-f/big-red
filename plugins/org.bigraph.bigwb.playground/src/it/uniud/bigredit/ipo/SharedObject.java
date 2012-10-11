package it.uniud.bigredit.ipo;

import org.bigraph.model.Control;
import org.bigraph.model.Layoutable;
import org.bigraph.model.ModelObject;
import org.bigraph.model.Node;
import org.bigraph.model.Root;
import org.bigraph.model.Site;

public class SharedObject {
	
	
	private String name;
	private boolean match=false;
	private Layoutable objectb0=null;
	private Layoutable parentobj0=null;
	private Layoutable objectb1=null;
	private Layoutable parentobj1=null;
	
	
	public SharedObject(String name){
		this.name=name;
		this.match=false;	
	}
	
	
	public boolean isParentRootB0(){
		if (parentobj0 instanceof Root){
			return true;
		}
		return false;
		
	}
	
	public boolean isParentRootB1(){
		if (parentobj1 instanceof Root){
			return true;
		}
		return false;
		
	}
	
	
	public boolean isNullElement0(){
		return objectb0==null;
	}
	
	public boolean isNullElement1(){
		return objectb1==null;
	}
	
	
	public Layoutable getObjectb0() {
		return objectb0;
	}


	public Layoutable getObjectb1() {
		return objectb1;
	}


	public Layoutable getParentB0(){
		return parentobj0;
	}
	
	public Layoutable getParentB1(){
		return parentobj1;
	}
	
	
	public boolean isShared(){
		return match && objectb0 != null && objectb1 !=null;
	}
	
	public void setElementB1(Layoutable obj1){
		this.objectb1=obj1;
		this.parentobj1=obj1.getParent();
		if(objectb0!=null){
			if(objectb0 instanceof Node && objectb1 instanceof Node){
				Control control0=((Node)objectb0).getControl();
				Control control1=((Node)objectb1).getControl();
				if(control0.equals(control1)){
					match=true;
				}
			}else{
				if(objectb0 instanceof Site && objectb1 instanceof Site){
					match=true;
				}
			}
		}
	}
	
	public void setElementB0(Layoutable obj0){
		this.objectb0=obj0;
		this.parentobj0=obj0.getParent();
		if(objectb1!=null){
			if(objectb0 instanceof Node && objectb1 instanceof Node){
				Control control0=((Node)objectb0).getControl();
				Control control1=((Node)objectb1).getControl();
				if(control0.equals(control1)){
					match=true;
				}
			}else{
				if(objectb0 instanceof Site && objectb1 instanceof Site){
					match=true;
				}
			}
		}
	}
	

}
