

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.DirectedWeightedMultigraph;

public class BackTracking {
	static int backTrackCounter = 0;

	public void mainBackTracking(String file) {
		FileWriter fw;
		try {
			// FinalMap stores final assignment of values
			HashMap<String, Integer> finalMap = new HashMap<String, Integer>();
			// Visited keeps track of visited Nodes
			ArrayList<String> visited = new ArrayList<String>();
			// unaryConstraint contains unary Constraints given in the problem
			HashMap<String, Integer> unaryConstraint = new HashMap<String, Integer>();
			// constraintMap stores mapping of edge weights as String weights are not allowed
			HashMap<String, Integer> constraintMap = new HashMap<String, Integer>();
			constraintMap.put("!=", 0);
			constraintMap.put("=", 1);
			constraintMap.put("<", 2);
			constraintMap.put(">", 3);
			constraintMap.put("<1", 4);
			constraintMap.put(">1", 5);
			constraintMap.put("<>1", 6);
			String firstNode = new String("");
			DirectedWeightedMultigraph<String, DefaultWeightedEdge> g = new DirectedWeightedMultigraph<>(
					DefaultWeightedEdge.class);
			// vertexMap contains Variables with domain values
			HashMap<String, List<Integer>> vertexMap = new HashMap<String, List<Integer>>();
			firstNode = parseInput(file, firstNode, constraintMap, g, vertexMap, unaryConstraint);
			for (Map.Entry m : vertexMap.entrySet()) {
				finalMap.put(String.valueOf(m.getKey()), -1);
			}
			// backtrackingUtil is the BackTrack function
			if (!backtrackingUtil(visited, g, vertexMap, firstNode, finalMap, unaryConstraint)) {
				fw = new FileWriter("Solution.txt",true);
				fw.write("No of Backtracking Steps:" + backTrackCounter + "\n");
				fw.write("Solution does not exist.Conflicts Below\n");
				for (String conflict : getFinalConflict(g, constraintMap, finalMap, unaryConstraint)) {
					fw.write(conflict + "\n");
				}
				fw.close();
			} else {
				fw = new FileWriter("Solution.txt",true);
				fw.write("No of Backtracking Steps:" + backTrackCounter + "\n");
				for (Map.Entry m : finalMap.entrySet()) {
					fw.write(String.valueOf(m.getKey()) + " " + (int) m.getValue() + "\n");
				}
				fw.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String parseInput(String file, String firstNode, HashMap<String, Integer> constraintMap,
			DirectedWeightedMultigraph<String, DefaultWeightedEdge> g, HashMap<String, List<Integer>> vertexMap,
			HashMap<String, Integer> unaryConstraint) {
		String line;

		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			boolean varInput = false;
			boolean constraintInput = false;
			while ((line = br.readLine()) != null) {
				if (line.equalsIgnoreCase("VARS")) {
					varInput = true;
					continue;
				}
				if (line.equalsIgnoreCase("ENDVARS")) {
					varInput = false;
					for (Map.Entry m : vertexMap.entrySet()) {
						g.addVertex(String.valueOf(m.getKey()));
					}
					continue;
				}
				if (line.equalsIgnoreCase("CONSTRAINTS")) {
					constraintInput = true;
					continue;
				}
				if (line.equalsIgnoreCase("ENDCONSTRAINTS")) {
					constraintInput = false;
					continue;
				}
				if (varInput == true) {
					String str[] = line.split(" ", 3);
					if (vertexMap.size() == 0) {
						firstNode = new String(str[0]);
					}
					List<Integer> list = new ArrayList<Integer>();
					String ListString[] = str[2].split(" ");
					for (String i : ListString) {
						list.add(Integer.parseInt(i));
					}
					vertexMap.put(str[0], list);
				}
				if (constraintInput == true) {
					g = CreateGraph(g, line, constraintMap, unaryConstraint);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return firstNode;
	}
	// CreateGraph method updates graph edges by line by line input
	public static DirectedWeightedMultigraph<String, DefaultWeightedEdge> CreateGraph(
			DirectedWeightedMultigraph<String, DefaultWeightedEdge> g, String line,
			HashMap<String, Integer> constraintMap, HashMap<String, Integer> unaryConstraint) {
		String str[] = line.split(" ", 2);
		String weight = str[0];
		String domainValues[] = str[1].split(" ");
		try {
			int weightInt = Integer.parseInt(domainValues[1]); // Unary constraint is handled this way 
			unaryConstraint.put(domainValues[1], weightInt);
			return g;
		} catch (NumberFormatException e) {  // Binary constraint is handled this way
			for (int i = 0; i <= domainValues.length - 2; i++) {
				for (int j = i + 1; j <= domainValues.length - 1; j++) {
					g.addEdge(domainValues[i], domainValues[j]);
					g.setEdgeWeight(domainValues[i], domainValues[j], constraintMap.get(weight));
				}
			}
			return g;
		}
	}
	// Main BackTrack Function
	public static boolean backtrackingUtil(ArrayList<String> visited,
			DirectedWeightedMultigraph<String, DefaultWeightedEdge> g, HashMap<String, List<Integer>> vertexMap,
			String vertex, HashMap<String, Integer> finalMap, HashMap<String, Integer> unaryConstraint) {
		backTrackCounter++;
		// Base step is when there is no conflict with assignment done
		if (finalMap.get(vertex) != -1 && getConflictedVariable(g, finalMap, unaryConstraint).isEmpty()) {
			return true;
		}
		visited.add(vertex);
		// We will iterate over each domain value of variable
		for (int i : vertexMap.get(vertex)) {
			finalMap.put(vertex, i);
			// IsSatisfiesConstraints helps us to choose the correct next node which satisfies constraints 
			if (IsSatisfiesConstraints(vertex, g, finalMap, unaryConstraint)) {
				boolean flag = true;

				for (DefaultWeightedEdge outedge : g.edgesOf(vertex)) {
					// Node will have multiple edges. So we will check for each edge
					//I am getting problem in this loop for Sodoku and Zebra
					// I suspect there is some scenario which occurs in both these case
					// does not treat this line of code well
					// FOr rest of the cases, it is working
					String a = outedge.toString().split(":")[0].trim().replace("(", "");
					String b = outedge.toString().split(":")[1].trim().replace(")", "");
					//edges of method returns any touching edge
					// so we have to take node other than our current node
					if (a.equalsIgnoreCase(vertex)) {
						if (!visited.contains(b)
								&& !backtrackingUtil(visited, g, vertexMap, b, finalMap, unaryConstraint)) {
							flag = false;
							break;
						}
					} else {
						if (!visited.contains(a)
								&& !backtrackingUtil(visited, g, vertexMap, a, finalMap, unaryConstraint)) {
							flag = false;
							break;
						}
					}

				}
				if (flag) {
					return true;
				}

			}
		}
		// Remove entry from visited and finalMap as this does not return true 
		visited.remove(vertex);
		finalMap.put(vertex, -1);
		return false;
	}
	// IsSatisfiesConstraints check whether a value for a node satisfies constraints or not
	// Utility class contains different method implementations for different symbols
	public static boolean IsSatisfiesConstraints(String vertex,
			DirectedWeightedMultigraph<String, DefaultWeightedEdge> g, HashMap<String, Integer> finalMap,
			HashMap<String, Integer> unaryConstraint) {
		boolean isGood = true;
		try {
			int x = Integer.parseInt(vertex);
			if (finalMap.get(vertex) != unaryConstraint.get(vertex)) {
				return false;
			}
		} catch (NumberFormatException e) {
			for (DefaultWeightedEdge outedge : g.edgesOf(vertex)) {
				int a = finalMap.get(outedge.toString().split(":")[0].trim().replace("(", ""));
				int b = finalMap.get(outedge.toString().split(":")[1].trim().replace(")", ""));
				if (a != -1 && b != -1) {
					int weight = (int) g.getEdgeWeight(outedge);
					try {
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
							return isGood;
						}
					} catch (Exception e1) {

						e1.printStackTrace();
					}

				}
			}
		}
		return isGood;
	}
	// getConflictedVariable is used to check test condition
	public static HashSet<String> getConflictedVariable(DirectedWeightedMultigraph<String, DefaultWeightedEdge> g,
			HashMap<String, Integer> finalMap, HashMap<String, Integer> unaryConstraint) {
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
	// getFinalConflict is used to fetch all the conflicts occured that caused failure
	public static HashSet<String> getFinalConflict(DirectedWeightedMultigraph<String, DefaultWeightedEdge> g,
			HashMap<String, Integer> constraintMap, HashMap<String, Integer> finalMap,
			HashMap<String, Integer> unaryConstraint) {
		HashSet<String> conflictedVariables = new HashSet<String>();
		;
		for (Map.Entry<String, Integer> m : finalMap.entrySet()) {
			conflictedVariables = new HashSet<String>();
			String vertex = m.getKey();
			if (unaryConstraint.containsKey(vertex) && unaryConstraint.get(vertex) != finalMap.get(vertex)) {
				conflictedVariables.add("(" + vertex + " : " + unaryConstraint.get(vertex) + ")");
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
