package ThirdParty.CACheck.cellarray.extract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;

import ThirdParty.CACheck.AMSheet;
import ThirdParty.CACheck.CellArray;
import ThirdParty.CACheck.R1C1Cell;
import ThirdParty.CACheck.util.CellUtils;
import ThirdParty.CACheck.util.Utils;

public class DependenceConstantExtractor {

	public static List<Dependence> constructDependence(AMSheet sheet,
			CellArray ca) {

		List<Dependence> deps = new ArrayList<Dependence>();
		for (int i = 0; i < ca.size(); i++) {
			Cell cell = ca.getCell(sheet, i);

			if (cell != null && CellUtils.isFormula(cell)) {
				if (ca.isRowCA) {
					List<R1C1Cell> paras = Utils
							.extractParameters(ca.rowOrColumn, ca.start + i,
									cell.getCellFormula());
					deps.add(new Dependence(cell, paras));
				} else {
					List<R1C1Cell> paras = Utils
							.extractParameters(ca.start + i, ca.rowOrColumn,
									cell.getCellFormula());
					deps.add(new Dependence(cell, paras));
				}
			}
		}

		return deps;
	}

	public static List<R1C1Cell> extractConstants(List<Dependence> deps) {

		// identify constants
		List<R1C1Cell> constants = new ArrayList<R1C1Cell>();
		for (Dependence dep : deps) {
			for (R1C1Cell para : dep.dependedCells) {
				if (!para.rowRelative && !para.columnRelative) {
					if (!constants.contains(para)) {
						constants.add(para);
					}
				}
			}
		}

		return constants;
	}

	public static List<R1C1Cell> extractPossibleConstants(
			List<Dependence> deps, float theshold) {

		// identify constants
		List<R1C1Cell> constants = extractConstants(deps);

		// temporarily return the cell when all have it or 80% of cells have
		// it.
		Map<String, Integer> cell2Num = new HashMap<String, Integer>();
		Map<String, R1C1Cell> cell2R1C1 = new HashMap<String, R1C1Cell>();

		for (Dependence dep : deps) {
			for (R1C1Cell cell : dep.dependedCells) {
				// constants has it.
				if (constants.contains(cell)) {
					continue;
				}

				Integer num = cell2Num.get(cell.getRelativeA1Cell());
				if (num == null) {
					cell2Num.put(cell.getRelativeA1Cell(), 1);
					cell2R1C1.put(cell.getRelativeA1Cell(), cell);
				} else {
					cell2Num.put(cell.getRelativeA1Cell(), num + 1);
				}
			}
		}

		// TODO here, we may be wrong. But don't worry. First do it.
		List<R1C1Cell> posCons = new ArrayList<R1C1Cell>();
		double totalNum = deps.size();
		double minPercent = theshold - 0.001;

		if (deps.size() > 1) {
			for (String key : cell2Num.keySet()) {
				double percent = cell2Num.get(key) / totalNum;
				if (percent >= minPercent) {
					posCons.add(cell2R1C1.get(key));
				}
			}
		}

		return posCons;
	}

}
