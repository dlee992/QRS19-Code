import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import utility.BasicUtility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by nju-lida on 16-7-9.
 */
public class Comparison {

    public static void main(String[] args) throws IOException {
        /*
        File current_dir = new File(System.getProperty("user.dir"));
        System.out.println(current_dir.getParent());
        */
        String parentFilePath = "C:\\Users\\njuli\\Desktop\\";
        String myFileName = "raw Default original spreadsheets_70(06-04 22-12-23).xlsx";
        String MelodyFileName = "Opt Default original spreadsheets_70(06-04 22-00-17).xlsx";

        Workbook myWorkbook = new XSSFWorkbook(new FileInputStream(new File(parentFilePath + myFileName)));
        Workbook MelodyWorkbook = new XSSFWorkbook(new FileInputStream(new File(parentFilePath + MelodyFileName)));

        Sheet mySheet = myWorkbook.getSheetAt(0);
        Sheet MelodySheet = MelodyWorkbook.getSheetAt(0);


        List<CellReference> cells = new ArrayList<CellReference>();
        List<CellReference> melodyCells = new ArrayList<CellReference>();
        for (Row row : mySheet) {
            System.out.println("Row------" + row.getRowNum() + "------");
            Row MelodyRow = MelodySheet.getRow(row.getRowNum());
            for (Cell cell : row) {
                System.out.println("    Column--" + cell.getColumnIndex() + "------");
                Cell MelodyCell = MelodyRow.getCell(cell.getColumnIndex());
                if (cell != null && MelodyCell != null) {

                    if (cell.getCellType() == Cell.CELL_TYPE_STRING && MelodyCell.getCellType() == Cell.CELL_TYPE_STRING) {
                        if (!cell.getStringCellValue().equals(MelodyCell.getStringCellValue())) {
                            System.out.println(cell.getStringCellValue());

                            cells.add(new CellReference(cell));
                            melodyCells.add(new CellReference(MelodyCell));
                        }
                    }

                    else if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC && MelodyCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
                        if (cell.getNumericCellValue() != MelodyCell.getNumericCellValue()) {
                            System.out.println(cell.getNumericCellValue());

                            cells.add(new CellReference(cell));
                            melodyCells.add(new CellReference(MelodyCell));
                        }
                    }

                }
            }
        }
        BasicUtility bu = new BasicUtility();
        bu.cellMark(cells, mySheet);
        bu.cellMark(melodyCells, MelodySheet);

        long time = System.currentTimeMillis();
        String myOutFileStr = "My result "+ time +".xlsx";
        FileOutputStream myOutFile = new FileOutputStream(parentFilePath + myOutFileStr);
        myWorkbook.write(myOutFile);

        String melodyOutFileStr = "Other result "+ time +".xlsx";
        FileOutputStream melodyOutFile = new FileOutputStream(parentFilePath + melodyOutFileStr);
        MelodyWorkbook.write(melodyOutFile);

    }

}
