package ThirdParty.CACheck.util.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ThirdParty.CACheck.util.DetailResultWriter;
import ThirdParty.CACheck.util.TotalResultWriter;

public class CombineAggAllInOne {

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
		String shareAggOutDir = AnalysisUtil.getDir(outDir, DirType.share_agg
				+ "_allinone");
		if (!new File(shareAggOutDir).exists()) {
			new File(shareAggOutDir).mkdirs();
		}

		File shareAggResFile = AnalysisUtil.getResultFile(outDir,
				DirType.share_agg + "_allinone", null);

		File shareAggDetailFile = AnalysisUtil.getDetailFile(outDir, "cs101",
				DirType.share_agg + "_allinone", null);

		// Retrieve cell arrays
		List<CA> allCAs = new ArrayList<CA>();
		for (String category : AnalysisUtil.getCategories(corpus)) {
			for (int sheetNum = 1; sheetNum < 8; sheetNum++) {
				File aggDetailFile = AnalysisUtil.getDetailFile(outDir,
						category, DirType.share_agg, null);
				DetailResultWriter.init(aggDetailFile, category, false);
				List<CA> CAs = DetailResultWriter.readSmells(sheetNum);
				allCAs.addAll(CAs);
			}
		}
		boolean resultInit = true;
		DetailResultWriter.init(shareAggDetailFile, "cs101", resultInit);
		TotalResultWriter.init(shareAggResFile, "cs101", resultInit);

		AnalysisUtil.totalStatistics(allCAs);
		TotalResultWriter.saveTotalResult();

		for (CA ca : allCAs) {
			ca.percentage = ca.percentage + 1;
		}
		DetailResultWriter.addCAResult2(allCAs);

	}
}
