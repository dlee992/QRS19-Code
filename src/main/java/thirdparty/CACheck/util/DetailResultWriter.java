package thirdparty.CACheck.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import thirdparty.CACheck.cellarray.extract.CAResult;
import thirdparty.CACheck.util.analysis.CA;

public class DetailResultWriter {

	private static int index = 0;

	private static int categoryIndex = index++;

	private static int excelIndex = index++;

	private static int worksheetIndex = index++;

	private static int cellArrayIndex = index++;

	private static int allOverlapIndex = index;

	private static int sameCellArrayIndex = index++;

	private static int oppositeCellArrayIndex = index++;

	private static int TPIndex = index;

	private static int missingIndex = index + 1;

	private static int inconsistentIndex = index + 2;

	private static int missingCellIndex = index + 5;
	private static int smellyFormulaCellIndex = index + 6;
	private static int numOfErrorIndex = index + 7;

	private static int percentageIndex = index + 11;
	private static int falseRepairIndex = index + 12;

	private static int constantIndex = index + 16;
	private static int constantChangeIndex = index + 17;

	public static File detailFile = null;
	public static String category = null;

	public static void init(File detailFile, String category, boolean isDir)
			throws Exception {

		DetailResultWriter.detailFile = detailFile;
		DetailResultWriter.category = category;

		ProcessLock lock = new ProcessLock();
		lock.lock();

		try {
			// initialize the file when we start to handle a new category.
			if (isDir) {
				if (detailFile.exists()) {
					detailFile.delete();
				}
				InputStream in = DetailResultWriter.class.getClassLoader()
						.getResourceAsStream("resources/details.xls");
				Workbook workbook = WorkbookFactory.create(in);
				workbook.write(new FileOutputStream(detailFile));
				in.close();
				workbook.close();
			}
		} catch (Exception e) {
		}

		lock.unlock();
	}

	public static void addCAResult(List<CAResult> cars) {
		ProcessLock lock = new ProcessLock();
		lock.lock();

		try {
			Workbook workbook = WorkbookFactory.create(new FileInputStream(
					detailFile));

			for (int i = 0; i < cars.size(); i++) {
				CAResult car = cars.get(i);

				Row row = getRow(workbook, car.isAmbiguous, car.percentage);

				row.createCell(categoryIndex).setCellValue(category);
				row.createCell(excelIndex).setCellValue(car.excel);
				row.createCell(worksheetIndex).setCellValue(car.worksheet);
				row.createCell(cellArrayIndex).setCellValue(
						car.cellArray.toString());
				if (car.isSameRowOrColumn) {
					row.createCell(sameCellArrayIndex).setCellValue(1);
				}
				if (car.oppositeInputs > 0) {
					row.createCell(oppositeCellArrayIndex).setCellValue(
							car.oppositeInputs);
				}
				if (car.isMissing) {
					row.createCell(missingIndex).setCellValue(1);
				}
				if (car.isInconsistent) {
					row.createCell(inconsistentIndex).setCellValue(1);
				}
				if (car.missingCells.size() != 0) {
					row.createCell(missingCellIndex).setCellValue(
							car.missingCells.size());
				}
				if (car.smellyFormulaCells.size() != 0) {
					row.createCell(smellyFormulaCellIndex).setCellValue(
							car.smellyFormulaCells.size());
				}
				if (car.errorCells.size() != 0) {
					row.createCell(numOfErrorIndex).setCellValue(
							car.errorCells.size());
				}
				row.createCell(percentageIndex).setCellValue(car.percentage);

				if (car.hasConstants) {
					row.createCell(constantIndex).setCellValue(1);
				}
				if (car.referChangedCells.size() > 0) {
					row.createCell(constantChangeIndex).setCellValue(1);
				}
			}
			workbook.write(new FileOutputStream(detailFile));
			workbook.close();
		} catch (Exception e) {
			Log.logNewLine(e, Log.writer);
		}

		lock.unlock();
	}

