package ThirdParty.CACheck.util.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ThirdParty.CACheck.util.DetailResultWriter;
import ThirdParty.CACheck.util.TotalResultWriter;

public class CombineAgg {

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
		String shareAggOutDir = AnalysisUtil.getDir(outDir, DirType.share_agg);
		if (!new File(shareAggOutDir).exists()) {
			new File(shareAggOutDir).mkdirs();
		}

		File shareAggResFile = AnalysisUtil.getResultFile(outDir,
				DirType.share_agg, null);

		// Retrieve cell arrays
		for (String category : AnalysisUtil.getCategories(corpus)) {

			File shareAggDetailFile = AnalysisUtil.getDetailFile(outDir,
					category, DirType.share_agg, null);

			boolean resultInit = true;
			for (int sheetNum = 1; sheetNum < 8; sheetNum++) {
				// read same cas
				File sameDetailFile = AnalysisUtil.getDetailFile(outDir,
						category, DirType.share_same, null);
				DetailResultWriter.init(sameDetailFile, category, false);
				List<CA> sameCAs = DetailResultWriter.readSmells(sheetNum, 500);

				// read diff cas
				File diffDetailFile = AnalysisUtil.getDetailFile(outDir,
						category, DirType.share_diff, null);
				DetailResultWriter.init(diffDetailFile, category, false);
				List<CA> diffCAs = DetailResultWriter.readSmells(sheetNum, 500);

				List<CA> all = new ArrayList<CA>();
				all.addAll(sameCAs);
				all.addAll(diffCAs);
				
				DetailResultWriter.init(shareAggDetailFile, category,
						resultInit);
				TotalResultWriter.init(shareAggResFile, category, resultInit);
				if (resultInit) {
					resultInit = false;
				}

				AnalysisUtil.totalStatistics(all);
				DetailResultWriter.addCAResult2(all);
				TotalResultWriter.saveTotalResult();
			}
		}
	}
}
