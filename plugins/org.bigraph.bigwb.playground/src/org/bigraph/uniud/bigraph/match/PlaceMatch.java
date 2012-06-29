package org.bigraph.uniud.bigraph.match;
/**
 * Algorithm
 * 
 * matrix:
 *  fill with 0 or 1, 1 if match
 * 
 *                               row control in redex
 *     column control in agent   0 0 0 0               sum of row<=1
 *                                          
 * 								 sum of column = 1
 * 
 * 
 * second part matching links
 * 
 * matrix:
 * 							row edge/outer
 * column control selected  first part linkAgent, second part linkRedex, Column matching complex (e^2)*c   
 * 
 * 
 * 
 * @author Carlo Maiero
 */


import org.bigraph.model.Bigraph;
import org.bigraph.model.Container;
import org.bigraph.model.Edge;
import org.bigraph.model.Layoutable;
import org.bigraph.model.Link;
import org.bigraph.model.Node;
import org.bigraph.model.OuterName;
import org.bigraph.model.Port;
import org.bigraph.model.Root;
import org.bigraph.model.Site;
import org.bigraph.model.interfaces.IRoot;
import org.eclipse.core.runtime.IAdaptable;

import it.uniud.bigredit.model.MatchData;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;


import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.cp.solver.search.integer.varselector.StaticVarOrder;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerConstantVariable;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.set.SetConstantVariable;
import choco.kernel.model.variables.tree.TreeParametersObject;
import choco.kernel.solver.Solver;

public class PlaceMatch {

	
	public static BidiMap<Integer, Layoutable> listAgent=new BidiMap<Integer, Layoutable>();
	public static BidiMap<Integer, Layoutable> listRedex=new BidiMap<Integer, Layoutable>();
	public static HashMap<Integer,Integer[]> succListAgent=new HashMap<Integer,Integer[]>(); 
	public static HashMap<Integer,Integer[]> succListRedex=new HashMap<Integer,Integer[]>(); 
	public static List<Integer> listRootRedex = new ArrayList<Integer>();
	public static int numRootRedex;
	private static BidiMap<Link,Link> maplinkAgent_Redex;
	
