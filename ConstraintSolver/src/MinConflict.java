

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

public class MinConflict {
	public void mainMinConflict(int maxSteps, String file) {
		FileWriter fw;
		boolean solutionFlag = false;
		String variable = "";
		int variableValue = -1;
		try {
		int stepsRequired = 0;
		// constraintMap stores mapping of edge weights as String weights are not allowed
		HashMap<String,Integer> constraintMap = new HashMap<String, Integer>();
		// FinalMap stores final assignment of values
		HashMap<String,Integer> finalMap = new HashMap<String, Integer>();
		// unaryConstraint contains unary Constraints given in the problem
		HashMap<String,Integer> unaryConstraint = new HashMap<String, Integer>();
		constraintMap.put("!=", 0);
		constraintMap.put("=", 1);
		constraintMap.put("<", 2);
		constraintMap.put(">", 3);
		constraintMap.put("<1", 4);
		constraintMap.put(">1", 5);
		constraintMap.put("<>1", 6);
		DirectedWeightedMultigraph<String, DefaultWeightedEdge> g = new DirectedWeightedMultigraph<>(DefaultWeightedEdge.class);
		HashMap<String,List<Integer>> vertexMap = new HashMap<String, List<Integer>>();	
		parseInput(file, constraintMap, g, vertexMap, unaryConstraint);
		//initial assignment random
		for (Map.Entry<String,List<Integer>> m : vertexMap.entrySet()) {
			finalMap.put(m.getKey(), m.getValue().get(0));
		}
		// Iterating over max Steps( Cutoff )
		for (int i = 1; i<=maxSteps; i++) {
			// finding conflicting Variables 
			HashSet<String> conflictedVariables = getConflictedVariable(g, finalMap, unaryConstraint);
			// If no conflicting variables, then assignment is correct. Success.
			if (conflictedVariables.isEmpty()) {
				solutionFlag = true;
				stepsRequired = i;
				break;
			} 
			// if Not, then get fetch one conflicting variable
			variable = conflictedVariables.iterator().next();
			//chose domain value for that variable which causes minimum conflicts
			variableValue = choseMinimumConflictValueOfVariable(variable, vertexMap, g, constraintMap, unaryConstraint, finalMap);
			// Update assignment map with that value
			finalMap.put(variable,variableValue);
		}
		if (solutionFlag == true) {
			fw = new FileWriter("Solution.txt",true);
			fw.write("Solution exists. No. of Steps taken: "+stepsRequired+"\n");
			for (Map.Entry<String,Integer> m : finalMap.entrySet()) {
				fw.write(m.getKey()+ " "+m.getValue()+"\n");
			}
			fw.close();
		} else {
			fw = new FileWriter("Solution.txt",true);
			fw.write("Solution not found in maxSteps given: "+maxSteps+"\n");
			fw.write("Conflicts Found:\n");
				for (String conflict : getFinalConflict(g, constraintMap, finalMap, unaryConstraint)) {
					fw.write(conflict + "\n");
				}
				fw.close();
		}
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	// parses input to create graph
	public static void parseInput(String file, HashMap<String,Integer>constraintMap, DirectedWeightedMultigraph<String, DefaultWeightedEdge> g, HashMap<String,List<Integer>> vertexMap, HashMap<String,Integer> unaryConstraint ) {
		String line;
			
		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			boolean varInput= false;
			boolean constraintInput = false;
			while ( (line = br.readLine()) != null) {
				if (line.equalsIgnoreCase("VARS")) {
					varInput =true;
					continue;
				} 
				if (line.equalsIgnoreCase("ENDVARS")) {
					varInput =false;
					for (Map.Entry m : vertexMap.entrySet()) {
						g.addVertex(String.valueOf(m.getKey()));
					}
					continue;
				} 
				if (line.equalsIgnoreCase("CONSTRAINTS")) {
					constraintInput =true;
					continue;
				} 
				if (line.equalsIgnoreCase("ENDCONSTRAINTS")) {
					constraintInput =false;
					continue;
				} 
				if  (varInput ==true) {
					String str[] = line.split(" ",3);
					List <Integer> list = new ArrayList<Integer>();
					String ListString[] = str[2].split(" ");
					for (String i: ListString) {
						list.add(Integer.parseInt(i));
					}
					vertexMap.put(str[0], list);
				}
				if (constraintInput == true) {
					g= CreateGraph(g,line,constraintMap,unaryConstraint );
				}	
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// CreateGraph method updates graph edges by line by line input
	public static DirectedWeightedMultigraph<String, DefaultWeightedEdge> CreateGraph(DirectedWeightedMultigraph<String, DefaultWeightedEdge> g, String line, HashMap<String,Integer>constraintMap, HashMap<String,Integer> unaryConstraint ) {
		String str[] = line.split(" ",2);
		String weight = str[0];
		String domainValues[] = str[1].split(" ");
		try {
			int weightInt = Integer.parseInt(domainValues[1]); // Unary constraint
			unaryConstraint.put(domainValues[1], weightInt);
			return g;
		}
		catch(NumberFormatException e) {
			for (int i=0;i<=domainValues.length -2; i++) {
				for (int j=i+1; j<=domainValues.length-1; j++) {
					g.addEdge(domainValues[i], domainValues[j]);
					g.setEdgeWeight(domainValues[i], domainValues[j],constraintMap.get(weight));
				}
			}	
			return g;
		}
	}
	// getConflictedVariable gives set containing cconflicting variable
	public static HashSet<String> getConflictedVariable(DirectedWeightedMultigraph<String, DefaultWeightedEdge> g,
			HashMap<String, Integer> finalMap,
			HashMap<String, Integer> unaryConstraint) {
		HashSet<String> conflictedVariables = new HashSet<String>();
		for (Map.Entry<String, Integer> m : finalMap.entrySet()) {
			String vertex = m.getKey();
			if (unaryConstraint.containsKey(vertex) && unaryConstraint.get(vertex) != finalMap.get(vertex)) {
				conflictedVariables.add(vertex);
			}
			for (DefaultWeightedEdge outedge : g.edgesOf(vertex)) {
				int a = finalMap.get(outedge.toString().split(":")[0].trim().replace("(", ""));
				int b = finalMap.get(outedge.toString().split(":")[1].trim().replace(")", ""));
				boolean isGood = true;
				int weight = (int) g.getEdgeWeight(outedge);

				switch (weight) {
				case 0:
					isGood = new Utility().NotEqual(a, b);
					break;
				case 1:
					isGood = new Utility().Equal(a, b);
					break;
				case 2:
					isGood = new Utility().lessThan(a, b);
					break;
				case 3:
					isGood = new Utility().greaterThan(a, b);
					break;
				case 4:
					isGood = new Utility().oneLessThan(a, b);
					break;
				case 5:
					isGood = new Utility().oneGreaterThan(a, b);
					break;
				case 6:
					isGood = new Utility().oneLessOrGreaterThan(a, b);
					break;
				}

				if (isGood == false) {
					conflictedVariables.add(vertex);
				}
			}
		}

		return conflictedVariables;
	}
	
	//choseMinimumConflictValueOfVariable returns value of a variable with minimum conflict
	public static int choseMinimumConflictValueOfVariable(String variable, HashMap<String, List<Integer>> vertexMap,
			DirectedWeightedMultigraph<String, DefaultWeightedEdge> g, HashMap<String, Integer> constraintMap,
			HashMap<String, Integer> unaryConstraint, HashMap<String,Integer> finalMap) {
		int finalValue = -1;
		int minConflictNumber = Integer.MAX_VALUE;
		for (int i : vertexMap.get(variable)) {
			int tempNoOfComflicts = 0;
			finalMap.put(variable, i);
			if (unaryConstraint.containsKey(variable) && unaryConstraint.get(variable) != finalMap.get(variable)) {
				tempNoOfComflicts++;
			}
			for (DefaultWeightedEdge outedge : g.edgesOf(variable)) {
				int a = finalMap.get(outedge.toString().split(":")[0].trim().replace("(", ""));
				int b = finalMap.get(outedge.toString().split(":")[1].trim().replace(")", ""));
				boolean isGood = true;
				int weight = (int) g.getEdgeWeight(outedge);

				switch (weight) {
				case 0:
					isGood = new Utility().NotEqual(a, b);
					break;
				case 1:
					isGood = new Utility().Equal(a, b);
					break;
				case 2:
					isGood = new Utility().lessThan(a, b);
					break;
				case 3:
					isGood = new Utility().greaterThan(a, b);
					break;
				case 4:
					isGood = new Utility().oneLessThan(a, b);
					break;
				case 5:
					isGood = new Utility().oneGreaterThan(a, b);
					break;
				case 6:
					isGood = new Utility().oneLessOrGreaterThan(a, b);
					break;
				}

				if (isGood == false) {
					tempNoOfComflicts++;
				}
			}
			
			if (tempNoOfComflicts<minConflictNumber) {
				minConflictNumber = tempNoOfComflicts;
				finalValue = i;
			}
		}
		return finalValue;
	}
	// getFinalConflict returns conflcits in case of failure ie. no success before cutoff
	public static HashSet<String> getFinalConflict(DirectedWeightedMultigraph<String, DefaultWeightedEdge> g,
			HashMap<String, Integer> constraintMap, HashMap<String, Integer> finalMap,
			HashMap<String, Integer> unaryConstraint) {
		HashSet<String> conflictedVariables = new HashSet<String>();;
		for (Map.Entry<String, Integer> m : finalMap.entrySet()) {
			conflictedVariables = new HashSet<String>();
			String vertex = m.getKey();
			if (unaryConstraint.containsKey(vertex) && unaryConstraint.get(vertex) != finalMap.get(vertex)) {
				conflictedVariables.add("("+vertex+" : "+unaryConstraint.get(vertex)+")");
			}
			for (DefaultWeightedEdge outedge : g.edgesOf(vertex)) {
				int a = finalMap.get(outedge.toString().split(":")[0].trim().replace("(", ""));
				int b = finalMap.get(outedge.toString().split(":")[1].trim().replace(")", ""));
				boolean isGood = true;
				int weight = (int) g.getEdgeWeight(outedge);

				switch (weight) {
				case 0:
					isGood = new Utility().NotEqual(a, b);
					break;
				case 1:
					isGood = new Utility().Equal(a, b);
					break;
				case 2:
					isGood = new Utility().lessThan(a, b);
					break;
				case 3:
					isGood = new Utility().greaterThan(a, b);
					break;
				case 4:
					isGood = new Utility().oneLessThan(a, b);
					break;
				case 5:
					isGood = new Utility().oneGreaterThan(a, b);
					break;
				case 6:
					isGood = new Utility().oneLessOrGreaterThan(a, b);
					break;
				}

				if (isGood == false) {
					conflictedVariables.add(String.valueOf(outedge));
				}
			}
		}

		return conflictedVariables;
	}

}


