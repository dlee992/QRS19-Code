package DataCollection;

import java.io.File;
import java.util.HashSet;

import static kernel.GP.fileSeparator;
import static kernel.GP.parent_dir;

public class FilterVEnron2 {
    private static String entrance = parent_dir + fileSeparator + "Inputs" + fileSeparator + "VEnron2";

    public static void validate() {
        File directory = new File(entrance);
        int count = 0;
        HashSet<Integer> indexSet = new HashSet<>();
        for (File subDir:
                directory.listFiles()) {
            for (File file:
                 subDir.listFiles()) {
                int parsedInt = Integer.parseInt(subDir.getName());
                indexSet.add(parsedInt);
                System.out.println(++count+"::"+subDir.getName()+"----"+file.getName());
            }
        }

        for(int i=1;i<=1609;i++)
            if (!indexSet.contains(i))
                System.out.println("Missing group="+i);

        System.exit(0);
    }

    public static void main(String[] args) {
        File directory = new File(entrance);
        System.out.println(directory.getName());
        int count = 0;

        validate();

        // visit each evolution group
        // but some groups contain several directories
        // we need to delete these directories except one with maximum index
        // move the file with maximum index into the group's directory

        for (File subDir:
             directory.listFiles()) {

            File savedDir = null;
            File savedFile = null;
            int maxIndex = 0;
            boolean dirFlag = false;
            String[] subDirList = subDir.list();

            //if (!subDir.getName().equals("4")) continue;
            for (int i =0; i<subDirList.length; i++) {
                File subFile = new File(subDir.getPath(), subDirList[i]);
                if (subFile.isDirectory()) {
                    dirFlag = true;
                    int parsedInt = Integer.parseInt(subFile.getName());
                    if (parsedInt < maxIndex) {
                        // delete this directory
                        String[] subList = subFile.list();
                        for (String filename:
                             subList) {
                            File subsubFile = new File(subFile.getPath(), filename);
                            subsubFile.delete();
                        }
                        subFile.delete();
                    }
                    else {
                        // delete savedDir directory
                        if (savedDir != null) {
                            String[] subList = savedDir.list();
                            for (String filename :
                                    subList) {
                                File subsubFile = new File(savedDir.getPath(), filename);
                                subsubFile.delete();
                            }
                            savedDir.delete();
                        }
                        // replace savedDir
                        savedDir = subFile;
                        maxIndex = parsedInt;
                    }
                }
            }

            maxIndex = 0;
            if (dirFlag) {
                String[] fileList = savedDir.list();
                for (String filename:
                     fileList) {
                    File file = new File(savedDir.getPath(), filename);
                    int charIndex = filename.indexOf('_');
                    String prefix = filename.substring(0, charIndex);
                    int index = Integer.parseInt(prefix);

                    if (index > maxIndex) {
                        if (savedFile != null) {
                            savedFile.delete();
                        }
                        savedFile = file;
                        maxIndex = index;
                    }
                    else {
                        file.delete();
                    }
                }

                //move savedFile
                savedFile.renameTo(new File(subDir.getPath(), savedFile.getName()));
                savedDir.delete();
            }

        }
    }
}
