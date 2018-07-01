package kernel;

import java.util.logging.Logger;

public class Timeout {
    static Logger logger = Logger.getLogger("Timeout");
    static String spreadsheetName, sheetName;


    public static void main(String[] args) {
        TimeoutForSheet timeoutForSheet = new TimeoutForSheet(Thread.currentThread());
        Thread thread = new Thread(timeoutForSheet);
        thread.start();

        try {
            System.out.println(System.nanoTime() + "Main begin to run");
            Thread.sleep(5*1000);
            System.out.println(System.nanoTime() + "Main sleeps for ** seconds");
        } catch (InterruptedException e) {
            System.out.println(System.nanoTime() + "Main is stopped by other thread");
        }

    }
}
