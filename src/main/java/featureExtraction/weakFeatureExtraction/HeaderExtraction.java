package featureExtraction.weakFeatureExtraction;

import clustering.smellDetectionClustering.FakeCell;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.List;

//import static programEntry.GP.O5;


public class HeaderExtraction {
	private static Logger logger = LogManager.getLogger(HeaderExtraction.class.getName());

	private Sheet sheet;
	private Cell cell;
	
	public HeaderExtraction(Sheet sheet, Cell cell) {
		this.sheet = sheet;
		this.cell = cell;
	}

	public HeaderExtraction(Sheet sheet) {
	    this.sheet = sheet;
    }

	public List<Header> getFeature() {
		List<Header> headersList = new ArrayList<Header>();
		/*
		0 is horizontal Header, 1 is vertical Header.
		level 1 header.
		 */
		for (int i=0; i<=1; i++) {
			Header header = findHeaderStr(cell, i, 1);
			if (header != null) {
				headersList.add(header);
				/*
				level 2 header.
				 */
//				if (O5) {
//					for (int j = 0; j <= 1; j++) {
//						Header secondHeader = findHeaderStr(header.getCell(), j, 2);
//						if (secondHeader != null) {
//							headersList.add(secondHeader);
//						}
//					}
//				}
			}
		}
			
		return headersList;
	}

	public FakeCell findHeaderPosition(Cell cell, int isRow, int level) {
        String hd = "";
        int rowNum = cell.getRowIndex();
        int colNum = cell.getColumnIndex();

        Cell tempCell = null;
        Row tempRow = cell.getRow();
        Row.MissingCellPolicy missingCellPolicy = Row.CREATE_NULL_AS_BLANK;
        int i = 1;

        if (isRow == 1) {
            while (rowNum - i >= 0) {
                tempRow = sheet.getRow(rowNum - i);
                if (tempRow!=null){
                    tempCell = tempRow.getCell(colNum, missingCellPolicy);
                    if (tempCell != null){
                        if (tempCell.getCellType() != 1 && !hd.equals("")){
                            break;
                        }
                        else if (tempCell.getCellType() == 1){
                            return new FakeCell(tempCell.getRowIndex(), tempCell.getColumnIndex());
                        }
                    }
                }
                i++;
            }
        }
        else if (isRow == 0) {
            while (colNum - i >= 0) {
                if (tempRow!=null){
                    tempCell = tempRow.getCell(colNum - i, missingCellPolicy);
                    if (tempCell != null){
                        if (tempCell.getCellType() != 1 && !hd.equals("")){
                            break;
                        }
                        else if (tempCell.getCellType() == 1){
                            return new FakeCell(tempCell.getRowIndex(), tempCell.getColumnIndex());
                        }
                    }
                }
                i++;
            }
        }

        for (int j = cell.getRowIndex()-1; j >= 0; j--) {
        	Row row = sheet.getRow(j);
        	if (row == null) {
        		return new FakeCell(j+1, cell.getColumnIndex());
        	}

        	Cell cellInRowi = row.getCell(cell.getColumnIndex());
        	if (cellInRowi == null || cellInRowi.getCellType() == 3) {
        		return new FakeCell(j+1, cell.getColumnIndex());
        	}
        }

		return null;
    }

	private Header findHeaderStr(Cell cell, int isRow, int level) {
		String hd = "";
		int rowNum = cell.getRowIndex();
		int colNum = cell.getColumnIndex();

		Cell tempCell = null;
		Row tempRow = cell.getRow();
		Row.MissingCellPolicy missingCellPolicy = Row.CREATE_NULL_AS_BLANK;
		int i = 1;

		if (isRow == 1) {
			while (rowNum - i >= 0) {
				tempRow = sheet.getRow(rowNum - i);
				if (tempRow!=null){
					 tempCell = tempRow.getCell(colNum, missingCellPolicy);
					 if (tempCell != null){
						 if (tempCell.getCellType() != 1 && !hd.equals("")){
							break;
						 }
				 		 else if (tempCell.getCellType() == 1){
							hd = tempCell.getStringCellValue() + hd;
						 }
					}
				}
				i++;
			}
		}
		else if (isRow == 0) {
			while (colNum - i >= 0) {
				if (tempRow!=null){
					tempCell = tempRow.getCell(colNum - i, missingCellPolicy);
					 if (tempCell != null){
						 if (tempCell.getCellType() != 1 && !hd.equals("")){
							 break;
						 }
						 else if (tempCell.getCellType() == 1){
							 hd = tempCell.getStringCellValue()  + hd;
						 }
					 }
				}
				i++;
			}
		}
		
		if (!hd.equals("")){
			assert tempCell != null;
			/*
			logger.debug("cell's row=" + cell.getRowIndex()+ " column="+cell.getColumnIndex()
							+": header cell's row="+tempCell.getRowIndex()+", column="+tempCell.getColumnIndex()
							+", str=" + hd);
							*/
			return new Header(tempCell, hd, isRow, level);
		}
		
		return null;
	}
}
