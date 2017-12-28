 package programEntry;

/**
 * Created by lida on 2016/11/7.
 */
public class GP {
    // in the chronological order
    public static boolean filterString = true; // filter string-type formula cells.
    public static boolean plusFrozen = true; // differ frozen blocks and free blocks.

	public static boolean addA = false;
	public static boolean addB = false;
	public static boolean addC = true;

    public static boolean plusCellArray = false; // Extract cell array first, before the 1st stage clustering.
    public static boolean plusFirstSecond = false; //First formula and their dependence, 2nd data during the 2nd stage.
    public static boolean plusTuning = false; // wisely tune the threshold used in 2nd stage clustering.
    public static boolean plusExtendSmallClu = false; // After 2nd stage, extend small clusters by cell array info.


	public static String usedOptimizations() {
		return "";
	}
}
