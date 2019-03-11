package DataCollection;

import org.apache.poi.ss.usermodel.Row;

import java.util.ArrayList;
import java.util.List;

public class Worksheet_result {
    public String spreadsheet, sheet;
    public List<String> smellList = new ArrayList<>();

    public void handleOneRow(Row row) {
        spreadsheet = row.getCell(1).toString();
        sheet = row.getCell(2).toString();
        String smellString = row.getCell(3).toString();
        int p = 0, q ;
        while (p < smellString.length()) {
            while (p < smellString.length() && !(smellString.charAt(p) >= 'A' && smellString.charAt(p) <= 'Z')) p++;
            q=p;
            while (q < smellString.length() && !(smellString.charAt(q) == ',' || smellString.charAt(q) == ']')) q++;
            if (p < smellString.length() && q < smellString.length()) {
                smellList.add(smellString.substring(p, q));
                p = q;
            }
        }
        System.out.println(smellString +" == "+ smellList.toString());
    }
}
