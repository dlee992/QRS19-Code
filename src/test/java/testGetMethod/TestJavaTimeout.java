package testGetMethod;

import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestJavaTimeout {

    private static Logger logger = Logger.getLogger("TestJavaTimeout");

    public static void mainx(String[] args) throws InterruptedException {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (++i != 400_000) {
                    logger.log(Level.INFO, "I am still running i = " + i);
                }
                logger.log(Level.INFO, "I cancel myself.");
            }
        };

        ExecutorService service = Executors.newSingleThreadExecutor();
        Future<?> future = service.submit(runnable);
        logger.log(Level.INFO, "begin to execute non-terminated loop.");
        Thread.sleep(100);
        future.cancel(true);
        logger.log(Level.INFO, "cancel non-terminated loop.");
    }

}
