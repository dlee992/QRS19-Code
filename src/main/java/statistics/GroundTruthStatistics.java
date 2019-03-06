package statistics;

import entity.Cluster;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by nju-lida on 16-6-28.
 */
public class GroundTruthStatistics {
    public List<Cluster> clusterList;
    public List<CellReference> smellList;
    public static AtomicInteger smellCount = new AtomicInteger(0);

    public void read(String fileName, String sheetName) throws IOException, InvalidFormatException {
        clusterList = new ArrayList<Cluster>();
        smellList   = new ArrayList<CellReference>();

        //hard code bug.
        // * @param      beginIndex   the beginning index, inclusive.
        // * @param      endIndex     the ending index, exclusive.
        File file = new File(fileName.substring(0, fileName.lastIndexOf('.')) + "_" + sheetName + ".xls");

        if (!file.exists()) return;
        Workbook workbook = WorkbookFactory.create(new FileInputStream(file));

        Sheet sheet = workbook.getSheet(sheetName);
        for (Row r : sheet) {
            for (Cell cell : r) {
                if (cell.getCellStyle().getFillForegroundColor()!= IndexedColors.AUTOMATIC.getIndex() &&
                        cell.getCellStyle().getFillForegroundColor() != IndexedColors.WHITE.getIndex()) {

                    //System.out.println("");

                    Cluster cluster = new Cluster(String.valueOf(cell.getCellStyle().getFillForegroundColor())+","+cell.getCellStyle().getFillPattern());

                    if (!clusterList.contains(cluster)) {
                        clusterList.add(cluster);
                    }
                    clusterList.get(clusterList.indexOf(cluster)).addChild(new Cluster(new CellReference(cell).formatAsString()));
                }

                if (cell.getCellComment() != null) {
                    smellList.add(new CellReference(cell));
                }
            }
        }

        System.out.print("@@@@@@ SmellCount = " + smellCount.addAndGet(smellList.size()));
        workbook.close();
    }

}
