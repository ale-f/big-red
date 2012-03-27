package org.bigraph.uniud.bigraph.match;
import dk.itu.big_red.model.*;
import dk.itu.big_red.model.changes.ChangeGroup;
import dk.itu.big_red.model.changes.ChangeRejectedException;


public class BigraphMatchWB {
	
	private static Bigraph agent;
	private static Bigraph redex;
	
	
	public static void match(){//Bigraph agent, Bigraph redex){
		fillBigraphExampleAgent4();
		fillBigraphExampleRedex4();
//		fillBigraphAgent2();
//		fillBigraphRedex2();
		PlaceMatch.match(agent, redex);
		
	}
	
	public static void main(String[] args){
		match();
	}
	
	public static void fillster(){
		Signature s = new Signature();
		Control c1 = new Control();
		c1.addPort(new PortSpec("0", 0, 0.5));

		Bigraph b = new Bigraph();
		b.setSignature(s);
		ChangeGroup cg = new ChangeGroup();

		Root r1 = new Root();
		Node n1 = new Node(c1);
		OuterName O1 = new OuterName();

		cg.add(b.changeAddChild(O1, "O1"));
		cg.add(r1.changeAddChild(n1, "n1"));
		cg.add(n1.getPort("0").changeConnect(O1));

		try {
		 b.tryApplyChange(cg);
		} catch (ChangeRejectedException cre) {
		 System.out.println(cre.getRationale());
		}
		
	}
	
	public static void fillBigraphAgent(){
		agent= new Bigraph();
		Control c1= new Control();
		PortSpec portSpec1_1=new PortSpec("p1");
		PortSpec portSpec1_2=new PortSpec("p2");
		c1.addPort(portSpec1_1);
		c1.addPort(portSpec1_2);
		c1.setName("C1");
		
		Control c2= new Control();
		PortSpec portSpecc2_1=new PortSpec("p1");
		c2.addPort(portSpecc2_1);
		c2.setName("C2");
		
		Root r1= new Root();
		Node u= new Node(c1);
		u.setName("U");
		
		u.setComment("prova");
		
		Node v= new Node(c2);
		v.setName("V");
		Node v2= new Node(c2);
		v2.setName("V2");
		
		r1.addChild(u);
		u.addChild(v);
		r1.addChild(v2);
		Edge edge= new Edge();
		edge.changeName("e1");
		OuterName o1= new OuterName();
		o1.changeName("o1");
		
		u.getPort("p1").setLink(edge);
		u.getPort("p2").setLink(o1);
		v.getPort("p1").setLink(o1);
		v2.getPort("p1").setLink(edge);
		
		
		agent.addChild(r1);
	}
	
	public static void fillBigraphRedex(){
		redex= new Bigraph();
		Control c1= new Control();
		PortSpec portSpec1_1=new PortSpec("p1");
		PortSpec portSpec1_2=new PortSpec("p2");
		c1.addPort(portSpec1_1);
		c1.addPort(portSpec1_2);
		c1.setName("C1");
		
		Control c2= new Control();
		PortSpec portSpecc2_1=new PortSpec("p1");
		c2.addPort(portSpecc2_1);
		c2.setName("C2");
		
		Root r1= new Root();
		Node u= new Node(c1);
		
		Node v= new Node(c2);
		Node v2= new Node(c2);
		
		r1.addChild(u);
		u.addChild(v);
		//r1.addChild(v2);
		
		
		//u.getPort("p1").setLink(edge);
		//u.getPort("p2").setLink(o1);
		//v.getPort("p1").setLink(o1);
		//v2.getPort("p1").setLink(edge);
		
		
		redex.addChild(r1);	
	}
	
