package extraction.weakFeatureExtraction;

import utility.FormulaParsing;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.AreaPtg;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

import java.util.ArrayList;
import java.util.List;

public class OtherExtraction {

	private Cell cell = null;
	
	public int columnOrRowBasedCell(Cell clusterCell) {
		int colFlag = -1;
		int rowFlag = -1;
		
		this.cell = clusterCell;
		
		List<CellReference> precedents = new ArrayList<CellReference>();
		extractPrecedentCells(precedents, true, true);
		if (!precedents.isEmpty()){
			for (CellReference cdTemp : precedents){
					if (rowFlag == -1){
						if (cdTemp.getCol() == clusterCell.getColumnIndex()){
							colFlag = 0;
						}
						else {colFlag = -1;}
						}
					if (colFlag == -1){
						if(cdTemp.getRow() == clusterCell.getRowIndex()){
							rowFlag = 0;
						}
						else{
							rowFlag = -1;
						}
					}
			}
				
			if (rowFlag == colFlag){
				return -1;
			}
			else if (rowFlag == 0){
				return 0;
			}
			else {
				return 1;
			}
		}
		return -1;
	}

	private void extractPrecedentCells(List<CellReference> dependency, Boolean areaPtgExtract, Boolean refPtgExtract) {
		String formula = cell.getCellFormula();
		//Only within worksheet
		if (!formula.contains("!")){
			try{
				Workbook wb = cell.getSheet().getWorkbook();
				Ptg[] fp = new FormulaParsing().getPtg(formula, wb, FormulaType.forInt(2), wb.getSheetIndex(cell.getSheet()));

				for (Ptg aFp : fp) {
					if (aFp.toString().contains("ptg.AreaPtg") && areaPtgExtract)
						extractCellsInAreaPtg(dependency, aFp);
					if (aFp.toString().contains("ptg.RefPtg") && refPtgExtract)
						extractCellInRefPtg(dependency, aFp);
				}
			}
			catch(FormulaParseException e){
				e.printStackTrace();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
		
	private void extractCellsInAreaPtg(List<CellReference> dependency, Ptg ptg){
			
		AreaPtg ar = new AreaPtg(ptg.toFormulaString());
		int firstRow = ar.getFirstRow();
		int lastRow = ar.getLastRow();
		int firstColumn = ar.getFirstColumn();
		int lastCol = ar.getLastColumn();
					
					
		for (int ii = firstRow; ii < lastRow + 1; ii++) {
			Row r = cell.getSheet().getRow(ii);
			if (r!=null){
				for (int j = firstColumn; j < lastCol + 1; j++) {
					Cell cell = r.getCell(j);
					if (cell !=null){
						CellReference cellWithin = new CellReference(r.getCell(j));
						//CellReference cf = new CellReference (cellWithin.getRowIndex(),cellWithin.getColumnIndex());
						dependency.add(cellWithin);
					}
				}
			}
		}
	}
			
	private void extractCellInRefPtg(List<CellReference> dependency, Ptg ptg){
		CellReference rf = new CellReference(ptg.toFormulaString());
		dependency.add(rf);
	}
}
