package thirdparty.CACheck.amcheck;

public class AnalysisPattern {

	private static int index = 1;

	/**
	 * Only extract the correct cell arrays
	 */
	public static int Correct_CellArray = index++;

	/**
	 * The cell array refers the cells in the same row or column
	 */
	public static int Same_Row_Or_Column_Cells = index++;

	/**
	 * The cell array refers the cells that share some dependence
	 */
	public static int Share_Cells = index++;

	/**
	 * Don't use heuristic
	 */
	public static int NO = index++;

	/**
	 * Filter out all overlaped CAs.
	 */
	public static int OVERLAP = index++;

	/**
	 * Only care about conformance errors.
	 */
	public static int Conformance_Cell = index++;

	/**
	 * Only care about ambiguous cells
	 */
	public static int Ambiguous_Cell = index++;

	/**
	 * Care about ambiguous cells, and refer change is not wrong.
	 */
	public static int Ambiguous_Cell_ReferChange = index++;

	/**
	 * Care about conformance errors, and include as many as correct cell
	 * arrays.
	 */
	public static int Conformance_And_Correct_Cell = index++;

	/**
	 * The current patterns
	 */
	public int curPattern;

	/**
	 * The filter heuristic
	 */
	public int curHeuristic;

	public static boolean refined = false;

	public static boolean simple = false;
	
	public static boolean overlapDataCell = true;

	public static int Result_All = index++;
	public static int Result_Same = index++;
	public static int Result_Diff = index++;
	public static int Result_Opposite = index++;

	public int resultType = Result_All;

	public boolean aggressiveFilter = false;

	public static boolean cached = false;

	public AnalysisPattern() {

	}

	public void setType(int type) {
		if (type == 1) {
			// Correct cell array extraction
			this.curPattern = AnalysisPattern.Correct_CellArray;
			this.curHeuristic = AnalysisPattern.NO;
		}
		if (type == 2) {
			// Correct cell array extraction, all overlap CAs are filtered out.
			this.curPattern = AnalysisPattern.Correct_CellArray;
			this.curHeuristic = AnalysisPattern.OVERLAP;
		} else if (type == 3) {
			// Same row or column cell array extraction
			this.curPattern = AnalysisPattern.Same_Row_Or_Column_Cells;
			this.curHeuristic = AnalysisPattern.NO;
		} else if (type == 5) {
			// Share dependence, minimize conformance cells, and include correct
			// cell arrays.
			this.curPattern = AnalysisPattern.Share_Cells;
			this.curHeuristic = AnalysisPattern.Conformance_And_Correct_Cell;
			this.aggressiveFilter = false;
		} else if (type == 6) {
			// Share dependence, minimize conformance cells, and include correct
			// cell arrays, and aggressive filter.
			this.curPattern = AnalysisPattern.Share_Cells;
			this.curHeuristic = AnalysisPattern.Conformance_And_Correct_Cell;
			this.aggressiveFilter = true;
		} else if (type == 10) {
			// Share dependence, minimize conformance cells
			this.curPattern = AnalysisPattern.Share_Cells;
			this.curHeuristic = AnalysisPattern.Conformance_Cell;
		} else if (type == 11) {
			// Share dependence, minimize conformance cells, and include
			// correct cell arrays.
			this.curPattern = AnalysisPattern.Share_Cells;
			this.curHeuristic = AnalysisPattern.Conformance_And_Correct_Cell;
		}
	}

	public static String getTypeName(int type) {
		String typeName = null;
		if (type == 1) {
			typeName = "correct";
		}
		if (type == 2) {
			typeName = "correct-overlap";
		} else if (type == 3) {
			typeName = "same";
		} else if (type == 5) {
			typeName = "share";
		} else if (type == 6) {
			typeName = "share-aggressive";
		} else if (type == 10) {
			typeName = "share-conformance";
		} else if (type ==11) {
			typeName = "share-conformance-correct";
		}
		return typeName;
	}
}
