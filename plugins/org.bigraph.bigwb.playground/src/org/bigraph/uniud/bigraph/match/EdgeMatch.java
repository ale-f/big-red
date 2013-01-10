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
 * 
 * 
 * second part matching links
 * 
 * matrix:
 * 							row edge/outer
 * column control selected  first part linkAgent, second part linkRedex, Column matchin complex (e^2)*c   
 * 
 * 
 * 
 * @author Carlo Maiero
 */




import java.text.MessageFormat;
import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.common.logging.ChocoLogging;
import choco.kernel.model.Model;
import choco.kernel.model.variables.integer.IntegerVariable;

public class EdgeMatch {

	public static void EdgeMatch(String[] args) {
		
		
		//1-create the Model
		Model m= new CPModel();
		int numberPortA=5;//succListAgent.size();   //6; //number of Control/Site in Agent
		int numberEdgeA=4;   //3; //number of Control/Site in Redex
		
		//2-create the variables
		//initialize the matrix with 0, length, with matrixValue Poxibility
		//IntegerVariable[] matrix= Choco.makeIntVarArray("m", numberCSinA*numberCSinR, 0, matrixValue);
		
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
		int[][] linkmapA= new int[numberPortA][numberEdgeA];
		//linkmapA=new int[][]{{1,0},{0,1},{1,0},{1,0}};
		linkmapA=new int[][]{{1,0,0,0},{1,0,0,0},{0,1,0,0},{0,0,1,0},{0,0,0,1}};
		int[][] linkmapB= new int[numberPortA][numberEdgeA];
		//linkmapB=new int[][]{{1,0},{1,0},{0,1},{0,1}};
		linkmapB=new int[][]{{0,0,1,0},{0,0,1,0},{1,0,0,0},{0,1,0,0},{0,0,0,1}};
		
		IntegerVariable[][] matrix = new IntegerVariable[numberEdgeA][numberEdgeA];
		
		for(int i=0; i<numberEdgeA;i++){
			for(int j=0;j<numberEdgeA;j++){
				matrix[i][j]=Choco.makeIntVar("var_"+i+"_"+j,0,1, "");
				m.addVariable(matrix[i][j]);
			}
		}
		//3-constraints
		
		//row constraints: row==1
		for(int i=0; i<numberEdgeA;i++){
			m.addConstraint(Choco.eq(Choco.sum(matrix[i]),1));
		}
		
		//for column number we keep a separate matrix
		IntegerVariable[][] varCol = new IntegerVariable[numberEdgeA][numberEdgeA];
		for (int i = 0; i < numberEdgeA; i++) {
			for (int j = 0; j < numberEdgeA; j++) {
				// Copy of var in the column order
				varCol[i][j] = matrix[j][i];
			}
			// Each column?s sum is equal to the magic sum
			m.addConstraint(Choco.eq(Choco.sum(varCol[i]), 1));
		}
		
		
		/**
		 * constraint successors: 
		 * linkmapB= linkmap[i]*matrix[i]
		 * 
		 * column of variable matrix are inverted
		 */

		for (int i = 0; i < numberPortA; i++) {
			for (int j = 0; j < numberEdgeA; j++) {
				m.addConstraint(Choco.eq(linkmapB[i][j],
								Choco.scalar(matrix[j], linkmapA[i])));
				
			}
		}
		
		
		
		
		//4-solve the model
		CPSolver s = new CPSolver();
		
		s.read(m);
		// Solve the model
		
		ChocoLogging.toVerbose();
		s.solve();
		s.solveAll();
		//s.solveAll();
		
		System.out.println("Number of solutions found:" + s.getSolutionCount());
		// Print the solution
		
		for (int i = 0; i < numberEdgeA; i++) {

			for (int j = 0; j < numberEdgeA; j++) {
				System.out.print(MessageFormat.format("{0} ",
						s.getVar(matrix[i][j]).getVal()));
			}
			System.out.println("");
		}
		System.out.println("-------------------- solution number: "
				+ s.getSolutionCount());
		
		///  123,9 x 63,2 x 10,9 -  122 x 64 x 9.4
		while (s.nextSolution()) {
			//if (s.getSolutionCount()) {
				for (int i = 0; i < numberPortA; i++) {

					for (int j = 0; j < numberEdgeA; j++) {
						System.out.print(MessageFormat.format("{0} ",
								s.getVar(matrix[i][j]).getVal()));
					}
					System.out.println("");
				}
				System.out.println("-------------------- solution number: "
						+ s.getSolutionCount());
			//}
		}
		

	}

}
