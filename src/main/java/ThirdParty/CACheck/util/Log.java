package ThirdParty.CACheck.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ThirdParty.CACheck.cellarray.extract.CAResult;

public class Log {

	public static BufferedWriter writer = null;



	public static void logNewLine(BufferedWriter writer) {
		if(writer == null) return;

		System.out.println();
		try {
			writer.write("");
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void logNewLine(String str, BufferedWriter writer) {
		//FIXME：
		if (2 == 2) return;
		//
		if(writer == null) return;

		StringBuffer sb = new StringBuffer();
		sb.append("[");
		sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date()));
		sb.append("]  ");
		sb.append(str);

		System.out.println(sb.toString());
		try {
			writer.write(sb.toString());
			writer.newLine();
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void logNewLine(Exception e, BufferedWriter writer) {
		if(writer == null) return;

		logNewLine("", writer);
		e.printStackTrace(System.err);
		e.printStackTrace(new PrintWriter(writer));
	}

	public static void log(String str, BufferedWriter writer) {
		if(writer == null) return;

		System.out.print(str);

		try {
			writer.write(str);
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void log(List<CAResult> cars, BufferedWriter writer) {
		if(writer == null) return;

		for (CAResult car : cars) {
			log(car, writer);
		}

		logNewLine(writer);
	}

	public static void log(CAResult car, BufferedWriter writer) {
		if(writer == null) return;

		StringBuffer sb = new StringBuffer();
		sb.append(car.cellArray);
		if (car.isMissing) {
			sb.append(", missing");
		} 
		if (car.isInconsistent) {
			sb.append(", inconsistent");
		} 
		if (!car.isAmbiguous){
			sb.append(", correct");
		}

		if (car.isOpposite()) {
			sb.append(", opposite");
		}

		log(sb.toString(), writer);
		logNewLine(writer);
	}
}
