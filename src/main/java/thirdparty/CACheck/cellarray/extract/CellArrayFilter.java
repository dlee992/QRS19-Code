package thirdparty.CACheck.cellarray.extract;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;

import thirdparty.CACheck.AMSheet;
import thirdparty.CACheck.CellArray;
import thirdparty.CACheck.R1C1Cell;
import thirdparty.CACheck.amcheck.AnalysisPattern;
import thirdparty.CACheck.cellarray.inference.FormulaPattern;
import thirdparty.CACheck.cellarray.synthesis.SynthesisUtils;
import thirdparty.CACheck.util.CellUtils;
import thirdparty.CACheck.util.Log;
import thirdparty.synthesis.component.Component;
import thirdparty.synthesis.component.DivComponent;
import thirdparty.synthesis.component.MultComponent;

public class CellArrayFilter {

	// if a cell array is conflict with a correct one, remove it!.
	public static List<CAResult> preProcessFilter(AMSheet sheet,
			List<CAResult> allCARs, List<CAResult> correctCARs, AnalysisPattern analysisPattern) {

		// Don't filter.
		if (analysisPattern.curHeuristic == AnalysisPattern.NO) {
			return allCARs;
		}

		// Here, I'm not sure it should work. Some ambiguous CAs should be
		// correct, too.
		// But, anyway, in order to perform well, do it.
		/*
		 * if (AnalysisPattern.curHeuristic != AnalysisPattern.NO) { return
		 * allCARs; }
		 */

		List<CAResult> ret = new ArrayList<CAResult>();
		ret.addAll(allCARs);

		// Only refer changed, we think the cell arrays are correct, even
		// inconsistent.
		// For performance.
		for (CAResult car : allCARs) {
			if (car.isInconsistent && !car.isMissing
					&& car.constraints.getFormulaPatterns().size() == 1) {
				// only one specification, and inconsistent, so it is
				// referchanged.
				correctCARs.add(car);
			}
		}

		// Make sure correct cells in original correct CAs are selected
		// Here, I want to improve performance, and remove wrong cell arrays
		// early.
		// TODO refine this

		for (CAResult corCar : allCARs) {
			if (corCar.isAmbiguous) {
				continue;
			}
			if (corCar.isSameRowOrColumn) {
				for (int i = 0; i < ret.size(); i++) {
					CAResult car = ret.get(i);
					if (car.isInconsistent
							&& CAOverlap.overlap(corCar.cellArray,
									car.cellArray)
							&& corCar.cellArray.isRowCA != car.cellArray.isRowCA) {
						ret.remove(i);
						i--;
					}
				}
			}
		}

		// If a smelly cell array's all cells can be found in some correct cell
		// arrays, ignore it.
		for (int i = 0; i < ret.size(); i++) {
			CAResult car = ret.get(i);
			if (car.isAmbiguous) {
				// Some correct cell arrays are identifed as wrong, because SAT
				// for div and mult
				boolean specialComp = false;
				for (Component comp : car.constraints.getComponents()) {
					if (comp instanceof DivComponent
							|| comp instanceof MultComponent) {
						specialComp = true;
						break;
					}
				}
				if (specialComp) {
					Set<R1C1Cell> allInputs = car.constraints.getInputs();
					boolean sameInputs = true;
					for (FormulaPattern fp : car.constraints
							.getFormulaPatterns()) {
						if (!FormulaPattern.getInputs(fp.pattern).containsAll(
								allInputs)) {
							sameInputs = false;
							break;
						}
					}
					if (sameInputs) {
						// possible correct CA.
						continue;
					}
				}

				boolean covered = true;
				for (int j = 0; j < car.cellArray.size(); j++) {
					int row = 0;
					int column = 0;
					if (car.cellArray.isRowCA) {
						row = car.cellArray.rowOrColumn;
						column = car.cellArray.start + j;

						Cell cell = car.cellArray.getCell(sheet, j);
						if (!CellUtils.isFormula(cell)) {
							// continue;
						}

						boolean hit = false;
						for (CAResult corCAR : correctCARs) {
							if (!corCAR.cellArray.isRowCA) {
								if (corCAR.cellArray.rowOrColumn == column
										&& corCAR.cellArray.start <= row
										&& corCAR.cellArray.end >= row) {
									hit = true;
									break;
								}
							}
						}
						if (!hit) {
							covered = false;
							break;
						}
					} else {
						row = car.cellArray.start + j;
						column = car.cellArray.rowOrColumn;

						Cell cell = car.cellArray.getCell(sheet, j);
						if (!CellUtils.isFormula(cell)) {
							// continue;
						}

						boolean hit = false;
						for (CAResult corCAR : correctCARs) {
							if (corCAR.cellArray.isRowCA) {
								if (corCAR.cellArray.rowOrColumn == row
										&& corCAR.cellArray.start <= column
										&& corCAR.cellArray.end >= column) {
									hit = true;
									break;
								}
							}
						}
						if (!hit) {
							covered = false;
							break;
						}
					}
				}

				if (covered) {
					ret.remove(i);
					i--;
				}
			}
		}

		for (int i = 0; i < ret.size(); i++) {
			CAResult car = ret.get(i);
			if (car.isAmbiguous) {
				List<CAResult> ovs = CAOverlap.getOverlapCA(car, correctCARs);
				for (int k = 0; k < ovs.size(); k++) {
					CAResult tmp = ovs.get(k);
					if (car.cellArray.isRowCA == tmp.cellArray.isRowCA
							|| tmp.pattern == null) {
						ovs.remove(k);
						k--;
					}
				}
				if (ovs.size() > 1) {
					CAResult car1 = ovs.get(0);
					for (int k = 1; k < ovs.size(); k++) {
						CAResult car2 = ovs.get(k);
						if (!SynthesisUtils.semanticEqual(car1.pattern,
								car2.pattern)) {
							ret.remove(i);
							i--;
							break;
						}
					}
				}

				if (ret.contains(car)) {
					for (CAResult car2 : ovs) {
						if (car2.cellArray.size() > 100) {
							ret.remove(i);
							i--;
							break;
						}
					}
				}
			}
		}

		return ret;
	}

	// (1) one cell only belong to one cell array.
	// (2) most cell's values should be correct.
	// (3) Find most cell arrays
	public static List<CAResult> postProcessFilter(List<CAResult> allCARs,
			List<CellArray> correctCAs, AnalysisPattern analysisPattern) {
		return PostFilter.postProcessFilter(allCARs, correctCAs, analysisPattern);
	}

	public static void printFilterCellArrays(List<CAResult> before,
			List<CAResult> after, BufferedWriter writer) {
		//TODO：
		/**
		Log.log("Size before: " + before.size(), writer);
		Log.logNewLine(writer);
		Log.log("Size after: " + after.size(), writer);
		Log.logNewLine(writer);

		Log.log("***** Filtered out cell arrays: *****", writer);
		Log.logNewLine(writer);
		for (CAResult car : before) {
			if (!after.contains(car)) {
				Log.log(car, writer);
			}
		}
		Log.log("***** End *****", writer);
		Log.logNewLine(writer);
		Log.logNewLine(writer);

		Log.log("Cell arrays before filter:", writer);
		Log.logNewLine(writer);
		Log.log(before, writer);
		 **/
		Log.log("Cell arrays after filter:", writer);
		Log.logNewLine(writer);
		Log.log(after, writer);
	}
}
