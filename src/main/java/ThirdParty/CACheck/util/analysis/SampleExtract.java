package ThirdParty.CACheck.util.analysis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import ThirdParty.CACheck.util.DetailResultWriter;
import ThirdParty.CACheck.util.TotalResultWriter;

public class SampleExtract {

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

		List<CA> sampleCAs = new ArrayList<CA>();
		for (int sheetNum = 1; sheetNum < 8; sheetNum++) {
			// Retrieve cell arrays for each category.
			List<CA> allCAs = new ArrayList<CA>();
			for (String category : AnalysisUtil.getCategories(corpus)) {
				File shareDetailFile = AnalysisUtil.getDetailFile(outDir,
						category, DirType.share_agg, null);

				// just read files
				DetailResultWriter.init(shareDetailFile, category, false);
				List<CA> shareCAs = DetailResultWriter.readSmells(sheetNum);

				allCAs.addAll(shareCAs);
			}

			if (allCAs.size() < 100) {
				sampleCAs.addAll(allCAs);
				continue;
			}
			int size = allCAs.size();
			double step = size / 100.0f;
			int num = 0;
			for (double i = 0; i < size && num < 100; i = i + step) {
				sampleCAs.add(allCAs.get((int) i));
				num++;
			}
		}

		// Initilize
		String shareSampleOutDir = AnalysisUtil.getDir(outDir,
				DirType.share_sample);
		if (!new File(shareSampleOutDir).exists()) {
			new File(shareSampleOutDir).mkdirs();
		}

		File shareSampleResFile = AnalysisUtil.getResultFile(outDir,
				DirType.share_sample, null);

		String tmpCategory = AnalysisUtil.getCategories(corpus)[0];
		File shareSampleDetailFile = AnalysisUtil.getDetailFile(outDir,
				tmpCategory, DirType.share_sample, null);

		DetailResultWriter.init(shareSampleDetailFile, tmpCategory, true);
		TotalResultWriter.init(shareSampleResFile, tmpCategory, true);

		AnalysisUtil.totalStatistics(sampleCAs);
		DetailResultWriter.addCAResult2(sampleCAs);
		TotalResultWriter.saveTotalResult();

		copyFiles(outDir, sampleCAs);
	}

	private static void copyFiles(String outDir, List<CA> sampleCAs) {
		String sampleInDir = AnalysisUtil.getDir(outDir, DirType.share_agg);
		String sampleOutDir = AnalysisUtil.getDir(outDir, DirType.share_sample);

		for (CA ca : sampleCAs) {
			String category = ca.category;
			String iDir = sampleInDir + category + "/";
			String oDir = sampleOutDir + category + "/";
			if (!new File(oDir).exists()) {
				new File(oDir).mkdirs();
			}

			String inFile = iDir + ca.excel;
			String outFile = oDir + ca.excel;

			if (new File(inFile).exists() && !new File(outFile).exists()) {
				try {
					Workbook workbook = WorkbookFactory
							.create(new FileInputStream(inFile));
					workbook.write(new FileOutputStream(outFile));
					workbook.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