	public static ArrayList<MatchData> match(Bigraph agent, Bigraph redex) {
		
		listAgent=new BidiMap<Integer, Layoutable>();
		listRedex=new BidiMap<Integer, Layoutable>();
		succListAgent=new HashMap<Integer,Integer[]>(); 
		succListRedex=new HashMap<Integer,Integer[]>(); 
		listRootRedex = new ArrayList<Integer>();
		numRootRedex=0;
		maplinkAgent_Redex=new BidiMap<Link,Link>();
		
        /** Place graph Matching */		
		
		//successors List int list of successors	
		addBigraphAgent(agent, succListAgent, listAgent);
		printHash(succListAgent);
		//printMapGraph(listAgent);
		
		addBigraphAgent(redex, succListRedex, listRedex);
		printHash(succListRedex);
		
		fillRootRedex();
		//printMapGraph(listRedex);
		
		//printHash(succListRedex);
		//Control Site are mapped into an Integer value
		HashMap<Integer, Integer> Control_Site_mapAgent= new HashMap<Integer,Integer>();
		HashMap<Integer, Integer> Control_Site_mapRedex= new HashMap<Integer,Integer>();
		int matrixValue=1;
		
		//1-create the Model
		Model m= new CPModel();
		int numberCSinA=succListAgent.size();   //6; //number of Control/Site in Agent
		int numberCSinR=succListRedex.size();   //3; //number of Control/Site in Redex
		numRootRedex=listRootRedex.size();
		//2-create the variables
		IntegerVariable[][] matrix = new IntegerVariable[numberCSinA][numberCSinR];
		
		for(int i=0; i<numberCSinA;i++){
			for(int j=0;j<numberCSinR;j++){
				matrix[i][j]=Choco.makeIntVar("var_"+i+"_"+j,0,1, "");
				m.addVariable(matrix[i][j]);
			}
		}
		//3-constraints
		
		//row constraints: (row without roots) <=1
		
		IntegerVariable[][] varRow = new IntegerVariable[numberCSinA][numberCSinR-listRootRedex.size()];
		IntegerVariable[][] varRoot = new IntegerVariable[numberCSinA][listRootRedex.size()];
		for(int i=0; i<numberCSinA;i++){
			int k = 0;
			int r= 0;
			
			for (int j = 0; j < numberCSinR; j++) {
				//if j not Root
				if(!(listRootRedex.contains(j))){
					varRow[i][k]=matrix[i][j];
					k++;
				}else{
					varRoot[i][r]=matrix[i][j];
					r++;
				}
			}
			for (int t =0; t < varRoot[i].length;t++){
				m.addConstraint(Choco.leq(Choco.sum(varRow[i]),Choco.minus(1, varRoot[i][t])));
			}
			System.out.println(Choco.sum(varRow[i]) +" <= 1");
		}
		
		//for column number we keep a separate matrix
		IntegerVariable[][] varCol = new IntegerVariable[numberCSinR][numberCSinA];
		
		
		for (int i = 0; i < numberCSinR; i++) {
			// site can take lot of 
			if (!(listRedex.get(i) instanceof Site)) {
				for (int j = 0; j < numberCSinA; j++) {
					// Copy of var in the column order
					varCol[i][j] = matrix[j][i];
				}

				m.addConstraint(Choco.eq(Choco.sum(varCol[i]), 1));
				System.out.println(Choco.sum(varCol[i]) + " = 1");
			}
		}
		
		
		
		/**
		 * constraint successors: 
		 * for each( nodeA, nodeB){ //1 in matrix 
		 * 		foreach(i : succ(nodeA)){ 
		 * 			1= sum(matrix(i,j)) where j (appartiene ai) succ(nodeB)
		 * 		} 
		 * }
		 */

		
		
		for (int i = 0; i < numberCSinA; i++) {
			
			
			for (int j = 0; j < numberCSinR; j++) {
				// rule has parent !(listRedex.get(j) instanceof Site))
				if(!(listRedex.get(j) instanceof Root)) {
					
					/** Selected node in Redex is not an instance of Root */
					
					/** so if one of the selected node don't have parent is a root or something else
					 * and cant be selected in the matrix so the value it's put to 0
					 */
					if(getParentRedex(j)==-1 || getParentAgent(i)==-1){
						matrix[i][j].setUppB(0);
						System.out.println(matrix[i][j] +" = 0   UUUSEED");
						
					}else{
						/** if is not a root, the parent of the selected node (i,j) must be selected
						 * so we put parent(i,j)>=(i,j). because if the node is selected and is not 
						 * a root the parent must be 1. Is not true otherwise.
						 */
						
						m.addConstraint(Choco.geq(matrix[getParentAgent(i)][getParentRedex(j)],matrix[i][j]));
						System.out.println(matrix[getParentAgent(i)][getParentRedex(j)] +" >= "+matrix[i][j]);
						
						/** new contraint to verify 
						 * 
						 * for each node if != from site  father <= son
						 * 
						 * */
						
						if (!(listRedex.get(j) instanceof Site)){
							for(int son=0; son<succListAgent.get(i).length;son++){
								m.addConstraint(Choco.leq(matrix[i][j], Choco.sum(matrix[succListAgent.get(i)[son]])));
							}
						}
						

					}
					
					
					/**if not instance of Site and don't have successors in Redex
					and selected node in Agent have sons, it can't match. so upperbound of matrix(0)
				*/ 
				if( (!(listRedex.get(j) instanceof Site) && (succListRedex.get(j).length == 0)) || (succListRedex.get(j)==null) ){
					
					System.out.println(  i + " , "+ j + " deve essere 0: matrice " );
					if(succListAgent.get(i).length > 0){
						System.out.println(matrix[i][j] +" = 0");
						matrix[i][j].setUppB(0);
					}
				}
				

					
					
				}else{
					/** Selected node in Redex is instance of Root  
					 * 
					 * so for each anchestor of selected node (i,j)
					 * 
					 * the sumRow(anchestor(i)) + (i,j) <=1
					 */
					
					int anchestor=getParentAgent(i);
					while (anchestor != -1) {
						if (!(listAgent.get(anchestor) instanceof Root)) {

							m.addConstraint(Choco.leq(Choco.sum(
									Choco.sum(matrix[anchestor]),
									Choco.mult(numRootRedex, matrix[i][j])),
									numRootRedex));
						}
						anchestor = getParentAgent(anchestor);

					}

				}
	

				
				
			}		
		}
		

		
		
		
		//4-solve the model
		CPSolver s = new CPSolver();
		
		s.read(m);
		
		// Solve the model
		
		//ChocoLogging.toVerbose();
		
		s.solve();
		//s.solveAll();
		System.out.println("solution count: "+s.getSolutionCount());
		//s.solveAll();
		
		ArrayList<MatchData> matchList= new ArrayList<MatchData>();
		
		int[][] input= new int[numberCSinA][numberCSinR];
		/* READ SOLUTIONS*/
		do  {
			//if (s.getSolutionCount()) {
			//if (s.isCompletelyInstantiated()){
			if(s.checkSolution()){
				
			//if(s.isConsistent()){
				for (int i = 0; i < numberCSinA; i++) {

					for (int j = 0; j < numberCSinR; j++) {
						System.out.print(MessageFormat.format("{0} ",
								s.getVar(matrix[i][j]).getVal()));
						input[i][j]=s.getVar(matrix[i][j]).getVal();
					}
					System.out.println("");
				}
				System.out.println("-------------------- solution number: "
						+ s.getSolutionCount());
				
				if(linkMatch(input)){
					System.out.println("link matches!");
					matchList.add(getSolution(input));
				}else{
					System.out.println("link DON'T match!");
				}
			//	s.checkSolution();
			}
		//}
			
		}while(s.nextSolution());
		
		
		return matchList;
				
	/** TODO check signature of agent and redex */
				
				
	/** Second part:
	 * 
	 * link graph matrix for node matched
	 * 
	 *  AL(agent linkgraph) and RL(redex linkgraph)
	 * 							row edges
	 *  column (node, ports)	(O,1) 	
	 * 
	 * 
	 *  first step
	 *  solve AL * P = RL where P are permutation matrix on the column
	 * 
	 *  second step
	 *  
	 *  create two more matrix
	 *  
	 *  ALO(agent linkgraph)
	 * 							row edges/outer
	 *  column (node, ports)	(O,1) 	
	 *  
	 *  RO(agent linkgraph)
	 * 							row outer
	 *  column (node, ports)	(O,1) 	 
	 * 
	 *  solve ALO * P = RO where P are permutation matrix on the column
	 * 
	 */
				
				
	
				
		
		

	}
	


