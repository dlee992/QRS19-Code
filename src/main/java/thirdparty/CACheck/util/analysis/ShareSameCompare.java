package thirdparty.CACheck.util.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import thirdparty.CACheck.util.DetailResultWriter;
import thirdparty.CACheck.util.TotalResultWriter;

public class ShareSameCompare {

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
		String compareOutDir = AnalysisUtil.getDir(outDir, DirType.share_same_compare);
		if (!new File(compareOutDir).exists()) {
			new File(compareOutDir).mkdirs();
		}

		String[] compareTypes = { "common", "miss", "add" };
		for (String compareType : compareTypes) {
			File compareResFile = AnalysisUtil.getResultFile(outDir, DirType.share_same_compare,
					compareType);

			// Retrieve cell arrays
			for (String category : AnalysisUtil.getCategories(corpus)) {
				File sameDetailFile = AnalysisUtil.getDetailFile(outDir, category,
						DirType.same, null);
				File shareDetailFile = AnalysisUtil.getDetailFile(outDir, category,
						DirType.share, null);
				File compareDetailFile = AnalysisUtil.getDetailFile(outDir, category,
						DirType.share_same_compare, compareType);

				boolean resultInit = true;
				for (int sheetNum = 0; sheetNum < 8; sheetNum++) {
					// just read files
					DetailResultWriter.init(sameDetailFile, category, false);
					List<CA> sameCAs = DetailResultWriter.readSmells(sheetNum);
					Map<String, CA> sameHash = new HashMap<String, CA>();
					for (CA ca : sameCAs) {
						sameHash.put(AnalysisUtil.hashKey(ca), ca);
					}

					DetailResultWriter.init(shareDetailFile, category, false);
					List<CA> shareCAs = DetailResultWriter.readSmells(sheetNum);
					Map<String, CA> shareHash = new HashMap<String, CA>();
					for (CA ca : shareCAs) {
						shareHash.put(AnalysisUtil.hashKey(ca), ca);
					}

					List<CA> commonCAs = new ArrayList<CA>();
					for (CA ca : sameCAs) {
						if (shareHash.get(AnalysisUtil.hashKey(ca)) != null) {
							commonCAs.add(ca);
						}
					}

					List<CA> missCAs = new ArrayList<CA>();
					for (CA ca : sameCAs) {
						if (shareHash.get(AnalysisUtil.hashKey(ca)) == null) {
							missCAs.add(ca);
						}
					}

					List<CA> addCAs = new ArrayList<CA>();
					for (CA ca : shareCAs) {
						if (ca.isSameRowOrColumn
								&& sameHash.get(AnalysisUtil.hashKey(ca)) == null) {
							addCAs.add(ca);
						}
					}

					DetailResultWriter.init(compareDetailFile, category,
							resultInit);
					TotalResultWriter
							.init(compareResFile, category, resultInit);
					if (resultInit) {
						resultInit = false;
					}

					if (compareType.equals("common")) {
						AnalysisUtil.totalStatistics(commonCAs);
						DetailResultWriter.addCAResult2(commonCAs);
						TotalResultWriter.saveTotalResult();
					}
					if (compareType.equals("miss")) {
						AnalysisUtil.totalStatistics(missCAs);
						DetailResultWriter.addCAResult2(missCAs);
						TotalResultWriter.saveTotalResult();
					}
					if (compareType.equals("add")) {
						AnalysisUtil.totalStatistics(addCAs);
						DetailResultWriter.addCAResult2(addCAs);
						TotalResultWriter.saveTotalResult();
					}
				}
			}
		}
	}
}