	public static void addCAResult2(List<CA> cas) {
		ProcessLock lock = new ProcessLock();
		lock.lock();

		try {
			Workbook workbook = WorkbookFactory.create(new FileInputStream(
					detailFile));

			for (int i = 0; i < cas.size(); i++) {
				CA ca = cas.get(i);

				Row row = getRow(workbook, ca.isAmbiguous, ca.percentage);

				row.createCell(categoryIndex).setCellValue(category);
				row.createCell(excelIndex).setCellValue(ca.excel);
				row.createCell(worksheetIndex).setCellValue(ca.worksheet);
				row.createCell(cellArrayIndex).setCellValue(ca.cellArray);
				if (ca.isSameRowOrColumn) {
					row.createCell(sameCellArrayIndex).setCellValue(1);
				}
				if (ca.oppositeInputs > 0) {
					row.createCell(oppositeCellArrayIndex).setCellValue(
							ca.oppositeInputs);
				}
				if (ca.TP != 2) {
					row.createCell(TPIndex).setCellValue(ca.TP);
				}
				if (ca.isMissing) {
					row.createCell(missingIndex).setCellValue(1);
				}
				if (ca.isInconsistent) {
					row.createCell(inconsistentIndex).setCellValue(1);
				}
				if (ca.missingCells != 0) {
					row.createCell(missingCellIndex).setCellValue(
							ca.missingCells);
				}
				if (ca.smellyFormulaCells != 0) {
					row.createCell(smellyFormulaCellIndex).setCellValue(
							ca.smellyFormulaCells);
				}
				if (ca.errorCells != 0) {
					row.createCell(numOfErrorIndex).setCellValue(ca.errorCells);
				}
				row.createCell(percentageIndex).setCellValue(ca.percentage);

				if (ca.falseRepair != 0) {
					row.createCell(falseRepairIndex).setCellValue(
							ca.falseRepair);
				}

				if (ca.hasConstants) {
					row.createCell(constantIndex).setCellValue(1);
				}
				if (ca.referChanged) {
					row.createCell(constantChangeIndex).setCellValue(1);
				}
			}
			workbook.write(new FileOutputStream(detailFile));
			workbook.close();
		} catch (Exception e) {
			Log.logNewLine(e, Log.writer);
		}

		lock.unlock();
	}

	public static List<CA> readSmells(int sheetNum) {
		return readSmells(sheetNum, Integer.MAX_VALUE);
	}

	public static List<CA> readSmells(int sheetNum, int bigRow) {
		ProcessLock lock = new ProcessLock();
		lock.lock();

		List<CA> cas = new ArrayList<CA>();
		int i = 2;
		try {
			Workbook workbook = WorkbookFactory.create(new FileInputStream(
					detailFile));
			Sheet sheet = workbook.getSheetAt(sheetNum);

			for (; i <= sheet.getLastRowNum() && i < bigRow; i++) {
				Row row = sheet.getRow(i);
				CA ca = new CA();

				if (row == null) {
					break;
				}
				Cell testCell = row.getCell(categoryIndex);
				if (testCell == null || testCell.getStringCellValue() == null
						|| testCell.getStringCellValue().equals("")) {
					break;
				}

				ca.category = row.getCell(categoryIndex).getStringCellValue();
				ca.excel = row.getCell(excelIndex).getStringCellValue();
				ca.worksheet = row.getCell(worksheetIndex).getStringCellValue();
				ca.cellArray = row.getCell(cellArrayIndex).getStringCellValue();
				if (row.getCell(sameCellArrayIndex) != null) {
					ca.isSameRowOrColumn = true;
				}
				if (row.getCell(oppositeCellArrayIndex) != null) {
					ca.oppositeInputs = (int) row.getCell(
							oppositeCellArrayIndex).getNumericCellValue();
				}
				if (row.getCell(TPIndex) != null) {
					ca.TP = (int) row.getCell(TPIndex).getNumericCellValue();
				}
				if (row.getCell(missingIndex) != null) {
					ca.isMissing = true;
				}
				if (row.getCell(inconsistentIndex) != null) {
					ca.isInconsistent = true;
				}

				if (row.getCell(missingCellIndex) != null) {
					ca.missingCells = (int) row.getCell(missingCellIndex)
							.getNumericCellValue();
				}
				if (row.getCell(smellyFormulaCellIndex) != null) {
					ca.smellyFormulaCells = (int) row.getCell(
							smellyFormulaCellIndex).getNumericCellValue();
				}
				if (row.getCell(numOfErrorIndex) != null) {
					ca.errorCells = (int) row.getCell(numOfErrorIndex)
							.getNumericCellValue();
				}

				if (row.getCell(percentageIndex) != null) {
					ca.percentage = (float) row.getCell(percentageIndex)
							.getNumericCellValue();
				} else {
					ca.percentage = 0.00001f;
				}

				if (row.getCell(falseRepairIndex) != null) {
					ca.falseRepair = (int) row.getCell(falseRepairIndex)
							.getNumericCellValue();
				}

				if (row.getCell(constantIndex) != null) {
					ca.hasConstants = true;
				}
				if (row.getCell(constantChangeIndex) != null) {
					ca.referChanged = true;
				}

				cas.add(ca);
			}
			workbook.close();
		} catch (Exception e) {
			System.out.println("Details: " + detailFile.getName());
			System.out.println("Sheet" + sheetNum);
			System.out.println("Row" + i);
			e.printStackTrace();
		}

		lock.unlock();
		return cas;
	}

