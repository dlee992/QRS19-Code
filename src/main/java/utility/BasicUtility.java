package utility;

import entity.CellLocation;
import clustering.smellDetectionClustering.Smell;
import entity.InfoOfSheet;
import entity.Cluster;
import entity.R1C1Cell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.xssf.usermodel.XSSFFont;
import programEntry.GP;

import java.text.SimpleDateFormat;
import java.util.*;

import static programEntry.GP.filterString;


public class BasicUtility {
    private static Logger logger = LogManager.getLogger(BasicUtility.class.getName());

    public InfoOfSheet infoExtractedPOI(Sheet sheet) {

        List<Cell> dataCells                 = new ArrayList<Cell>();
        Map<String, List<String>> formulaMap = new HashMap<String, List<String>>();
        List<Cell> uiCells                   = new ArrayList<Cell>();


        PaneInformation paneInformation = sheet.getPaneInformation();
        short freezeColumn = -1;
        if (paneInformation != null && paneInformation.isFreezePane())
        {
            freezeColumn = paneInformation.getVerticalSplitPosition();
            System.out.println("freeze Column = " +freezeColumn);
        }

        for (Row r : sheet) {
            for (Cell c : r) {
                uiCells.add(c);

                String cellAddA1;
                String cellFormulaA1;
                String cellFormulaR1C1;

                if (c.getCellType() == 0) {
                    if (GP.plusFrozen) {
                        if (c.getColumnIndex() >= freezeColumn)
                            dataCells.add(c);
                    }
                    else {
                        dataCells.add(c);
                    }
                }
                else if (c.getCellType() == 2 && !c.toString().contains("#") && !c.toString().contains("!"))
                {
                    if (filterString) {
						boolean ValueType = false;
						try {
							c.getStringCellValue();
						} catch (Exception e) {
							ValueType = true;
						}
						if (!ValueType) continue;
					}
					
                    int row = c.getRowIndex();
                    int column = c.getColumnIndex();

                    CellReference cr2 = new CellReference(c);
                    cellAddA1 = cr2.formatAsString();

                    cellFormulaA1 = c.getCellFormula();
                    cellFormulaR1C1 = convertA1ToR1C1(row , column , cellFormulaA1);

                    List<String> info = new ArrayList<String>();
                    info.add(cellFormulaA1);
                    info.add(cellFormulaR1C1);

                    String dp = cellDependencies(sheet, cellAddA1, 0);

//                    System.out.println("Before checking the tree : "+ cellAddA1 + " R1C1 = " +cellFormulaR1C1
//                            + " dp = " + dp);
                    if (!dp.equals("{RR}")) {
//                        System.out.println("add cell = " + cellAddA1);
                        formulaMap.put(cellAddA1, info);
                    }
                }
            }
        }

        return new InfoOfSheet(formulaMap, dataCells);
    }

