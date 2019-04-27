
public class ConstraintSolverMain {

	public static void main(String[] args) {
		String algoName="";
		int cutOff = 0;
		String inputFilePath="";
		algoName  = args[0];
		if(args.length == 3) {
			cutOff = Integer.parseInt(args[1]);
			inputFilePath = args[2];
		} else {
			inputFilePath = args[1];
		}
		BackTracking bt = new BackTracking();
		MinConflict mc = new MinConflict();
		if ("BACKTRACK".equalsIgnoreCase(algoName)) {
			bt.mainBackTracking(inputFilePath);
		}
		if ("MINCONFLICTS".equalsIgnoreCase(algoName)) {
			mc.mainMinConflict(cutOff, inputFilePath);
		}

	}

}
