package thirdparty.CACheck.util.analysis;

import java.io.File;
import java.util.List;

import thirdparty.CACheck.util.TotalResultWriter;
import thirdparty.CACheck.util.Utils;

public class AnalysisUtil {

	public static void totalStatistics(List<CA> cas) {
		TotalResultWriter.curData = new TotalResultWriter();

		for (CA ca : cas) {
			if (ca.isMissing || ca.isInconsistent) {
				ca.isAmbiguous = true;
			}

			TotalResultWriter.curData.cellArray++;
			if (ca.isAmbiguous) {
				TotalResultWriter.curData.smellyCellArray++;
			} else {
				TotalResultWriter.curData.correctCellArray++;
			}

			if (ca.isSameRowOrColumn) {
				TotalResultWriter.curData.cellArraySameRowOrColumn++;
				if (ca.isAmbiguous) {
					TotalResultWriter.curData.smellyCellArraySameRowOrColumn++;
				} else {
					TotalResultWriter.curData.correctCellArraySameRowOrColumn++;
				}
			}

			if (ca.isMissing) {
				TotalResultWriter.curData.cellArrayMissingFormula++;
			}
			if (ca.isInconsistent) {
				TotalResultWriter.curData.cellArrayInconsistentFormula++;
			}

			if (ca.isAmbiguous) {
				TotalResultWriter.curData.conformanceError += ca.errorCells;
			}

			if (ca.isAmbiguous) {
				float percentage = ca.percentage;

				if (percentage >= 1.0) {
					TotalResultWriter.curData.cellArrayConformanceError100++;
				} else if (percentage >= 0.9 && percentage < 1.0) {
					TotalResultWriter.curData.cellArrayConformanceError101++;
				} else if (percentage >= 0.8 && percentage < 0.9) {
					TotalResultWriter.curData.cellArrayConformanceError102++;
				} else if (percentage >= 0.7 && percentage < 0.8) {
					TotalResultWriter.curData.cellArrayConformanceError103++;
				} else if (percentage >= 0.6 && percentage < 0.7) {
					TotalResultWriter.curData.cellArrayConformanceError104++;
				} else if (percentage >= 0.5 && percentage < 0.6) {
					TotalResultWriter.curData.cellConformanceError105++;
				} else {
					TotalResultWriter.curData.cellArrayConformanceError106++;
				}
			}
		}

	}

	public static String hashKey(CA ca) {
		return ca.category + ca.excel + ca.worksheet + ca.cellArray;
	}

	public static String[] getCategories(String corpus) {
		if (corpus.startsWith("euses")) {
			return new String[] { "cs101", "database", "filby", "financial",
					"forms3", "grades", "homework", "inventory", "jackson",
					"modeling", "personal" };
		}
		if (corpus.equals("casestudy")) {
			return new String[] { "c1", "c2", "c3", "c4", "c5", "c6", "c7",
					"c8", "c9", "c10" };
		}
		if (corpus.equals("casestudy2")) {
			return new String[] { "cs1", "cs2", "cs3", "cs4", "cs5", "cs6",
					"cs7", "cs8", "cs9", "cs10", "cs11", "cs12", "cs13",
					"cs14", "cs15", "cs16" };
		}
		if (corpus.equals("enron")) {
			return new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "j",
					"k", "l", "m", "p", "r", "s", "t", "v" };
		}
		return null;
	}

	public static File getDetailFile(String outDir, String category,
			String analysisType, String compareType) {
		String dir = getDir(outDir, analysisType);
		if (compareType != null) {
			File detailFile = new File(dir + compareType + "-" + category
					+ "-details.xls");
			return detailFile;
		} else {
			File detailFile = new File(dir + category + "-details.xls");
			return detailFile;
		}
	}

	public static File getResultFile(String outDir, String analysisType,
			String compareType) {
		String dir = getDir(outDir, analysisType);
		if (compareType != null) {
			File resultFile = new File(dir + compareType + "-results.xls");
			return resultFile;
		} else {
			File resultFile = new File(dir + "results.xls");
			return resultFile;
		}
	}

	public static String getDir(String outDir, String analysisType) {
		String dir = Utils.exprDir();
		dir = dir + outDir + "/" + analysisType + "/";
		return dir;
	}
}
