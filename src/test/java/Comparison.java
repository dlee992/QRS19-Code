
import utility.BasicUtility;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;

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

    public static void mainx(String[] args) throws IOException {
        /*
        File current_dir = new File(System.getProperty("user.dir"));
        System.out.println(current_dir.getParent());
        */
        String parentFilePath = "D:\\9.28\\";
        String myFileName = "9_1475071073073.xls";
        String MelodyFileName = "ruiqing.xls";

        HSSFWorkbook myWorkbook = new HSSFWorkbook(new FileInputStream(new File(parentFilePath + myFileName)));
        HSSFWorkbook MelodyWorkbook = new HSSFWorkbook(new FileInputStream(new File(parentFilePath + MelodyFileName)));

        HSSFSheet mySheet = myWorkbook.getSheetAt(0);
        HSSFSheet MelodySheet = MelodyWorkbook.getSheetAt(0);


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
                }
            }
        }
        BasicUtility bu = new BasicUtility();
        bu.cellMark(cells, mySheet);
        bu.cellMark(melodyCells, MelodySheet);

        long time = System.currentTimeMillis();
        String myOutFileStr = "My result "+ time +".xls";
        FileOutputStream myOutFile = new FileOutputStream(parentFilePath + myOutFileStr);
        myWorkbook.write(myOutFile);

        String melodyOutFileStr = "Other result "+ time +".xls";
        FileOutputStream melodyOutFile = new FileOutputStream(parentFilePath + melodyOutFileStr);
        MelodyWorkbook.write(melodyOutFile);

    }

}
