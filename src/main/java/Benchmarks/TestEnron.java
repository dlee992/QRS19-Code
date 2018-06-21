package Benchmarks;

import org.apache.commons.io.FileUtils;
import utility.BasicUtility;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static programEntry.GP.fileSeparator;
import static programEntry.GP.parent_dir;

public class TestEnron {

    static Logger logger = Logger.getLogger("Enron");
    private static int SUM;
    Random random = new Random();
    Set<String> occurredNames = new HashSet<>();

    String versionString = parent_dir + fileSeparator + "Inputs" + fileSeparator + "VEnron2";
    File versionDir = new File(versionString);

    String enronString = parent_dir + fileSeparator + "Inputs" + fileSeparator + "Enron-Corpus";
    File enronDir = new File(enronString);

    String outputString = parent_dir + fileSeparator + "Inputs" + fileSeparator + "Enron-Selected" +
            new BasicUtility().getCurrentTime();


    public static void main(String[] args) throws IOException {
        readAll();

    }

    private static void readAll() {
    }


    public void removeDuplicates() throws IOException {
        recursive(versionDir, 1);

//        System.exit(0);
        //TODO 剩下没有出现的都直接放到selected中
        File leftDir = new File(outputString + fileSeparator + "left");
        if (!leftDir.exists()) leftDir.mkdir();

        int cnt = 0;
        for (File leftFile:
             enronDir.listFiles()) {
            //if (++cnt == 5) break;
            String name = leftFile.getName();
            String identity = name.substring(name.lastIndexOf("__")+2, name.indexOf('.'));
            logger.info("name = " + name + ", identity = " + identity);
            if (!occurredNames.contains(identity)) {
                FileUtils.copyFile(leftFile, new File(leftDir + fileSeparator + name));
            }
        }

    }

    private void recursive(File versionDir, int depth) throws IOException {

        for (File subDir:
                versionDir.listFiles()) {

//            if (depth == 1 && !subDir.getName().equals("4")) continue;

            if (!subDir.isDirectory()) continue;

            logger.info("Evolution Group = " + subDir.getName());

            int sum = subDir.listFiles().length;
            int selected = random.nextInt(sum);
            File selectedFile = subDir.listFiles()[selected];
            logger.info("Selected File = " + selectedFile.getName());

            boolean recursiveFlag = false;

            for (File versionedFile:
                    subDir.listFiles()) {

                if (versionedFile.isDirectory()) {
                    if (!recursiveFlag) {
                        logger.info("in");
                        recursive(subDir, 2);
                        recursiveFlag = true;
                        logger.info("out");
                    }
                    continue;
                }

                SUM++;
                if (versionedFile.getName().contains("Tony_deals")) {
                    logger.info("Tony_deals!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    System.exit(1);
                }

                String versionName = versionedFile.getName();

                logger.info("versionName = " + versionName);
                String identity = versionName.substring(versionName.indexOf('_')+1, versionName.indexOf('.'));

                if (!occurredNames.contains(identity)) occurredNames.add(identity);

                if (versionedFile.getName().equals(selectedFile.getName())) {
                    //TODO 在enronDir下 寻找所有identity相同的文件

                    List<File> backups = new ArrayList<>();

                    for (File enronFile:
                            enronDir.listFiles()) {
                        String name = enronFile.getName();
                        if (name.contains(identity)) {
                            backups.add(enronFile);
                            logger.info("Backup File = " + enronFile.getName());
                        }
                    }

                    sum = backups.size();
                    String suffix = outputString + fileSeparator;
                    if (depth == 2) suffix += subDir.getParentFile().getName() + "-";
                    suffix += subDir.getName();
                    File newDir = new File(suffix);
                    if (!newDir.exists()) newDir.mkdir();
                    File testFile;

                    if (sum == 0) {
                        //TODO 不清楚为什么会存在元数据集中没有的文件
                        testFile = selectedFile;
                    }
                    else {
                        selected = random.nextInt(sum);
                        testFile = backups.get(selected);
                    }

                    FileUtils.copyFile(testFile,
                            new File(newDir.getAbsolutePath() + fileSeparator + testFile.getName()));
                    logger.info("versionFile = " + versionName + ", testFile = " + testFile.getName());
                }
            }
        }

    }
}
