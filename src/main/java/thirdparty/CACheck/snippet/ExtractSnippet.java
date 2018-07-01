package thirdparty.CACheck.snippet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;

import thirdparty.CACheck.AMSheet;

public class  ExtractSnippet {

	private AMSheet sheet = null;

	public ExtractSnippet(AMSheet sheet) {
		this.sheet = sheet;
	}

	public List<Snippet> extractSnippet() throws IOException {
		Snippet all = new Snippet(0, 0, sheet.getColumnNum() - 1,
				sheet.getRowNum() - 1);
		List<Snippet> snippets = new ArrayList<Snippet>();
		snippets.add(all);

		snippets = extract(snippets);

		snippets = extract(snippets);

		snippets = extract(snippets);

		snippets = extract(snippets);

		// remove the duplicated
		Snippet preSnippet = null;
		for (int i = 0; i < snippets.size(); i++) {
			Snippet curSnippet = snippets.get(i);
			if (preSnippet != null) {
				if (curSnippet.equals(preSnippet)) {
					snippets.remove(i);
					i--;
				} else {
					preSnippet = curSnippet;
				}
			} else {
				preSnippet = curSnippet;
			}
		}

		return snippets;
	}

	public List<Snippet> extract(List<Snippet> initSnippets) throws IOException {
		List<Snippet> snippets = new ArrayList<Snippet>();

		for (Snippet snippet : initSnippets) {
			if (sheet.noLabel(snippet)) {
				snippets.add(snippet);
				continue;
			}
			
			int rowNum = snippet.bottom + 1;
			int columnNum = snippet.right + 1;

			int startRow = snippet.up;
			int stopRow = snippet.bottom;
			while (startRow < rowNum) {
				while (startRow < rowNum) {
					if (sheet.isFence(startRow, true, snippet)) {
						startRow++;
					} else {
						break;
					}
				}
				stopRow = startRow;
				while (stopRow < rowNum) {
					if (!sheet.isFence(stopRow, true, snippet)) {
						stopRow++;
					} else {
						break;
					}
				}

				if (startRow < rowNum) {
					int startColumn = snippet.left;
					int stopColumn = snippet.right;

					while (startColumn < columnNum) {
						while (startColumn < columnNum) {
							if (sheet.isFence(startColumn, false, snippet)) {
								startColumn++;
							} else {
								break;
							}
						}
						stopColumn = startColumn;
						while (stopColumn < columnNum) {
							if (!sheet.isFence(stopColumn, false, snippet)) {
								stopColumn++;
							} else {
								break;
							}
						}

						if (stopColumn != startColumn) {
							Snippet tmp = new Snippet();
							tmp.left = startColumn;
							tmp.right = stopColumn - 1;
							tmp.up = startRow;
							tmp.bottom = stopRow - 1;

							// delete the just one cell data region.
							if (tmp.left != tmp.right || tmp.up != tmp.bottom) {
								snippets.add(tmp);
							}
						}

						startColumn = stopColumn;
					}
				}

				startRow = stopRow;
			}
		}
		return snippets;
	}

	public void mark(List<Snippet> snippets) {
		CellStyle style = sheet.getSheet().getWorkbook().createCellStyle();
		style.setFillBackgroundColor(IndexedColors.DARK_YELLOW.getIndex());
		style.setFillForegroundColor(IndexedColors.DARK_YELLOW.getIndex());
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

		for (Snippet snippet : snippets) {
			for (int i = snippet.up; i <= snippet.bottom; i++) {
				Row row = sheet.getSheet().getRow(i);
				for (int j = snippet.left; j <= snippet.right; j++) {
					Cell cell = row.getCell(j);
					if (cell != null) {
						cell.setCellStyle(style);
					}
				}
			}
		}
	}
}
