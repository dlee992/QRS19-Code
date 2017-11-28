package ThirdParty.CACheck.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import ThirdParty.CACheck.amcheck.ValidateCA;
import ThirdParty.CACheck.cellarray.extract.CAResult;

public class SmellyCellWriter {

	private static int index = 0;

	private static int categoryIndex = index++;

	private static int excelIndex = index++;

	private static int worksheetIndex = index++;

	private static int cellArrayIndex = index++;

	private static int cellIndex = index++;

	private static int missingIndex = index++;

	private static int inconsistentIndex = index++;

	public static File smellyCellFile = null;
	public static String category = null;

	public static void init(File smellyCellFile, String category, boolean isDir)
			throws Exception {

		SmellyCellWriter.smellyCellFile = smellyCellFile;
		SmellyCellWriter.category = category;

		ProcessLock lock = new ProcessLock();
		lock.lock();

		try {
			if (!smellyCellFile.exists()) {
				InputStream in = SmellyCellWriter.class.getClassLoader()
						.getResourceAsStream("resources/smellycells.xls");
				Workbook workbook = WorkbookFactory.create(in);
				workbook.write(new FileOutputStream(smellyCellFile));
				in.close();
				workbook.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		lock.unlock();
	}

	public static void addCAResult(List<CAResult> cars) {
		ProcessLock lock = new ProcessLock();
		lock.lock();

		try {
			Workbook workbook = WorkbookFactory.create(new FileInputStream(
					smellyCellFile));
			Sheet sheet = workbook.getSheetAt(0);

			for (int i = 0; i < cars.size(); i++) {
				CAResult car = cars.get(i);
				if (!ValidateCA.isTrue(car)) {
					continue;
				}

				for (Cell cell : car.ambiguousCells) {
					Row row = sheet.createRow(getLastRowNum(sheet) + 1);
					row.createCell(categoryIndex).setCellValue(category);
					row.createCell(excelIndex).setCellValue(car.excel);
					row.createCell(worksheetIndex).setCellValue(car.worksheet);
					row.createCell(cellArrayIndex).setCellValue(
							car.cellArray.toString());

					String str = cell.getRowIndex() + "-"
							+ cell.getColumnIndex();
					row.createCell(cellIndex).setCellValue(str);

					if (car.missingCells.contains(cell)) {
						row.createCell(missingIndex).setCellValue(1);
					}
					if (car.inconsistentFormulaCells.contains(cell)) {
						row.createCell(inconsistentIndex).setCellValue(1);
					}
				}
			}
			workbook.write(new FileOutputStream(smellyCellFile));
			workbook.close();
		} catch (Exception e) {
			Log.logNewLine(e, Log.writer);
		}

		lock.unlock();
	}

	public static int getLastRowNum(Sheet sheet) {
		// int lastRow = 0;
		for (int i = sheet.getLastRowNum(); i >= 0; i--) {
			Row row = sheet.getRow(i);

			if (row == null) {
				continue;
			} else {
				Cell testCell = row.getCell(categoryIndex);
				if (testCell == null || testCell.getStringCellValue() == null
						|| testCell.getStringCellValue().equals("")) {
					continue;
				} else {
					// lastRow = i;
					break;
				}
			}
		}
		return sheet.getLastRowNum();
	}
}