	private static Row getRow(Workbook workbook, boolean isAmbiguous,
			float percent) {
		if (isAmbiguous == false) {
			Sheet sheet = workbook.getSheetAt(0);
			Row row = sheet.createRow(getLastRowNum(sheet) + 1);
			return row;
		} else if (percent >= 1.0) {
			Sheet sheet = workbook.getSheetAt(1);
			Row row = sheet.createRow(getLastRowNum(sheet) + 1);
			return row;
		} else if (percent >= 0.9 && percent < 1.0) {
			Sheet sheet = workbook.getSheetAt(2);
			Row row = sheet.createRow(getLastRowNum(sheet) + 1);
			return row;
		} else if (percent >= 0.8 && percent < 0.9) {
			Sheet sheet = workbook.getSheetAt(3);
			Row row = sheet.createRow(getLastRowNum(sheet) + 1);
			return row;
		} else if (percent >= 0.7 && percent < 0.8) {
			Sheet sheet = workbook.getSheetAt(4);
			Row row = sheet.createRow(getLastRowNum(sheet) + 1);
			return row;
		} else if (percent >= 0.6 && percent < 0.7) {
			Sheet sheet = workbook.getSheetAt(5);
			Row row = sheet.createRow(getLastRowNum(sheet) + 1);
			return row;
		} else if (percent >= 0.5 && percent < 0.6) {
			Sheet sheet = workbook.getSheetAt(6);
			Row row = sheet.createRow(getLastRowNum(sheet) + 1);
			return row;
		} else {
			Sheet sheet = workbook.getSheetAt(7);
			Row row = sheet.createRow(getLastRowNum(sheet) + 1);
			return row;
		}
	}

	public static void addOverlaps(List<CAResult> cars) {
		List<CAResult> overlaps = new ArrayList<CAResult>();
		for (CAResult car : cars) {
			if (car.isOverlap) {
				overlaps.add(car);
			}
		}

		if (overlaps.size() > 0) {

			ProcessLock lock = new ProcessLock();
			lock.lock();

			try {
				Workbook workbook = WorkbookFactory.create(new FileInputStream(
						detailFile));
				Sheet sheet = workbook.getSheetAt(8);

				for (int i = 0; i < overlaps.size(); i++) {
					CAResult car = overlaps.get(i);

					Row row = sheet.createRow(getLastRowNum(sheet) + 1);

					row.createCell(categoryIndex).setCellValue(category);
					row.createCell(excelIndex).setCellValue(car.excel);
					row.createCell(worksheetIndex).setCellValue(car.worksheet);
					row.createCell(cellArrayIndex).setCellValue(
							car.cellArray.toString());
					if (car.isAllOverlap) {
						row.createCell(allOverlapIndex).setCellValue(1);
					}
				}
				workbook.write(new FileOutputStream(detailFile));
				workbook.close();
			} catch (Exception e) {
				Log.logNewLine(e, Log.writer);
			}

			lock.unlock();
		}
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
