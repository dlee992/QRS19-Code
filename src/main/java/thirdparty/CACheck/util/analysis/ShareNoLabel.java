package thirdparty.CACheck.util.analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import thirdparty.CACheck.util.DetailResultWriter;
import thirdparty.CACheck.util.TotalResultWriter;

public class ShareNoLabel {

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
		String noLabelOutDir = AnalysisUtil.getDir(outDir,
				DirType.share_nolabel);
		if (!new File(noLabelOutDir).exists()) {
			new File(noLabelOutDir).mkdirs();
		}

		String[] compareTypes = { "common", "miss", "add" };
		for (String compareType : compareTypes) {
			File noLabelResFile = AnalysisUtil.getResultFile(outDir,
					DirType.share_nolabel, compareType);

			// Retrieve cell arrays
			for (String category : AnalysisUtil.getCategories(corpus)) {
				File labelDetailFile = AnalysisUtil.getDetailFile(outDir,
						category, DirType.share_org, null);
				File noLabelDetailFile = AnalysisUtil.getDetailFile(outDir,
						category, DirType.share_agg, null);
				File compDetailFile = AnalysisUtil.getDetailFile(outDir,
						category, DirType.share_nolabel, compareType);

				boolean resultInit = true;
				for (int sheetNum = 0; sheetNum < 8; sheetNum++) {
					// just read files
					DetailResultWriter.init(labelDetailFile, category, false);
					List<CA> labelCAs = DetailResultWriter.readSmells(sheetNum);
					Map<String, CA> labelHash = new HashMap<String, CA>();
					for (CA ca : labelCAs) {
						labelHash.put(AnalysisUtil.hashKey(ca), ca);
					}

					DetailResultWriter.init(noLabelDetailFile, category, false);
					List<CA> noLabelCAs = DetailResultWriter.readSmells(sheetNum);
					Map<String, CA> noLabelHash = new HashMap<String, CA>();
					for (CA ca : noLabelCAs) {
						noLabelHash.put(AnalysisUtil.hashKey(ca), ca);
					}

					List<CA> commonCAs = new ArrayList<CA>();
					for (CA ca : labelCAs) {
						if (noLabelHash.get(AnalysisUtil.hashKey(ca)) != null) {
							commonCAs.add(ca);
						}
					}

					List<CA> missCAs = new ArrayList<CA>();
					for (CA ca : noLabelCAs) {
						if (labelHash.get(AnalysisUtil.hashKey(ca)) == null) {
							missCAs.add(ca);
						}
					}

					List<CA> addCAs = new ArrayList<CA>();
					for (CA ca : labelCAs) {
						if (noLabelHash.get(AnalysisUtil.hashKey(ca)) == null) {
							addCAs.add(ca);
						}
					}

					DetailResultWriter.init(compDetailFile, category,
							resultInit);
					TotalResultWriter
							.init(noLabelResFile, category, resultInit);
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
