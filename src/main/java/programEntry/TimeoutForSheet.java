package programEntry;


import static programEntry.TestEUSES.TIMEOUT;

public class TimeoutForSheet implements Runnable {


    public Thread mainThread;


    public TimeoutForSheet(Thread mainThread) {
        this.mainThread = mainThread;
    }


    @Override
    public void run() {
        try {
            System.out.println(System.nanoTime() + "TimeoutForSheet begin to run");
            Thread.sleep(TIMEOUT * 1000);
            mainThread.interrupt();
            System.out.println(System.nanoTime() + "One thread in the thread pool should be stopped");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