    public String convertA1ToR1C1(int row, int column, String formula) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < formula.length(); ) {
            int start = i;
            int end = i;
            // external worksheet
            if (formula.charAt(i) == '\'') {
                i++;
                while (i < formula.length() && formula.charAt(i) != '\'')
                    i++;
                i += 2; // skip the '!'
                end = i;

                result.append(formula.substring(start, end));
			} else if (!isLetter(formula.charAt(i)) && formula.charAt(i) != '$') {
                // the basic operations for excel
				while (i < formula.length()
						&& (!isLetter(formula.charAt(i))
						&& formula.charAt(i) != '$' && formula
						.charAt(i) != '\'')) {
					i++;
				}
                end = i;

                result.append(formula.substring(start, end));
			} else {
                if (isLetter(formula.charAt(i)) || formula.charAt(i) == '$') {
                    // deal with the "row"
                    if (formula.charAt(i) == '$') {
                        i++; // skip the $
                    }
					while (i < formula.length() && isLetter(formula.charAt(i))) {
						i++;
					}

                    if (isNumber(formula.charAt(i)) || formula.charAt(i) == '$') {
                        // deal with the "column"
                        if (formula.charAt(i) == '$') {
                            i++;
                        }
						while (i < formula.length()
								&& isNumber(formula.charAt(i))) {
							i++;
						}
                        end = i;
                        result.append(extractCell(row, column,
                                formula.substring(start, end)));
                    } else { // some func, not row number.
                        end = i;
                        result.append(formula.substring(start, end));
					}
                }
            }
        }

        return result.toString();
    }

    public String convertA1ToA1(int pRow, int pColumn, String formula){
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < formula.length(); ) {
            int start = i;
            int end;
            // external worksheet
            if (formula.charAt(i) == '\'') {
                i++;
                while (i < formula.length() && formula.charAt(i) != '\'')
                    i++;
                i += 2; // skip the '!'
                end = i;

                result.append(formula.substring(start, end));
            } else if (!isLetter(formula.charAt(i)) && formula.charAt(i) != '$') {
                // the basic operations for excel
				while (i < formula.length()
						&& (!isLetter(formula.charAt(i))
						&& formula.charAt(i) != '$' && formula
						.charAt(i) != '\'')) {
					i++;
				}
                end = i;

                result.append(formula.substring(start, end));
            } else {
                if (isLetter(formula.charAt(i)) || formula.charAt(i) == '$') {
                    // deal with the "row"
                    if (formula.charAt(i) == '$') {
                        i++; // skip the $
                    }
					while (i < formula.length() && isLetter(formula.charAt(i))) {
						i++;
					}

                    if (isNumber(formula.charAt(i)) || formula.charAt(i) == '$') {
                        // deal with the "column"
                        if (formula.charAt(i) == '$') {
                            i++;
                        }
						while (i < formula.length()
								&& isNumber(formula.charAt(i))) {
							i++;
						}
                        end = i;

                        StringBuffer sb = extractCell2(pRow, pColumn, formula.substring(start, end));
                        if (sb == null) return null;
                        String isOrNotExceedBound = sb.toString();

                        result.append(isOrNotExceedBound);
                    } else { // some func, not row number.
                        end = i;
                        result.append(formula.substring(start, end));
                    }
                }
            }
        }

