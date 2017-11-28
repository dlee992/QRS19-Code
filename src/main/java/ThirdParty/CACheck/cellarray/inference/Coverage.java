package ThirdParty.CACheck.cellarray.inference;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;

import ThirdParty.CACheck.AMSheet;
import ThirdParty.CACheck.CellArray;
import ThirdParty.CACheck.cellarray.extract.CAResult;
import ThirdParty.CACheck.cellarray.synthesis.SynthesisUtils;
import ThirdParty.CACheck.formula.ParseFormula;
import ThirdParty.CACheck.util.CellUtils;

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
