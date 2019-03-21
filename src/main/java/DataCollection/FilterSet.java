package DataCollection;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class FilterSet {

    public Set<String> set = new HashSet<>();

    public FilterSet() throws IOException, InvalidFormatException {

        String inputFN = "D:\\Dali\\CC1s-concurrent\\Inputs\\FilterSet.xlsx";
        File inputF = new File(inputFN);

        Workbook workbook = WorkbookFactory.create(new FileInputStream(inputF));
        Sheet sheet = workbook.getSheet("6000+");

        for (int i = 1; i< 6259; i++) {
            Row row = sheet.getRow(i);
            String category = Integer.toString((int)row.getCell(0).getNumericCellValue());

            String ss = row.getCell(1).getStringCellValue();
            String ws = row.getCell(2).getStringCellValue();

            set.add(category + "/" + ss + "/" + ws);
            System.out.println(category + "/" + ss + "/" + ws);
        }

    }
}