//        logger.debug("result = " + result);
        return result.toString();
    }

    private StringBuffer extractCell2(int pRow, int pColumn, String ref){
        StringBuffer newReference = new StringBuffer();
        String sCol;
        String sRow;
        boolean colRelative = true;
        boolean rowRelative = true;
        int i;
        if (ref.startsWith("$")) {
			i = 1;
			while (i < ref.length() && isLetter(ref.charAt(i))) {
				i++;
			}
            sCol = ref.substring(1, i);
            colRelative = false;
        } else {
			i = 0;
			while (i < ref.length() && isLetter(ref.charAt(i))) {
				i++;
			}
            sCol = ref.substring(0, i);
        }

        if (ref.charAt(i) == '$') {
            int start = i + 1;
			i = i + 1;
			while (i < ref.length() && isNumber(ref.charAt(i))) {
				i++;
			}
            sRow = ref.substring(start, i);
            rowRelative = false;
        } else {
            int start = i;
			while (i < ref.length() && isNumber(ref.charAt(i))) {
				i++;
			}
            sRow = ref.substring(start, i);
        }


        if (rowRelative) {
            int row = Integer.parseInt(sRow) + pRow;
            if (row < 0) return null;
            sRow = String.valueOf(row);
        }else
            sRow = '$'+sRow;

        if (colRelative) {
            int iCol = 0;
            for (int j = 0; j < sCol.length(); j++) {
                iCol = iCol * 26 + sCol.charAt(j) - 'A' + 1;
            }
            iCol += pColumn;
            if (iCol < 0) return null;
            sCol = "";
            while (iCol > 0) {
                sCol = (char)((iCol-1) % 26 + 'A') + sCol;
                iCol = (iCol-1) / 26;
            }
        }
        else
            sCol = '$'+sCol;

        return newReference.append(sCol).append(sRow);
    }

    public static R1C1Cell extractCell(int row, int column, String ref) {
        String sCol;
        String sRow;
        boolean colRelative = true;
        boolean rowRelative = true;
        int i;
        if (ref.startsWith("$")) {
			i = 1;
			while (i < ref.length() && isLetter(ref.charAt(i))) {
				i++;
			}
            sCol = ref.substring(1, i);
            colRelative = false;
        } else {
			i = 0;
			while (i < ref.length() && isLetter(ref.charAt(i))) {
				i++;
			}
            sCol = ref.substring(0, i);
        }

        if (ref.charAt(i) == '$') {
            int start = i + 1;
			i = i + 1;
			while (i < ref.length() && isNumber(ref.charAt(i))) {
				i++;
			}
            sRow = ref.substring(start, i);
            rowRelative = false;
        } else {
            int start = i;
			while (i < ref.length() && isNumber(ref.charAt(i))) {
				i++;
			}
            sRow = ref.substring(start, i);
        }

        int iCol = 0;
        for (int j = 0; j < sCol.length(); j++) {
            iCol = iCol * 26 + sCol.charAt(j) - 'A' + 1;
        }
        iCol--;

        int iRow = Integer.parseInt(sRow) - 1;

        R1C1Cell cell = new R1C1Cell();
        cell.curRow = row;
        cell.curColumn = column;
        cell.row = iRow;
        cell.rowRelative = rowRelative;
        cell.column = iCol;
        cell.columnRelative = colRelative;

        return cell;
    }

    private static boolean isLetter(char c) {
        return c >= 'A' && c <= 'Z';
    }

    private static boolean isNumber(char c) {
        return c >= '0' && c <= '9';
    }

    public List<R1C1Cell> getAreaCells(R1C1Cell preCell, R1C1Cell curCell) {
        List<R1C1Cell> cells = new ArrayList<R1C1Cell>();

        for (int row = preCell.row; row <= curCell.row; row++) {
            for (int col = preCell.column; col <= curCell.column; col++) {
                R1C1Cell cell = new R1C1Cell();

                cell.curRow = preCell.curRow;
                cell.curColumn = preCell.curColumn;
                cell.row = row;
                cell.column = col;
                cell.rowRelative = preCell.rowRelative;
                cell.columnRelative = preCell.columnRelative;

                cells.add(cell);
            }
        }

        return cells;
    }

    public static String cellDependencies(Sheet sheet, String cellAddA1, int depth) {

            String dependencyTree = "";
            if (depth == 0) dependencyTree = "{RR";

            CellReference crTmp = new CellReference(cellAddA1);
            int row = crTmp.getRow();
            int col = crTmp.getCol();

        try {
            Cell cell = sheet.getRow(row).getCell(col);

            if (cell.getCellType() == 2 && !cell.toString().contains("#") && !cell.toString().contains("!")) {
                List<CellLocation> cls = new FormulaParsing().getFormulaDependencies(sheet, col, row);

                for (CellLocation cl : cls) {
                    int currentRow = cl.getRow();
                    int currentCol = cl.getColumn();

                    Sheet currentSheet = sheet.getWorkbook().getSheet(cl.getSheet_name());
                    if (currentSheet != null) {
                        Row r = currentSheet.getRow(currentRow);
                        if (r != null) {
                            Cell currentCell = r.getCell(currentCol);
                            if (currentCell != null) {
                                CellReference cr = new CellReference(currentCell);
                                String r1c1 = extractCell(row, col, cr.formatAsString())
                                        .toString();

                                //FIXME: if the referenced cell is empty, program doesn't execute here
                                dependencyTree += "{" + cl.getSheet_name() + "!" + r1c1;
                                try {
                                    dependencyTree += cellDependencies(currentSheet, cr.formatAsString(), 1);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            }

            dependencyTree = dependencyTree + "}";

        }
        catch (Exception e) {
            logger.debug("sheetName=" + sheet.getSheetName() +" row=" + row + " col=" + col);
            e.printStackTrace();
        }
        return dependencyTree;
    }

    public List<R1C1Cell> extractParameters(int row, int column, String formula) {
        List<R1C1Cell> paras = new ArrayList<R1C1Cell>();

        boolean isRange = false;
        R1C1Cell preCell = null;

        for (int i = 0; i < formula.length(); ) {
            int start = i;
            int end = i;
            // external worksheet
            if (formula.charAt(i) == '\'') {
                i++;
                while (i < formula.length() && formula.charAt(i) != '\'')
                    i++;
                i += 2; // skip the '!'

			} else if (!isLetter(formula.charAt(i)) && formula.charAt(i) != '$') {
                // the basic operations for excel
                for (; i < formula.length()
                        && (!isLetter(formula.charAt(i))
                        && formula.charAt(i) != '$' && formula
                        .charAt(i) != '\''); i++)
                    ;

			} else { // a new inputs cells.
                R1C1Cell cell = null;

                if (isLetter(formula.charAt(i)) || formula.charAt(i) == '$') {
                    // deal with the "row"
                    if (formula.charAt(i) == '$') {
                        i++; // skip the $
                    }
					while (i < formula.length() && isLetter(formula.charAt(i))) {
						i++;
					}
                    if (isNumber(formula.charAt(i)) || formula.charAt(i) == '$') {
                        // deal with the "column"
                        if (formula.charAt(i) == '$') {
                            i++;
                        }
						while (i < formula.length()
								&& isNumber(formula.charAt(i))) {
							i++;
						}
                        end = i;

                        cell = extractCell(row, column,
                                formula.substring(start, end));
                    } else {
                        // some excel function.
						// result.append(formula.substring(start, end));
                        continue;
                    }
                }
                if (cell != null) {
                    if (isRange) {
                        List<R1C1Cell> cells = getRangeCells(preCell, cell);
                        for (R1C1Cell c : cells) {
                            paras.add(c);
                        }

                        isRange = false;
                        preCell = null;
                    } else if (i < formula.length() && formula.charAt(i) == ':') {
                        isRange = true;
                        preCell = cell;
                    } else {
                        paras.add(cell);
                    }

				}
            }
        }

        return paras;
    }

    private List<R1C1Cell> getRangeCells(R1C1Cell preCell, R1C1Cell curCell) {
        List<R1C1Cell> cells = new ArrayList<R1C1Cell>();

        if (preCell.row != curCell.row) {
            for (int row = preCell.row; row <= curCell.row; row++) {
                R1C1Cell cell = new R1C1Cell();

                cell.curRow = preCell.curRow;
                cell.curColumn = preCell.curColumn;
                cell.row = row;
                cell.column = preCell.column;
                cell.rowRelative = preCell.rowRelative;
                cell.columnRelative = preCell.columnRelative;

                cells.add(cell);
            }
        } else {
            for (int col = preCell.column; col <= curCell.column; col++) {
                R1C1Cell cell = new R1C1Cell();

                cell.curRow = preCell.curRow;
                cell.curColumn = preCell.curColumn;
                cell.row = preCell.row;
                cell.column = col;
                cell.rowRelative = preCell.rowRelative;
                cell.columnRelative = preCell.columnRelative;

                cells.add(cell);
            }
        }

        return cells;
    }

    public void smellyCellMark(Workbook wb, Sheet sheet,
                                      List<Smell> smells) {
        CreationHelper factory = wb.getCreationHelper();

        for (Smell sl : smells) {
            CellReference cr = sl.getCr();
            String description = sl.toString();
            Drawing drawing = sheet.createDrawingPatriarch();

            if (cr != null) {
                Cell cell = sheet.getRow(cr.getRow()).getCell(cr.getCol());

                if (cell != null) {
                    Comment comment = cell.getCellComment();
                    if (comment != null) {
                        RichTextString rts = comment.getString();

                        RichTextString newStr = factory.createRichTextString(rts.toString() + "\n" + description);
                        comment.setString(newStr);
                        comment.setAuthor("Da Li");
                    } else {
                        // When the comment box is visible, have it show in a 7*5 space
                        ClientAnchor anchor = factory.createClientAnchor();
                        anchor.setCol1(cr.getCol());
                        anchor.setCol2(cr.getCol() + 5);
                        anchor.setRow1(cr.getRow());
                        anchor.setRow2(cr.getRow() + 7);

                        // Create the comment and set the text+author
                        comment = drawing.createCellComment(anchor);
                        RichTextString str = factory.createRichTextString(description);
                        comment.setString(str);
                        comment.setAuthor("Li");

                        // Assign the comment to the cell
                        cell.setCellComment(comment);
                    }
                }
            }
        }
    }

    public void clusterPrintMark(List<Cluster> clusters, Sheet sheet) {
        CellStyle style = sheet
                .getWorkbook().createCellStyle();

        for (Row r : sheet) {
            for (Cell cell : r) {
                style.setFillPattern(CellStyle.NO_FILL);
                cell.setCellStyle(style);
            }
        }

        //ColorSet color = new ColorSet();
        short i = 0;

        IndexedColors[] color = IndexedColors.values();

        //FillPatternType[] pattern = FillPatternType.values();
        List<IndexedColors> pickedColor = new ArrayList<IndexedColors>(Arrays.asList(color));
        pickedColor.remove(IndexedColors.AUTOMATIC);
        pickedColor.remove(IndexedColors.BLACK);
        pickedColor.remove(IndexedColors.DARK_BLUE);
        pickedColor.remove(IndexedColors.DARK_GREEN);
        pickedColor.remove(IndexedColors.DARK_TEAL);
        pickedColor.remove(IndexedColors.DARK_RED);
        pickedColor.remove(IndexedColors.DARK_YELLOW);
        pickedColor.remove(IndexedColors.GREY_80_PERCENT);
        pickedColor.remove(IndexedColors.WHITE);
        pickedColor.remove(IndexedColors.BLUE);
        pickedColor.remove(IndexedColors.BLUE_GREY);
        pickedColor.remove(IndexedColors.RED);

        for (Cluster cluster : clusters) {
            if (cluster != null) {

                // first, color the seed cells
                CellStyle clusterStyle = sheet
                        .getWorkbook().createCellStyle();
                Font font = sheet.getWorkbook().createFont();
                font.setFontName(XSSFFont.DEFAULT_FONT_NAME);
                //font.setFontHeightInPoints((short)15);
                //font.setColor(IndexedColors.DARK_RED.getIndex());
                clusterStyle.setFont(font);

                List<Cell> seedCellList = cluster.getSeedCells();

                for (Cell cell : seedCellList) {
                    // if cell is contained in seed cluster, then its font color is set as dark_red.


                    if (i < pickedColor.size()) {
                        clusterStyle.setFillForegroundColor(pickedColor.get(i).getIndex());
                        clusterStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
                    } else if (i < 2 * pickedColor.size()) {
                        clusterStyle.setFillForegroundColor(pickedColor.get(i - pickedColor.size()).getIndex());
                        clusterStyle.setFillPattern(CellStyle.FINE_DOTS);
                    } else {
                        clusterStyle.setFillForegroundColor(pickedColor.get(i - 2 * pickedColor.size()).getIndex());
                        clusterStyle.setFillPattern(CellStyle.LEAST_DOTS);
                    }

                    cell.setCellStyle(clusterStyle);
                }

                // second, color the non-seed cells
                clusterStyle = sheet.getWorkbook().createCellStyle();
                font = sheet.getWorkbook().createFont();
                font.setFontName(XSSFFont.DEFAULT_FONT_NAME);
                font.setFontHeightInPoints((short)15);
                font.setColor(IndexedColors.DARK_RED.getIndex());
                clusterStyle.setFont(font);
                List<Cell> cellList = cluster.getClusterCells();
                for (Cell cell: cellList)
                    if (!seedCellList.contains(cell)) {
                        if (i < pickedColor.size()) {
                            clusterStyle.setFillForegroundColor(pickedColor.get(i).getIndex());
                            clusterStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
                        } else if (i < 2 * pickedColor.size()) {
                            clusterStyle.setFillForegroundColor(pickedColor.get(i - pickedColor.size()).getIndex());
                            clusterStyle.setFillPattern(CellStyle.FINE_DOTS);
                        } else {
                            clusterStyle.setFillForegroundColor(pickedColor.get(i - 2 * pickedColor.size()).getIndex());
                            clusterStyle.setFillPattern(CellStyle.LEAST_DOTS);
                        }

                        cell.setCellStyle(clusterStyle);
                    }

                // iterating
                i++;
            }
        }
    }

    public void cellMark(List<CellReference> crList, Sheet sheet) {
        CellStyle clusterStyle = sheet.getWorkbook().createCellStyle();

        for (CellReference cr : crList) {
            Cell cell = sheet.getRow(cr.getRow()).getCell(cr.getCol());
            if (cell == null) continue;

            clusterStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            clusterStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            cell.setCellStyle(clusterStyle);
        }
    }

    public double getNumericalValue(Sheet sheet, R1C1Cell cell, int row,
                                           int col) throws Exception {
        R1C1Cell trueInput = cell.getTrueCell(row, col);

        Row r = sheet.getRow(trueInput.row);
        if (r == null)
            return 0;

        Cell c = r.getCell(trueInput.column);

        return getNumericalValue(c);
    }

    public double getNumericalValue(Cell c) throws Exception {
        if (c != null && c.getCellType() == Cell.CELL_TYPE_STRING) {
            if (c.getStringCellValue().trim().equals("-")) {
                return 0.0;
            }
            throw new Exception("Not a number");
        }
        double data = 0;
        if (c != null) {
            data = c.getNumericCellValue();
        }
        return data;
    }

    public String getCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime());
    }
}


