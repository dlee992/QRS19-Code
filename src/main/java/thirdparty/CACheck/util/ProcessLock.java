package thirdparty.CACheck.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class ProcessLock {

	File file = new File(Utils.exprDir() + "tools/lock");
	RandomAccessFile raf = null;
	FileChannel fileChannel = null;
	FileLock fileLock = null;

	public void lock() {
		try {
			if (!file.exists()) {
				file.createNewFile();
			}

			raf = new RandomAccessFile(file, "rw");
			fileChannel = raf.getChannel();
			fileLock = null;
			while (true) {
				fileLock = fileChannel.tryLock();
				if (fileLock != null) {
					break;
				} else {
					try {
						Thread.sleep(100);
					} catch (Exception e1) {
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void unlock() {
		try {
			fileLock.release();
			fileChannel.close();
			raf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
