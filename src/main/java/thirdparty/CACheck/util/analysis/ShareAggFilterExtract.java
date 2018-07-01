package thirdparty.CACheck.util.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import thirdparty.CACheck.util.DetailResultWriter;
import thirdparty.CACheck.util.TotalResultWriter;

public class ShareAggFilterExtract {

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
		String shareAggFilterOutDir = AnalysisUtil.getDir(outDir,
				DirType.share_agg_filter);
		if (!new File(shareAggFilterOutDir).exists()) {
			new File(shareAggFilterOutDir).mkdirs();
		}

		File shareAggFilterResFile = AnalysisUtil.getResultFile(outDir,
				DirType.share_agg_filter, null);

		// Retrieve cell arrays
		for (String category : AnalysisUtil.getCategories(corpus)) {
			File shareDetailFile = AnalysisUtil.getDetailFile(outDir, category,
					DirType.share, null);
			File shareAggDetailFile = AnalysisUtil.getDetailFile(outDir,
					category, DirType.share_agg, null);
			File shareAggFilterDetailFile = AnalysisUtil.getDetailFile(outDir,
					category, DirType.share_agg_filter, null);

			boolean resultInit = true;
			for (int sheetNum = 0; sheetNum < 8; sheetNum++) {
				// just read files
				DetailResultWriter.init(shareDetailFile, category, false);
				List<CA> shareCAs = DetailResultWriter.readSmells(sheetNum);
				Map<String, CA> shareHash = new HashMap<String, CA>();
				for (CA ca : shareCAs) {
					shareHash.put(AnalysisUtil.hashKey(ca), ca);
				}

				DetailResultWriter.init(shareAggDetailFile, category, false);
				List<CA> shareAggCAs = DetailResultWriter.readSmells(sheetNum);
				Map<String, CA> shareAggHash = new HashMap<String, CA>();
				for (CA ca : shareAggCAs) {
					shareAggHash.put(AnalysisUtil.hashKey(ca), ca);
				}

				List<CA> filterCAs = new ArrayList<CA>();
				for (CA ca : shareCAs) {
					if (shareAggHash.get(AnalysisUtil.hashKey(ca)) == null) {
						filterCAs.add(ca);
					}
				}

				DetailResultWriter.init(shareAggFilterDetailFile, category,
						resultInit);
				TotalResultWriter.init(shareAggFilterResFile, category,
						resultInit);
				if (resultInit) {
					resultInit = false;
				}

				AnalysisUtil.totalStatistics(filterCAs);
				DetailResultWriter.addCAResult2(filterCAs);
				TotalResultWriter.saveTotalResult();
			}
		}
	}
}
