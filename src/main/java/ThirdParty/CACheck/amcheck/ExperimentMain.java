package ThirdParty.CACheck.amcheck;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ThirdParty.CACheck.util.DetailResultWriter;
import ThirdParty.CACheck.util.Log;
import ThirdParty.CACheck.util.ProcessLock;
import ThirdParty.CACheck.util.SmellyCellWriter;
import ThirdParty.CACheck.util.TotalResultWriter;
import ThirdParty.CACheck.util.Utils;

public class ExperimentMain {

	public static void main(String[] args) throws Exception {
		
		ValidateCA.load();
		
		String corpus = null;
		String category = null;
		String version = null;
		int type = 6; // default type
		String xlsFile = null; // the excel file

		if (args.length < 3) {
			System.out.println("You don't use correct parameters!");
			System.out.println("Example 1: corpus category version");
			System.out.println("Example 2: corpus category version type");
			System.out.println("Example 3: corpus category version type file");
			return;
		}
		corpus = args[0];
		category = args[1];
		version = args[2];
		if (args.length == 4) {
			type = Integer.parseInt(args[3]);
		}
		if (args.length == 5) {
			type = Integer.parseInt(args[3]);
			xlsFile = args[4];
		}

		AnalysisPattern analysisPattern = new AnalysisPattern();
		analysisPattern.setType(type);

		try {
			process(corpus, category, version, type, xlsFile, analysisPattern);
		} catch (Exception e) {
			e.printStackTrace(System.err);
			System.exit(1);
		} finally {
			System.exit(0);
		}
	}

	public static void process(String corpus, String category, String version,
			int type, String xlsFile, AnalysisPattern analysisPattern) throws Exception {

		String dir = Utils.exprDir();
		File inDir = new File(dir + corpus + "/" + category);
		String outDir = dir + "output-" + corpus + "-" + version + "/"
				+ AnalysisPattern.getTypeName(type) + "/";
		if ("debug".equals(corpus)) {
			outDir = dir + "output-debug/";
		}

		File logFile = new File(outDir + category + ".txt");
		File resFile = new File(outDir + "results.xls");
		File smellyCellFile = new File(outDir + "smellycells.xls");
		File detailFile = new File(outDir + category + "-details.xls");

		boolean isDir = (xlsFile == null);

		if (isDir) {
			ProcessLock lock = new ProcessLock();
			lock.lock();

			if (!new File(outDir).exists()) {
				new File(outDir).mkdirs();
			}
			// log file
			if (logFile.exists()) {
				logFile.delete();
			}
			logFile.createNewFile();

			lock.unlock();
		}

		BufferedWriter writer = new BufferedWriter(
				new FileWriter(logFile, true));
		Log.writer = writer;

		TotalResultWriter.init(resFile, category, isDir);
		DetailResultWriter.init(detailFile, category, isDir);
		SmellyCellWriter.init(smellyCellFile, category, isDir);

		if (!isDir) {
			long startTime = System.currentTimeMillis();

			File file = new File(inDir.getAbsoluteFile() + "/" + xlsFile);

			if (ExcelPreProcess.preProcess(file)) {
				ExcelAnalysis.processExcel(category, file, outDir, writer, analysisPattern);
			}

			long endTime = System.currentTimeMillis();
			TotalResultWriter.curData.time = (endTime - startTime) / 1000;

			TotalResultWriter.saveTotalResult();
		} else {
			// analyze every excel
			ExecutorService executor = Executors.newFixedThreadPool(10);
			for (File file : ExcelPreProcess.getAllExcels(inDir)) {
				processExcel(corpus, category, version, type, file.getName(),
						executor, analysisPattern);
			}
			executor.shutdown();
			executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);

			// print total result
			TotalResultWriter.printTotalResult(writer);
		}

		writer.close();
	}

	public static void processExcel(final String corpus, final String category,
			final String version, final int type, final String xlsFile,
			ExecutorService executor, AnalysisPattern analysisPattern) throws Exception {

		if (category.equals("debug")) {
			process(corpus, category, version, type, xlsFile, analysisPattern);
			return;
		}

		// Use anther process to execute.
		Runnable thread = () -> {
            try {
                String toolDir = Utils.exprDir() + "tools";
                String tmpDir = Utils.exprDir() + "tmp";
                if (!new File(tmpDir).exists()) {
                    new File(tmpDir).mkdirs();
                }

                final ProcessBuilder pb = new ProcessBuilder();
                pb.directory(new File(toolDir));
                final File tmpFile = File.createTempFile(xlsFile + "-",
                        ".txt", new File(tmpDir));
                // tmpFile.deleteOnExit();
                pb.redirectOutput(tmpFile);

                if (System.getProperty("os.name").toLowerCase()
                        .contains("windows")) {
                    pb.command("cmd.exe", "/c", "amcheck.bat", corpus,
                            category, version, "" + type, xlsFile);
                } else {
                    pb.command("./amcheck.sh", corpus, category, version, "" + type,
                        xlsFile);
            }

                Process p = pb.start();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(p.getErrorStream()));
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        Log.log(line, Log.writer);
                        Log.logNewLine(Log.writer);
                    }
                } catch (Exception e) {
                }
                p.waitFor();

                tmpFile.delete();
            } catch (Exception e) {
                Log.logNewLine(e, Log.writer);
            }
        };
		executor.execute(thread);
	}
}
