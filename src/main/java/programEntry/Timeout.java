package programEntry;

import java.util.logging.Logger;

public class Timeout {
    static Logger logger = Logger.getLogger("Timeout");
    static String spreadsheetName, sheetName;


    public static void main(String[] args) {
        if (args.length != 2) {
            logger.info("缺少参数");
            return;
        }

        spreadsheetName = args[0];
        sheetName = args[1];




    }
}
