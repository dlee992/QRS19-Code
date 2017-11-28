package ThirdParty.CACheck.amcheck;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ThirdParty.CACheck.cellarray.extract.CAResult;
import ThirdParty.CACheck.util.DetailResultWriter;
import ThirdParty.CACheck.util.Utils;
import ThirdParty.CACheck.util.analysis.CA;

public class ValidateCA {
	private static List<CA> trueCAs = new ArrayList<CA>();

	public static void load() {
		String dir = Utils.exprDir();
		String filename = dir + "tools/euses-true.xls";
		File file = new File(filename);
		if (file.exists()) {
			try {
				DetailResultWriter.init(file, "cs101", false);
				trueCAs = DetailResultWriter.readSmells(0);
				System.err.println(trueCAs.size());
				DetailResultWriter.detailFile = null;
				DetailResultWriter.category = null;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean isTrue(CAResult car) {
		for (CA ca : trueCAs) {
			if (ca.falseRepair != 1) {
				if (car.excel.equals(ca.excel)
						&& car.worksheet.equals(ca.worksheet)
						&& car.cellArray.toString().equals(ca.cellArray)) {
					return true;
				}
			}
		}
		return false;
	}

	public static void main(String[] args) {
		load();
	}
}