	private static int explorei=0;
	private static int exploremax=0;
	
	private static void addBigraphAgent(Bigraph bigraph,
			HashMap<Integer, Integer[]> succList,
			BidiMap<Integer, Layoutable> mapGraph) {
		explorei = 0;
		exploremax = 0;
		if (bigraph == null) {
			System.out.println("Bigraph == null");
		}
		for (Layoutable r : bigraph.getChildren()) {   // ).getRoots()) {

			if (r instanceof Container) {
				explore((Container) r, succList, mapGraph);
			} else {
				if (!(r instanceof Edge || r instanceof OuterName)) {
					succList.put(explorei, new Integer[] {});
					mapGraph.put(explorei, (Layoutable) r);
					explorei++;
					exploremax++;
				} else {
					System.out.println("edge || outer");
				}

			}
		}
	}
	

	
	
	private static int getParentRedex(int index){
		Layoutable iLay=listRedex.get(index);
		if(!(iLay.getParent() instanceof Bigraph)){
			return listRedex.getKey(iLay.getParent());
		}
		return -1;
	}
	
	private static int getParentAgent(int index){
		Layoutable iLay=listAgent.get(index);
		if(!(iLay.getParent() instanceof Bigraph)){
			return listAgent.getKey(iLay.getParent());
		}
		return -1;
	}
	
	
	
	private static void addBigraphRedex(Bigraph bigraph,
			HashMap<Integer, Integer[]> succList,
			BidiMap<Integer, Layoutable> mapGraph) {
		explorei = 0;
		exploremax = 0;
		for (Layoutable r : bigraph.getChildren()){//getRoots()) {

			for (Layoutable l : ((Container) r).getChildren()) {

				if (l instanceof Container) {
					explore((Container) l, succList, mapGraph);
				} else {
					succList.put(explorei, new Integer[] {});
					mapGraph.put(explorei, l);
					explorei++;
					exploremax++;
				}
			}
		}
	}
	
	
	private static void explore(Container container,
			HashMap<Integer, Integer[]> list,
			BidiMap<Integer, Layoutable> mapGraph) {
		int max=0;
		//if(container instanceof Bigraph){
		//    max=((Bigraph)container).getRoots().size();
		    
		//}else{
			max=container.getChildren().size();
		//}
		System.out.println("max: " + max);
		Integer[] array = new Integer[max];
		list.put(explorei, array);
		mapGraph.put(explorei, container);
		explorei++;
		exploremax++;
		int j = 0;
		for (Layoutable l : container.getChildren()) {
			if(l instanceof Edge || l instanceof OuterName){
				System.out.println("super fist");
			}
			
			array[j] = exploremax;
			j++;
			if (l instanceof Site){
				System.out.println("site expl-i: " + explorei +" exp-max: "+ exploremax+ " j: "+ j);
				exploremax++;
			}
			if (l instanceof Container) {
				explore((Container) l, list, mapGraph);
			} else {
				list.put(explorei, new Integer[] {});
				mapGraph.put(explorei, l);
				explorei++;
			}
		}
	}
	
