package thirdparty.CACheck.cellarray.synthesis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import thirdparty.CACheck.cellarray.extract.CAResult;
import thirdparty.CACheck.util.ProcessLock;
import thirdparty.CACheck.util.TotalResultWriter;
import thirdparty.CACheck.util.Utils;

public class PatternCache {

	public static List<Object> getCachedPattern(CAResult car) {
		ProcessLock pl = new ProcessLock();
		pl.lock();

		Map<String, List<Object>> patterns = restorePatterns();
		List<Object> pattern = patterns.get(constructKey(car));

		pl.unlock();
		return pattern;
	}

	public static void cachePattern(CAResult car, List<Object> pattern) {
		ProcessLock pl = new ProcessLock();
		pl.lock();

		Map<String, List<Object>> patterns = restorePatterns();
		patterns.put(constructKey(car), pattern);
		storePatterns(patterns);

		pl.unlock();
	}

	private static String constructKey(CAResult car) {
		StringBuffer sb = new StringBuffer();
		sb.append(TotalResultWriter.category);
		sb.append("--");
		sb.append(car.excel);
		sb.append("--");
		sb.append(car.worksheet);
		sb.append("--");
		sb.append(car.cellArray.toString());

		return sb.toString();
	}

	// initialize
	private static File cacheDir = new File(Utils.exprDir() + "tools/cache/");
	private static File cache = new File(Utils.exprDir() + "tools/cache/"
			+ TotalResultWriter.category + ".ser");
	static {
		ProcessLock pl = new ProcessLock();
		pl.lock();

		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		if (!cache.exists()) {
			try {
				cache.createNewFile();
			} catch (Exception e) {
			}
		}

		pl.unlock();
	}

	@SuppressWarnings("unchecked")
	private static Map<String, List<Object>> restorePatterns() {
		Map<String, List<Object>> patterns = null;
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream(cache));
			patterns = (Map<String, List<Object>>) ois.readObject();
		} catch (Exception e) {
			patterns = new HashMap<String, List<Object>>();
		} finally {
			try {
				ois.close();
			} catch (Exception e) {
			}
		}
		return patterns;
	}

	private static void storePatterns(Map<String, List<Object>> patterns) {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream(cache));
			oos.writeObject(patterns);
		} catch (Exception e) {
		} finally {
			try {
				oos.flush();
				oos.close();
			} catch (Exception e) {
			}
		}
	}
}
