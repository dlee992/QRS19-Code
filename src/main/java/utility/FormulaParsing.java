package utility;

import entity.CellLocation;
import entity.R1C1Cell;
import featureExtraction.weakFeatureExtraction.CellArray;
import org.apache.poi.hssf.usermodel.HSSFEvaluationWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.*;
import org.apache.poi.ss.formula.ptg.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


public class FormulaParsing {

	public Ptg[] getPtg(String formula, Workbook wb, FormulaType type, int sheetIndex) {
        FormulaParsingWorkbook fpWorkbook = null;
        if (wb.getSpreadsheetVersion().name().equals("EXCEL97"))
            fpWorkbook = HSSFEvaluationWorkbook.create((HSSFWorkbook) wb);
        else if (wb.getSpreadsheetVersion().name().equals("EXCEL2007"))
            fpWorkbook = XSSFEvaluationWorkbook.create((XSSFWorkbook) wb);

        Ptg[] ptgList = null;

        try {
			ptgList = FormulaParser.parse(formula, fpWorkbook, type, sheetIndex);
		}
		catch (NullPointerException npe) {
        	System.out.println("NullPointerException");
		}
		catch (FormulaParseException fpe) {
        	System.out.println("FormulaParseException");
		}


        return ptgList;
    }

	public List<CellLocation> getFormulaDependencies(Sheet sheet, int col, int row) {
		Workbook wb         = sheet.getWorkbook();
        Cell     cell       = sheet.getRow(row).getCell(col);
        String   formulaStr = cell.getCellFormula();

        EvaluationWorkbook ew = null;
        if (wb.getSpreadsheetVersion().name().equals("EXCEL97"))
            ew = HSSFEvaluationWorkbook.create((HSSFWorkbook) wb);
        else if (wb.getSpreadsheetVersion().name().equals("EXCEL2007"))
            ew = XSSFEvaluationWorkbook.create((XSSFWorkbook) wb);

		Ptg[]    tokens     = getPtg(formulaStr, wb, FormulaType.forInt(2), wb.getSheetIndex(sheet));

		ArrayList<CellLocation> res = new ArrayList<CellLocation>();

		for (Ptg token : tokens) {
			if (token.getClass() == RefPtg.class) {
				RefPtg p = (RefPtg) token;
				CellLocation c = new CellLocation(sheet.getSheetName(), p.getRow(), p.getColumn());
				if (!res.contains(c))
					res.add(c);

				//System.out.println("RefPtg found -> " + tokens[i]);
			}
			if (token.getClass() == Ref3DPtg.class) {
				Ref3DPtg p = (Ref3DPtg) token;

				//EvaluationWorkbook ew = EvaluationWorkbook.create(sheet.getWorkbook());
				assert ew != null;
				int sheetIndex = ew.convertFromExternSheetIndex(p.getExternSheetIndex());
				CellLocation c = new CellLocation(sheet.getWorkbook().getSheetName(sheetIndex), p.getRow(), p.getColumn());
				if (!res.contains(c)) {
					res.add(c);
					//System.out.println("Ref3DPtg found -> " + tokens[i]);
				}
			}
			if (token.getClass() == AreaPtg.class) {
				AreaPtg p = (AreaPtg) token;
				int col1 = p.getFirstColumn();
				int row1 = p.getFirstRow();
				int col2 = p.getLastColumn();
				int row2 = p.getLastRow();

				//TODO: do not unfold cell ranges if they are just data cells
//                boolean allData = true;
//                for (int x = col1; x <= col2; x++) {
//                    for (int y = row1; y <= row2; y++) {
//                        Cell refCell = sheet.getRow(y).getCell(x);
//                        if (refCell.getCellType() != 0) // not a data cell
//                            allData = false;
//                    }
//                }
//
//                if (allData) {
//                    String cellArea ="";
//                    CellLocation c= new CellLocation(sheet.getSheetName(), cellArea);
//                    res.add(c);
//                }
//                else
                    for (int x = col1; x <= col2; x++) {
                        for (int y = row1; y <= row2; y++) {
                            CellLocation c = new CellLocation(sheet.getSheetName(), y, x);
                            res.add(c);
                        }
                    }
				//System.out.println("AreaPtg found -> " + tokens[i]);
			}
			if (token.getClass() == Area3DPtg.class) {
				Area3DPtg p = (Area3DPtg) token;
				//EvaluationWorkbook ew = EvaluationWorkbook.create(sheet.getWorkbook());
				assert ew != null;
				int sheetIndex = ew.convertFromExternSheetIndex(p.getExternSheetIndex());
				int col1 = p.getFirstColumn();
				int row1 = p.getFirstRow();
				int col2 = p.getLastColumn();
				int row2 = p.getLastRow();
				for (int x = col1; x <= col2; x++) {
					for (int y = row1; y <= row2; y++) {
						CellLocation c = new CellLocation(sheet.getWorkbook().getSheetName(sheetIndex), y, x);
						res.add(c);
					}
				}
				//System.out.println("Ref3DPtg found -> " + tokens[i]);
			}
		}
		
		return res;
	}

