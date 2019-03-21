package DataCollection;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class RandomSample {

    public static void main(String args[]) throws IOException, InvalidFormatException {
        String inputFN = "C:\\Users\\njuli\\Desktop\\实验结果\\大数据检测结果\\final Scope.xlsx";
        String outputFN = "C:\\Users\\njuli\\Desktop\\实验结果\\大数据检测结果\\final Scope.xlsx";

        File inputF = new File(inputFN);
        Workbook workbook = WorkbookFactory.create(new FileInputStream(inputF));

        int min = 2-1, max = 1526-1;
        Set<Integer> sampledSet = new HashSet<>();

        Sheet unionSetSheet = workbook.getSheet("UnionSet");
        for (int turn = 8; turn <= 12; turn++) {
            int count = 1;
            sampledSet.clear();
            while (count <= 305) {
                int randomNum = ThreadLocalRandom.current().nextInt(min, max + 1);
                if (!sampledSet.contains(randomNum)) {
                    sampledSet.add(randomNum);
                    Row row = unionSetSheet.getRow(randomNum);
                    row.createCell(turn).setCellValue(1);
                    count++;
                }
            }
        }

        FileOutputStream resultStream = new FileOutputStream(new File(outputFN));
        workbook.write(resultStream);
        resultStream.close();
    }
}
