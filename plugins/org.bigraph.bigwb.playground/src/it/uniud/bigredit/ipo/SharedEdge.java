package it.uniud.bigredit.ipo;

import org.bigraph.model.Edge;

public class SharedEdge {

	private String name;
	private boolean match=false;
	private Edge e0=null;
	private Edge e1=null;
	
	public SharedEdge(String name){
		this.name=name;
	}
	
	public void setEdgeE0(Edge e0){
		this.e0=e0;
		if(e1!=null){
			match=true;
		}
	}
	
	public void setEdgeE1(Edge e1){
		this.e1=e1;
		if(e0 != null){
			match=true;
		}
	}
	
	public boolean isShared(){
		return match;
	}
	
	public boolean isNullEdge0(){
		return e0==null;
	}
	
	public Edge getE0() {
		return e0;
	}

	public Edge getE1() {
		return e1;
	}

	public boolean isNullEdge1(){
		return e1==null;
	}

	
	
}
