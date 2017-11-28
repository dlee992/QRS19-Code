package featureExtraction.semllDetectionFeatureExtraction;

import utility.BasicUtility;
import utility.FormulaParsing;
import entity.R1C1Cell;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;

import java.util.ArrayList;
import java.util.List;

public class TokenFeature {
	
	private Workbook wb = null;
	private Cell cell = null;
	private BasicUtility bu = new BasicUtility();
	
	public TokenFeature(Cell cell, Workbook wb) {
		this.wb = wb;
		this.cell = cell;
	}

	public List<List<String>> getFeature() {
		List<List<String>> tokens = new ArrayList<List<String>>();
		
		//List<String> rangeTokenList = new ArrayList<String>();
		List<String> opTokenList = new ArrayList<String>();
		List<String> refTokenList = new ArrayList<String>();
		List<String> scalarList = new ArrayList<String>();
		
		CellReference cr = new CellReference (cell);
		int row = cr.getRow();
		int col = cr.getCol();
		
		try{
		Ptg[] fp = new FormulaParsing().getPtg(cell.getCellFormula(), wb, FormulaType.forInt(2), wb.getSheetIndex(cell.getSheet()));
		R1C1Cell preR1C1Cell;
		R1C1Cell curR1C1Cell;
		
		for (Ptg element: fp){
				String elementStr = element.toString();

					 if (element instanceof RefPtg ){
						 refTokenList.add(bu.extractCell(row, col, element.toFormulaString()).toString());
						 }
					 else if (element instanceof AreaPtg){
							String[] cell = element.toFormulaString().split(":");
							preR1C1Cell = bu.extractCell(row, col, cell[0]);
							curR1C1Cell = bu.extractCell(row, col, cell[1]);
							refTokenList.add(preR1C1Cell +":"+ curR1C1Cell);

						}
					else if (element instanceof OperationPtg || element instanceof AttrPtg){
						opTokenList.add(elementStr.substring(elementStr.lastIndexOf(".")+1, elementStr.length()));
					}
					else if (element instanceof ScalarConstantPtg){
						scalarList.add("ScalarConstantPtg");
					}
					 
			}

		tokens.add(opTokenList);
		tokens.add(refTokenList);
		tokens.add(scalarList);
		
		}
		catch(FormulaParseException e){
			e.printStackTrace();
		}
		return tokens;
	}

}
