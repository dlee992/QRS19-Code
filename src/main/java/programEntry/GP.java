 package programEntry;

 import experiment.StatisticsForAll;
 import utility.BasicUtility;

 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicInteger;

 /**
 * Created by lida on 2016/11/7.
 */
public class GP {
    // in the chronological order
    public static boolean filterString = true; // filter string-type formula cells.
    public static boolean plusFrozen = true; // differ frozen blocks and free blocks.

	public static boolean addA = true;
	public static boolean addB = true;
	public static boolean addC = true;

    public static boolean plusCellArray = false; // Extract cell array first, before the 1st stage clustering.
    public static boolean plusFirstSecond = false; //First formula and their dependence, 2nd data during the 2nd stage.
    public static boolean plusTuning = false; // wisely tune the threshold used in 2nd stage clustering.
    public static boolean plusExtendSmallClu = false; // After 2nd stage, extend small clusters by cell array info.

    public static String fileSeparator = System.getProperty("file.separator");
    public static String parent_dir = System.getProperty("user.dir");
    public static String outDirPath = parent_dir + fileSeparator + "Outputs";
    public static String testDate = "2018-01-23 Prototype idea";

    public static ExecutorService exeService;
    public static StatisticsForAll staAll;
    public static String prefixOutDir;
    public static File logFile;
    public static BufferedWriter logBuffer = null;
    public static AtomicInteger index;

    static {
         if (addA) testDate += "A";
         if (addB) testDate += "B";
         if (addC) testDate += "C";

         exeService = Executors.newFixedThreadPool(8);

         prefixOutDir = outDirPath + fileSeparator + "MiddleTemp " + new BasicUtility().getCurrentTime() + fileSeparator;
         File middleDir = new File(prefixOutDir);
         if (!middleDir.exists()) {
             middleDir.mkdir();
         }

//         logFile = new File(prefixOutDir + "logInfo " + new BasicUtility().getCurrentTime() + ".txt");
//         if (!logFile.exists()) {
//             try {
//                 logFile.createNewFile();
//                 logBuffer = new BufferedWriter(new FileWriter(logFile));
//             } catch (IOException e) {
//                 e.printStackTrace();
//             }
//         }

         index = new AtomicInteger();
    }

	public static String addSuffix() {
		StringBuilder builder = new StringBuilder();
		if (addA) builder.append("A");
		if (addB) builder.append("B");
		if (addC) builder.append("C");

	    return builder.toString();
	}
}
