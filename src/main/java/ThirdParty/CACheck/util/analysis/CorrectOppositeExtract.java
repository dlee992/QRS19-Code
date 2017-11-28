package ThirdParty.CACheck.util.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ThirdParty.CACheck.util.DetailResultWriter;
import ThirdParty.CACheck.util.TotalResultWriter;

public class CorrectOppositeExtract {

	public static void main(String[] args) throws Exception {
		String corpus = null;
		String outDir = null;

		if (args.length < 2) {
			System.out.println("You don't use correct parameters!");
			System.out.println("Example1: euses output-xxx");
			System.out.println("Example2: casestudy output-xxx");
			System.out.println("Example3: enron output-xxx");
			return;
		}
		corpus = args[0];
		outDir = args[1];

		// Initilize
		String oppOutDir = AnalysisUtil
				.getDir(outDir, DirType.correct_opposite);
		if (!new File(oppOutDir).exists()) {
			new File(oppOutDir).mkdirs();
		}

		doOppAnalysis(corpus, outDir, 0); // all op cas;
		doOppAnalysis(corpus, outDir, 1); // with 1 op cas;
		doOppAnalysis(corpus, outDir, 2); // with 2 op cas;
		doOppAnalysis(corpus, outDir, 3); // with 3 op cas;
		doOppAnalysis(corpus, outDir, 4); // with 4 op cas;
		doOppAnalysis(corpus, outDir, 5); // with 5 op cas;
		doOppAnalysis(corpus, outDir, 6); // with 6 op cas;
	}

	private static void doOppAnalysis(String corpus, String outDir,
			int threshold) throws Exception {
		String type = threshold + "";
		if (threshold == 0) {
			type = "all";
		}

		File oppResFile = AnalysisUtil.getResultFile(outDir,
				DirType.correct_opposite, type);

		// Retrieve cell arrays
		for (String category : AnalysisUtil.getCategories(corpus)) {
			File correctDetailFile = AnalysisUtil.getDetailFile(outDir,
					category, DirType.correct_overlap, null);
			File oppDetailFile = AnalysisUtil.getDetailFile(outDir, category,
					DirType.correct_opposite, type);

			boolean resultInit = true;
			for (int sheetNum = 0; sheetNum < 8; sheetNum++) {
				// just read files
				DetailResultWriter.init(correctDetailFile, category, false);
				List<CA> correctCAs = DetailResultWriter.readSmells(sheetNum);

				List<CA> oppCAs = new ArrayList<CA>();
				for (CA ca : correctCAs) {
					if (threshold == 0) {
						if (ca.oppositeInputs > 0) {
							oppCAs.add(ca);
						}
					} else if (threshold >= 1 && threshold <= 5) {
						if (ca.oppositeInputs == threshold) {
							oppCAs.add(ca);
						}
					} else if (threshold > 5) {
						if (ca.oppositeInputs > 5) {
							oppCAs.add(ca);
						}
					}
				}

				DetailResultWriter.init(oppDetailFile, category, resultInit);
				TotalResultWriter.init(oppResFile, category, resultInit);
				if (resultInit) {
					resultInit = false;
				}

				AnalysisUtil.totalStatistics(oppCAs);
				DetailResultWriter.addCAResult2(oppCAs);
				TotalResultWriter.saveTotalResult();
			}
		}
	}
}
