package ThirdParty.CACheck.cellarray.extract;

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;

import ThirdParty.CACheck.AMSheet;
import ThirdParty.CACheck.CellArray;
import ThirdParty.CACheck.R1C1Cell;
import ThirdParty.CACheck.amcheck.AnalysisPattern;
import ThirdParty.CACheck.util.CellUtils;
import ThirdParty.CACheck.util.Utils;

public class SameCellArrayExtractor extends CellArrayExtractor {

	public SameCellArrayExtractor(AMSheet sheet, AnalysisPattern analysisPattern) {
		super(sheet, analysisPattern);
	}

	public int splitCA(CellArray ca, List<CellArray> cas) {

		for (int i = 0; i < ca.size(); i++) {
			Cell cell = ca.getCell(sheet, i);

			if (cell != null && CellUtils.isFormula(cell)) {
				List<R1C1Cell> paras = null;
				if (ca.isRowCA) {
					paras = Utils.extractParameters(ca.rowOrColumn, ca.start
							+ i, cell.getCellFormula());
				} else {
					paras = Utils.extractParameters(ca.start + i,
							ca.rowOrColumn, cell.getCellFormula());
				}
				if (!possibleMember(paras, ca.isRowCA)) {
					return ca.start + i;
				}
			}
		}

		cas.add(ca);
		return -1;
	}

	private boolean possibleMember(List<R1C1Cell> paras, boolean rowCA) {

		// no inputs, means just some computation on data.
		if (paras.size() == 0) {
			return false;
		}

		boolean possibleMember = true;
		for (R1C1Cell para : paras) {
			if (para.rowRelative || para.columnRelative) {
				if (rowCA) {
					if (para.column == para.curColumn) {
						continue;
					} else {
						possibleMember = false;
						break;
					}
				} else {
					if (para.row == para.curRow) {
						continue;
					} else {
						possibleMember = false;
						break;
					}
				}
			} else {
				possibleMember = false;
				break;
			}
		}

		return possibleMember;
	}
}
