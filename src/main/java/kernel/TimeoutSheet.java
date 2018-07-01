package kernel;

public class TimeoutSheet {

    public String spreadsheetName;
    public String sheetName;


    public TimeoutSheet(String ssName, String stName) {
        this.sheetName = stName;
        this.spreadsheetName = ssName;
    }
}