	public static void fillBigraphExampleRedex(){
		redex= new Bigraph();
		Control c1= new Control();
		PortSpec portSpec1_1=new PortSpec("p1");
		PortSpec portSpec1_2=new PortSpec("p2");
		c1.addPort(portSpec1_1);
		c1.addPort(portSpec1_2);
		c1.setName("C1");
		
		Control c2= new Control();
		PortSpec portSpec2_1=new PortSpec("p1");
		PortSpec portSpec2_2=new PortSpec("p2");
		c2.addPort(portSpec2_1);
		c2.addPort(portSpec2_2);
		c2.setName("C2");
		
		Root r1= new Root();
		
		Node v= new Node(c2);
		v.setName("V");
		
		Root r2= new Root();
		
		Node u= new Node(c1);
		u.setName("U");
		
		Node v2= new Node(c2);
		v2.setName("V2");
		
		//r1.addChild(u);
		//r2.addChild(v2);
		//u.addChild(v);
		redex.addChild(r1);
		r1.addChild(v);
		//redex.addChild(r2);
	}
	
	public static void fillBigraphExampleRedex1(){
		redex= new Bigraph();
		Control c1= new Control();
		PortSpec portSpec1_1=new PortSpec("p1");
		PortSpec portSpec1_2=new PortSpec("p2");
		c1.addPort(portSpec1_1);
		c1.addPort(portSpec1_2);
		c1.setName("C1");
		
		Control c2= new Control();
		PortSpec portSpec2_1=new PortSpec("p1");
		PortSpec portSpec2_2=new PortSpec("p2");
		c2.addPort(portSpec2_1);
		c2.addPort(portSpec2_2);
		c2.setName("C2");
		
		Root r1= new Root();
		
		Node v= new Node(c2);
		v.setName("V");
		
		Root r2= new Root();
		
		Node u= new Node(c1);
		u.setName("U");
		
		Node v2= new Node(c2);
		v2.setName("V2");
		Node v3= new Node(c2);
		v3.setName("V2");
		
		r1.addChild(u);
		r2.addChild(v2);

		u.addChild(v);
		redex.addChild(r1);
		//r1.addChild(v);
		redex.addChild(r2);
	}
	
	public static void fillBigraphExampleAgent(){
		agent= new Bigraph();
		Control c1= new Control();
		PortSpec portSpec1_1=new PortSpec("p1");
		PortSpec portSpec1_2=new PortSpec("p2");
		c1.addPort(portSpec1_1);
		c1.addPort(portSpec1_2);
		c1.setName("C1");
		
		Control c2= new Control();
		c2.setName("C2");
		
		Root r1= new Root();
		
		Node u= new Node(c1);
		u.setName("U");
		
		Node u1= new Node(c1);
		u1.setName("U1");
		
		Node v= new Node(c2);
		v.setName("V");
		
		Node v2= new Node(c2);
		v2.setName("U");
		
		u1.addChild(v);
		u.addChild(u1);
		r1.addChild(u);
		r1.addChild(v2);
		agent.addChild(r1);
	}
	
	
	public static void fillBigraphExampleRedex2(){
		redex= new Bigraph();
		Control c1= new Control();
		PortSpec portSpec1_1=new PortSpec("p1");

		c1.addPort(portSpec1_1);

		c1.setName("C1");
		
		Control c2= new Control();
		c2.setName("C2");
		
		Edge edge= new Edge();
		edge.changeName("e1");

		
		Root r1= new Root();
		
		Node u= new Node(c2);
		u.setName("U");
		
		Node v= new Node(c1);
		v.setName("V");
		v.getPort("p1").setLink(edge);
		
		r1.addChild(u);
		
		u.addChild(v);

		redex.addChild(r1);

	}
	
	public static void fillBigraphExampleAgent2(){
		agent= new Bigraph();

		Control c1= new Control();
		PortSpec portSpec1_1=new PortSpec("p1");
		c1.addPort(portSpec1_1);
		
		Edge edge= new Edge();
		edge.changeName("e1");


		OuterName edge2=new OuterName();
		edge2.changeName("e2");

		c1.setName("C1");
		Control c2= new Control();
		c2.setName("C2");
		
		Root r1= new Root();
		
		Node u= new Node(c2);
		u.setName("U");
		
		Node v= new Node(c1);
		v.setName("V");
		v.getPort("p1").setLink(edge);
		
		Node v2= new Node(c1);
		v2.setName("V2");
		v2.getPort("p1").setLink(edge2);
		
		
		r1.addChild(u);
		
		u.addChild(v);
		u.addChild(v2);

		agent.addChild(r1);
	}
	

