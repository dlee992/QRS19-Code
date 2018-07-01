package thirdparty.CACheck.util.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import thirdparty.CACheck.util.DetailResultWriter;
import thirdparty.CACheck.util.TotalResultWriter;

public class ShareSameExtract {

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
		String shareSameOutDir = AnalysisUtil
				.getDir(outDir, DirType.share_same);
		if (!new File(shareSameOutDir).exists()) {
			new File(shareSameOutDir).mkdirs();
		}

		File shareSameResFile = AnalysisUtil.getResultFile(outDir,
				DirType.share_same, null);

		// Retrieve cell arrays
		for (String category : AnalysisUtil.getCategories(corpus)) {
			File shareDetailFile = AnalysisUtil.getDetailFile(outDir, category,
					DirType.share_agg, null);
			File shareSameDetailFile = AnalysisUtil.getDetailFile(outDir,
					category, DirType.share_same, null);

			boolean resultInit = true;
			for (int sheetNum = 0; sheetNum < 8; sheetNum++) {
				// just read files
				DetailResultWriter.init(shareDetailFile, category, false);
				List<CA> shareCAs = DetailResultWriter.readSmells(sheetNum);

				List<CA> shareSameCAs = new ArrayList<CA>();
				for (CA ca : shareCAs) {
					if (ca.isSameRowOrColumn) {
						shareSameCAs.add(ca);
					}
				}

				DetailResultWriter.init(shareSameDetailFile, category,
						resultInit);
				TotalResultWriter.init(shareSameResFile, category, resultInit);
				if (resultInit) {
					resultInit = false;
				}

				AnalysisUtil.totalStatistics(shareSameCAs);
				DetailResultWriter.addCAResult2(shareSameCAs);
				TotalResultWriter.saveTotalResult();
			}
		}
	}
}
