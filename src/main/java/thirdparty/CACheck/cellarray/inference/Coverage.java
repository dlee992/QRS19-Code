package thirdparty.CACheck.cellarray.inference;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;

import thirdparty.CACheck.AMSheet;
import thirdparty.CACheck.CellArray;
import thirdparty.CACheck.cellarray.extract.CAResult;
import thirdparty.CACheck.cellarray.synthesis.SynthesisUtils;
import thirdparty.CACheck.formula.ParseFormula;
import thirdparty.CACheck.util.CellUtils;

public class Coverage {
	private AMSheet sheet = null;

	public Coverage(AMSheet sheet) {
		this.sheet = sheet;
	}

	public float computeCoverage(List<Object> pattern, CAResult car) {
		List<Cell> uncoveredCells = new ArrayList<Cell>();
		List<Cell> uncompatibleCells = new ArrayList<Cell>();

		Constraints cons = car.constraints;

		CellArray ca = car.cellArray;
		for (int i = 0; i < ca.size(); i++) {
			Cell cell = ca.getCell(sheet, i);
			if (cell != null) {
				if (SynthesisUtils.isError(sheet, ca, cell, pattern)) {
					uncoveredCells.add(cell);
				} else if (CellUtils.isFormula(cell)) {
					List<Object> cellPattern = ParseFormula.parse(ca, cell);
					if (cellPattern != null
							&& !SynthesisUtils.compatible(cons, cellPattern,
									pattern)) {
						uncompatibleCells.add(cell);
					}
				}
			}
		}

		float total = car.cellArray.size();
		float correct = total - uncoveredCells.size()
				- uncompatibleCells.size();
		return correct / total;
	}

	public float computeCECoverage(List<Object> pattern, CAResult car) {
		List<Cell> uncoveredCells = new ArrayList<Cell>();

		CellArray ca = car.cellArray;
		for (int i = 0; i < ca.size(); i++) {
			Cell cell = ca.getCell(sheet, i);
			if (cell != null) {
				if (SynthesisUtils.isError(sheet, ca, cell, pattern)) {
					uncoveredCells.add(cell);
				}
			}
		}

		float total = car.cellArray.size();
		float correct = total - uncoveredCells.size();
		return correct / total;
	}
}