	public static void fillBigraphExampleRedex3(){
		redex= new Bigraph();
		Control c1= new Control();
		//PortSpec portSpec1_1=new PortSpec("p1");
		//c1.addPort(portSpec1_1);
		
		//Edge edge= new Edge();
		//edge.changeName("e1");


		//OuterName edge2=new OuterName();
		//edge2.changeName("e2");

		c1.setName("C1");
		
		


		
		Root r1= new Root();
		
		Node n1_1= new Node(c1);
		n1_1.setName("U");
		
		Node n2_1= new Node(c1);
		n2_1.setName("V");
		//v.getPort("p1").setLink(edge);
		
		r1.addChild(n1_1);
		
		n1_1.addChild(n2_1);

		redex.addChild(r1);

	}
	
	
	public static void fillBigraphExampleRedex3_1(){
		redex= new Bigraph();
		
		
		Root r1= new Root();

		Site s1 = new Site();
		s1.changeName("$0");
		r1.addChild(s1);
		
		redex.addChild(r1);

	}
	
	public static void fillBigraphExampleAgent3(){
		agent= new Bigraph();

		Control c1= new Control();
		//PortSpec portSpec1_1=new PortSpec("p1");
		//c1.addPort(portSpec1_1);
		
		//Edge edge= new Edge();
		//edge.changeName("e1");


		//OuterName edge2=new OuterName();
		//edge2.changeName("e2");

		c1.setName("C1");
		//Control c2= new Control();
		//c2.setName("C2");
		
		Root r1= new Root();
		
		Node n1= new Node(c1);
		n1.setName("n1");
		
		Node n2= new Node(c1);
		n2.setName("n2");
		//v.getPort("p1").setLink(edge);
		
		Node n3= new Node(c1);
		n3.setName("n3");
		//.getPort("p1").setLink(edge2);
		
		Node n4=  new Node(c1);
		n4.setName("n4");
		
		r1.addChild(n1);
		
		n1.addChild(n2);
		n2.addChild(n3);
		n2.addChild(n4);
		agent.addChild(r1);
	}
	

	
	
	public static void fillBigraphExampleRedex4(){
		redex= new Bigraph();
		Control c1= new Control();

		
		Root r1= new Root();

		Site s1 = new Site();
		s1.changeName("$0");
		r1.addChild(s1);
		
		Root r2= new Root();

		Node n1 = new Node(c1);
		n1.changeName("N1");
		r2.addChild(n1);
		
		redex.addChild(r1);
		redex.addChild(r2);
		

	}
	
	public static void fillBigraphExampleAgent4(){
		agent= new Bigraph();

		Control c1= new Control();
		//PortSpec portSpec1_1=new PortSpec("p1");
		//c1.addPort(portSpec1_1);
		
		//Edge edge= new Edge();
		//edge.changeName("e1");


		//OuterName edge2=new OuterName();
		//edge2.changeName("e2");

		c1.setName("C1");
		//Control c2= new Control();
		//c2.setName("C2");
		
		Root r1= new Root();
		
		Node n1= new Node(c1);
		n1.setName("n1");
		
		Node n2= new Node(c1);
		n2.setName("n2");
		//v.getPort("p1").setLink(edge);
		
		Node n3= new Node(c1);
		n3.setName("n3");
		//.getPort("p1").setLink(edge2);
		
		Node n4=  new Node(c1);
		n4.setName("n4");
		
		r1.addChild(n1);
		
		n1.addChild(n2);
		n2.addChild(n3);
		n1.addChild(n4);
		agent.addChild(r1);
	}
	
	

}
