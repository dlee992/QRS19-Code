package featureExtraction.weakFeatureExtraction;

import org.apache.poi.ss.usermodel.*;
import utility.FormulaParsing;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.util.CellReference;

import java.util.ArrayList;
import java.util.List;

public class AllianceExtraction {

	private Sheet sheet;
    private List<CellReference> dependency = null;
	
	public AllianceExtraction(Sheet sheet) {
		this.sheet = sheet;
	}

	public List<Alliance> allianceExtraction() {
		Alliance alliance;
		List<Alliance> allianceList = new ArrayList<Alliance>();
		
		for (int i = 0; i< sheet.getLastRowNum();i++){
			Row r = sheet.getRow(i);
			if (r!=null){
			for (int j = 0; j< r.getLastCellNum();j++){
				Cell cell = r.getCell(j);
				try {
					if (cell != null && !cell.toString().contains("#")) {
						if (cell.getCellTypeEnum() == CellType.FORMULA && cell.getCellFormula().contains(":")) {
							try {
								Workbook wb = sheet.getWorkbook();
								Ptg[] fp = new FormulaParsing().getPtg(cell.getCellFormula(), wb, FormulaType.forInt(2), wb.getSheetIndex(sheet));

								dependency = new ArrayList<CellReference>();

								for (Ptg aFp : fp) {
									if (aFp.toString().contains("ptg.AreaPtg")) {
										alliance = new Alliance(aFp.toFormulaString());
										if (!allianceList.contains(alliance)) {
											extractCellsInAreaPtg(aFp, cell);
											alliance.setPrecedents(dependency);
											allianceList.add(alliance);
										}

									}
								}
							} catch (FormulaParseException e) {
								e.printStackTrace();
							}
						}
					}
				}
				catch (NullPointerException null_pointer_e) {
					System.err.println(null_pointer_e.toString() + ": " + null_pointer_e.getMessage() + ": " +
					sheet.getSheetName() + ": " + cell.getAddress());
					null_pointer_e.printStackTrace();
				}
			}
		}
		
		}
		return allianceList;
	}

	private void extractCellsInAreaPtg(Ptg ptg, Cell curCell) {
		AreaPtg ar = new AreaPtg(ptg.toFormulaString());
		int firstRow = ar.getFirstRow();
		int lastRow = ar.getLastRow();
		int firtCol = ar.getFirstColumn();
		int lastCol = ar.getLastColumn();
		
		for (int ii = firstRow; ii < lastRow + 1; ii++) {
			Row r = curCell.getSheet().getRow(ii);
			if (r!=null){
				for (int j = firtCol; j < lastCol + 1; j++) {
					Cell cell = r.getCell(j);
					if (cell !=null){
						CellReference cellWithin = new CellReference(r.getCell(j));
						dependency.add(cellWithin);
					}
				}
			}
		}
	}

	public List<Alliance> getFeature(List<Alliance> alliances, Cell cell) {
		List<Alliance> cellAlliance = new ArrayList<Alliance>();
		
		CellReference cellRef = new CellReference (cell);
		for (Alliance alliance: alliances){
			if (checkDependency(alliance.getPrecedents(), cellRef)){
				cellAlliance.add(alliance);
			}
				
		}
	return cellAlliance;
	}

	private boolean checkDependency(List<CellReference> precedents, CellReference cellToCheck) {
		Boolean checkIfDepend = false;
		for (CellReference dependentCell: precedents){
			if (dependentCell.equals(cellToCheck)){
				checkIfDepend = true;
				break;
			}
		}
		return checkIfDepend;
	}

}
