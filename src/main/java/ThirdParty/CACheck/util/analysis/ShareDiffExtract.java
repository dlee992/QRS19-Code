package ThirdParty.CACheck.util.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ThirdParty.CACheck.util.DetailResultWriter;
import ThirdParty.CACheck.util.TotalResultWriter;

public class ShareDiffExtract {

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
		String shareDiffOutDir = AnalysisUtil
				.getDir(outDir, DirType.share_diff);
		if (!new File(shareDiffOutDir).exists()) {
			new File(shareDiffOutDir).mkdirs();
		}

		File shareDiffResFile = AnalysisUtil.getResultFile(outDir,
				DirType.share_diff, null);

		// Retrieve cell arrays
		for (String category : AnalysisUtil.getCategories(corpus)) {
			File shareDetailFile = AnalysisUtil.getDetailFile(outDir, category,
					DirType.share_agg, null);
			File shareDiffDetailFile = AnalysisUtil.getDetailFile(outDir,
					category, DirType.share_diff, null);

			boolean resultInit = true;
			for (int sheetNum = 0; sheetNum < 8; sheetNum++) {
				// just read files
				DetailResultWriter.init(shareDetailFile, category, false);
				List<CA> shareCAs = DetailResultWriter.readSmells(sheetNum);

				List<CA> shareDiffCAs = new ArrayList<CA>();
				for (CA ca : shareCAs) {
					if (!ca.isSameRowOrColumn) {
						shareDiffCAs.add(ca);
					}
				}

				DetailResultWriter.init(shareDiffDetailFile, category,
						resultInit);
				TotalResultWriter.init(shareDiffResFile, category, resultInit);
				if (resultInit) {
					resultInit = false;
				}

				AnalysisUtil.totalStatistics(shareDiffCAs);
				DetailResultWriter.addCAResult2(shareDiffCAs);
				TotalResultWriter.saveTotalResult();
			}
		}
	}
}
