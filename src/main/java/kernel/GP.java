 package kernel;

 import statistics.StatisticsForAll;

import java.io.BufferedWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

 /**
 * Created by lida on 2016/11/7.
 */
public class GP {
    // in the chronological order
    public static boolean filterString = false; // filter string-type formula cells.
    public static boolean plusFrozen = false; // differ frozen blocks and free blocks.

    //effectiveness
	public static boolean addA = false;
	public static boolean addB = false;
	public static boolean addC = false;
	public static boolean clusterOptimization = true;

	//efficiency
    //TODO: bugs happen which result in wrong results
	public static boolean addD = false; //adjust different R1C1 cell reference (absolute and/or relative), 已弃用

    static AtomicInteger finishedSS = new AtomicInteger(0);
    static AtomicInteger finishedWS = new AtomicInteger(0);

    public static boolean plusTuning = false; // wisely tune the threshold used in 2nd stage clustering.

    public static String fileSeparator = System.getProperty("file.separator");
    public static String parent_dir = System.getProperty("user.dir");
    public static String outDirPath = parent_dir + fileSeparator + "Outputs";
    public static String testDate;
    public static boolean buildArtifact = false;

    public static ExecutorService exeService;
    public static List<TestWorksheet> tasks = new ArrayList<>();
    public static List<Future<?>> futures = new ArrayList<>();
    public static Boolean [] finishs;
    public static ConcurrentHashMap<String, AtomicInteger> printFlag = new ConcurrentHashMap<>();

    public static StatisticsForAll staAll;
    public static String prefixOutDir;
    public static File logFile;
    public static BufferedWriter logBuffer = null;
    public static AtomicInteger index;

    private static final int CAPACITY = 50;
    public static Semaphore semaphore = new Semaphore(CAPACITY-2);
    public static Semaphore futureSemaphore = new Semaphore(0);

     static {
//         if (addA) testDate += "A";
//         if (addB) testDate += "B";
//         if (addC) testDate += "C";
//         if (addD) testDate += "D";

         //TODO: 当队列长度饱和时，采用CallerRuns策略，让主线程阻塞，并开始执行当前准备发布的子线程任务。
         //RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();
         //exeService = new ThreadPoolExecutor(8, 8, 0, TimeUnit.MILLISECONDS,
                 //new ArrayBlockingQueue<Runnable>(CAPACITY));
            exeService = Executors.newFixedThreadPool(8);

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
//		if (addA) builder.append("A");
//		if (addB) builder.append("B");
//		if (addC) builder.append("C");
//        if (addD) builder.append("D");

	    return builder.toString();
	}
}