	public Set<R1C1Cell> generateInputs(Sheet sheet, CellArray ca) {
        Set<R1C1Cell> inputs = new TreeSet<R1C1Cell>(R1C1Cell.getComparator());

        if (ca.isRowCA) {
            int row = ca.rowOrColumn;
            for (int col = ca.start; col <= ca.end; col++) {
                Cell cell = sheet.getRow(row).getCell(col);
                if (cell != null
                        && cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                    List<R1C1Cell> spec = getOperator(sheet, cell.getColumnIndex(), cell.getRowIndex());
                    for (R1C1Cell r1C1Cell : spec) {
                        if (!inputs.contains(r1C1Cell)) {
                            inputs.add(r1C1Cell);
                        }
                    }
                }
            }
        } else {
            int col = ca.rowOrColumn;
            for (int row = ca.start; row <= ca.end; row++) {
                Cell cell = sheet.getRow(row).getCell(col);
                if (cell != null
                        && cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
                    List<R1C1Cell> spec = getOperator(sheet, cell.getColumnIndex(), cell.getRowIndex());
                    for (R1C1Cell r1C1Cell : spec) {
                        if (!inputs.contains(r1C1Cell)) {
                            inputs.add(r1C1Cell);
                        }
                    }
                }
            }
        }

        return inputs;
    }

    public List<R1C1Cell> getOperator(Sheet sheet, int col, int row) {
        Workbook wb         = sheet.getWorkbook();
        Cell     cell       = sheet.getRow(row).getCell(col);
        String   formulaStr = cell.getCellFormula();

        Ptg[]    tokens     = getPtg(formulaStr, wb, FormulaType.forInt(2), wb.getSheetIndex(sheet));

        ArrayList<R1C1Cell> res = new ArrayList<R1C1Cell>();

		for (Ptg token : tokens) {
			boolean found_something = false;
			if (token.getClass() == RefPtg.class) {
				RefPtg p = (RefPtg) token;

				R1C1Cell c = new R1C1Cell();
				c.column = p.getColumn();
				c.row = p.getRow();
				c.curColumn = col;
				c.curRow = row;

				if (!res.contains(c))
					res.add(c);
				found_something = true;

				//System.out.println("RefPtg found -> " + tokens[i]);
			}
			if (token.getClass() == Ref3DPtg.class) {
				continue;
			}
			if (token.getClass() == AreaPtg.class) {
				AreaPtg p = (AreaPtg) token;
				int col1 = p.getFirstColumn();
				int row1 = p.getFirstRow();
				int col2 = p.getLastColumn();
				int row2 = p.getLastRow();
				for (int x = col1; x <= col2; x++) {
					for (int y = row1; y <= row2; y++) {
						R1C1Cell c = new R1C1Cell();
						c.column = x;
						c.row = y;
						c.curColumn = col;
						c.curRow = row;
						if (!res.contains(c))
							res.add(c);
					}
				}
				//System.out.println("AreaPtg found -> " + tokens[i]);
				found_something = true;
			}
			if (token.getClass() == Area3DPtg.class) {
				continue;
			}
			if (!found_something) {
				/* debug */
			}
		}

        return res;
    }
}
