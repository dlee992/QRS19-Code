package ThirdParty.CACheck.util.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ThirdParty.CACheck.util.DetailResultWriter;
import ThirdParty.CACheck.util.TotalResultWriter;

public class ShareNoFix {

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
		String noFixOutDir = AnalysisUtil.getDir(outDir, DirType.share_nofix);
		if (!new File(noFixOutDir).exists()) {
			new File(noFixOutDir).mkdirs();
		}

		String[] compareTypes = { "common", "miss", "add" };
		for (String compareType : compareTypes) {
			File noFixResFile = AnalysisUtil.getResultFile(outDir,
					DirType.share_nofix, compareType);

			// Retrieve cell arrays
			for (String category : AnalysisUtil.getCategories(corpus)) {
				File fixDetailFile = AnalysisUtil.getDetailFile(outDir,
						category, DirType.share_org, null);
				File noFixDetailFile = AnalysisUtil.getDetailFile(outDir,
						category, DirType.share_agg, null);
				File compDetailFile = AnalysisUtil.getDetailFile(outDir,
						category, DirType.share_nofix, compareType);

				boolean resultInit = true;
				for (int sheetNum = 0; sheetNum < 8; sheetNum++) {
					// just read files
					DetailResultWriter.init(fixDetailFile, category, false);
					List<CA> fixCAs = DetailResultWriter.readSmells(sheetNum);
					Map<String, CA> fixHash = new HashMap<String, CA>();
					for (CA ca : fixCAs) {
						fixHash.put(AnalysisUtil.hashKey(ca), ca);
					}

					DetailResultWriter.init(noFixDetailFile, category, false);
					List<CA> noFixCAs = DetailResultWriter.readSmells(sheetNum);
					Map<String, CA> noFixHash = new HashMap<String, CA>();
					for (CA ca : noFixCAs) {
						noFixHash.put(AnalysisUtil.hashKey(ca), ca);
					}

					List<CA> commonCAs = new ArrayList<CA>();
					for (CA ca : fixCAs) {
						if (noFixHash.get(AnalysisUtil.hashKey(ca)) != null) {
							commonCAs.add(ca);
						}
					}

					List<CA> missCAs = new ArrayList<CA>();
					for (CA ca : noFixCAs) {
						if (fixHash.get(AnalysisUtil.hashKey(ca)) == null) {
							missCAs.add(ca);
						}
					}

					List<CA> addCAs = new ArrayList<CA>();
					for (CA ca : fixCAs) {
						if (noFixHash.get(AnalysisUtil.hashKey(ca)) == null) {
							addCAs.add(ca);
						}
					}

					DetailResultWriter.init(compDetailFile, category,
							resultInit);
					TotalResultWriter.init(noFixResFile, category, resultInit);
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
