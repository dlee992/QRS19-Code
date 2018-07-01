package thirdparty.CACheck.util.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import thirdparty.CACheck.util.DetailResultWriter;
import thirdparty.CACheck.util.TotalResultWriter;

public class CombineAggAll {

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
				+ "_all");
		if (!new File(shareAggOutDir).exists()) {
			new File(shareAggOutDir).mkdirs();
		}

		File shareAggResFile = AnalysisUtil.getResultFile(outDir,
				DirType.share_agg + "_all", null);

		// Retrieve cell arrays
		for (String category : AnalysisUtil.getCategories(corpus)) {

			File shareAggDetailFile = AnalysisUtil.getDetailFile(outDir,
					category, DirType.share_agg + "_all", null);

			List<CA> allCAs = new ArrayList<CA>();
			for (int sheetNum = 1; sheetNum < 8; sheetNum++) {
				File aggDetailFile = AnalysisUtil.getDetailFile(outDir,
						category, DirType.share_agg, null);
				DetailResultWriter.init(aggDetailFile, category, false);
				List<CA> CAs = DetailResultWriter.readSmells(sheetNum);
				allCAs.addAll(CAs);
			}

			boolean resultInit = true;
			DetailResultWriter.init(shareAggDetailFile, category, resultInit);
			TotalResultWriter.init(shareAggResFile, category, resultInit);

			AnalysisUtil.totalStatistics(allCAs);
			TotalResultWriter.saveTotalResult();
			
			for (CA ca : allCAs) {
				ca.percentage = ca.percentage + 1;
			}
			DetailResultWriter.addCAResult2(allCAs);
		}
	}
}