	private static void printHash(HashMap<Integer,Integer[]> list){
		int size= list.size();
		for(int i=0; i<size; i++){
			System.out.print(i + "  ->  " );
			for (int j=0;j<list.get(i).length;j++){
				System.out.print(list.get(i)[j]+ " " );
			}
			System.out.println("");
		}
	}
	
	private static void printMapGraph(HashMap<Integer,Layoutable> list){
		
		int size= list.size();
		for(int i=0; i<size; i++){
			if(list.get(i) instanceof Node){
				System.out.println(i +" "+((Node)list.get(i)).getName());
			}
		}
	}

	
	
	
	
	private static int getMatchingAgent(int[][] input, int column){

		for(int i = 0; i< input.length;i++){
			if(input[i][column] == 1){
				return i;
			}
		}
		return -1;
	}
	
	

	
	private static void fillRootRedex(){
		for(int i=0;i< succListRedex.size();i++){
			
			if(listRedex.get(i) instanceof Root){
				listRootRedex.add(i);
			}
		}
	}
	
	private static MatchData getSolution(int[][] input){
		MatchData data= new MatchData();
		for (int redexColumn = 0; redexColumn < succListRedex.size(); redexColumn++) {
			int agentRow = getMatchingAgent(input, redexColumn);
			Layoutable agentMatch = listAgent.get(agentRow);
			Layoutable redexMatch = listRedex.get(redexColumn);
			data.addRootMatch(redexMatch, agentMatch);
		}
		data.setLinkMap(maplinkAgent_Redex);
		return data;
		
		
		
	}
	
	
	
	private static boolean linkMatch(int[][] input) {

		maplinkAgent_Redex = new BidiMap<Link, Link>();

		for (int redexColumn = 0; redexColumn < succListRedex.size(); redexColumn++) {
			int agentRow = getMatchingAgent(input, redexColumn);
			Layoutable agentMatch = listAgent.get(agentRow);
			Layoutable redexMatch = listRedex.get(redexColumn);
			System.out.println("match A: " + agentRow + " "+agentMatch.getType()+" " +agentMatch.getName() + ", R:" + redexColumn +" "+ redexMatch.getType()+" "+  redexMatch.getName()  );


			if (agentMatch instanceof Node && redexMatch instanceof Node) {
				if (!(((Node) agentMatch).getControl().getName()
						.equals(((Node) redexMatch).getControl().getName()))) {

					System.out.println("Type of Control is different"
							+ ((Node) agentMatch).getControl() + " "
							+ ((Node) redexMatch).getControl());
					return false;
				}

				for (Port portRedex : ((Node) redexMatch).getPorts()) {
					Port portAgent = ((Node) agentMatch).getPort(portRedex
							.getName());

					//System.out.println("for each port");
					
					if (portRedex.getLink() instanceof Edge) {
						if (!(portAgent.getLink() instanceof Edge)) {
							/** edge redex -> outer agent return false */
							return false;
						}else{
							/* check that every edge has the same number of connections */
							if (portRedex.getLink().getPoints().size() != portAgent.getLink().getPoints().size()){
								return false;
							}
							
						}
					}

					if (maplinkAgent_Redex.containsKey(portRedex.getLink())) {
						System.out.println("contains key");
						if (!(maplinkAgent_Redex.get(portRedex.getLink())
								.equals(portAgent.getLink()))) {
							return false;
						}
					} else {
						maplinkAgent_Redex.put(portRedex.getLink(),
								portAgent.getLink());
					}

				}

			}
		}

		return true;
	}
	

}
