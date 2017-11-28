package featureExtraction.weakFeatureExtraction;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.List;

public class SnippetExtraction {

	private Sheet sheet = null;
	private int lastColumnIndex = 0;
	
	public SnippetExtraction(Sheet sheet) {
		this.sheet = sheet;
		for (Row row : sheet) {
			if (lastColumnIndex < row.getLastCellNum()-1) {
				lastColumnIndex = row.getLastCellNum()-1;
			}
		}
	}

	public List<Snippet> extractSnippet() {
		Snippet all = new Snippet(0, 0, lastColumnIndex, sheet.getLastRowNum());
		List<Snippet> snippets = new ArrayList<Snippet>();
		snippets.add(all);
		
		/* Why four times? How to explain it? */
		snippets = extract(snippets);

		snippets = extract(snippets);

		snippets = extract(snippets);

		snippets = extract(snippets);
	
		return snippets;
	}

	private List<Snippet> extract(List<Snippet> initSnippets) {
		List<Snippet> snippets = new ArrayList<Snippet>();

		for (Snippet snippet : initSnippets) {
			int rowNum = snippet.bottom + 1;
			int columnNum = snippet.right + 1;

			int startRow = snippet.up;
			int stopRow;
			while (startRow < rowNum) {
				while (startRow < rowNum) {
					if (isFence(startRow, true, snippet)) {
						startRow++;
					} else {
						break;
					}
				}
				stopRow = startRow;
				while (stopRow < rowNum) {
					if (!isFence(stopRow, true, snippet)) {
						stopRow++;
					} else {
						break;
					}
				}

				if (startRow < rowNum) {
					int startColumn = snippet.left;
					int stopColumn;

					while (startColumn < columnNum) {
						while (startColumn < columnNum) {
							if (isFence(startColumn, false, snippet)) {
								startColumn++;
							} else {
								break;
							}
						}
						stopColumn = startColumn;
						while (stopColumn < columnNum) {
							if (!isFence(stopColumn, false, snippet)) {
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

	private boolean isFence(int num, boolean isRow, Snippet dr) {
		if (isRow) {
			boolean fence = true;
			Row row = sheet.getRow(num);
			if (row != null) {
				for (int col = dr.left; col <= dr.right; col++) {
					Cell cell = row.getCell(col, MissingCellPolicy.RETURN_BLANK_AS_NULL);
					if (cell != null) {
						int cellType = cell.getCellType();
						
						if (!(cellType == Cell.CELL_TYPE_BLANK || cellType == Cell.CELL_TYPE_STRING)) {
							fence = false;
							break;
						}
					}
				}
			}
			return fence;
		} else {
			boolean fence = true;
			for (int rowIndex = dr.up; rowIndex <= dr.bottom; rowIndex++) {
				Row row = sheet.getRow(rowIndex);
				if (row != null) {
					Cell cell = row.getCell(num);
					if (cell != null) {
						int cellType = cell.getCellType();
						
						if (!(cellType == Cell.CELL_TYPE_BLANK || cellType == Cell.CELL_TYPE_STRING)) {
							fence = false;
							break;
						}
					}
				}
			}
			return fence;
		}
	}

	public Snippet cellSnippetBelong(Cell cell, List<Snippet> snippets) {
		for (Snippet snippet: snippets){
			List<Cell> cells = new ArrayList<Cell>();
			
			for (int i = snippet.up; i <= snippet.bottom; i++) {
				Row row = sheet.getRow(i);
				for (int j = snippet.left; j <= snippet.right; j++) {
					Cell tmpCell = row.getCell(j);
					if (tmpCell != null) {
						cells.add(tmpCell);
					}
				}
			}
			
			for (Cell cellCompare : cells){
				if (cell.getColumnIndex() == cellCompare.getColumnIndex() &&
						cell.getRowIndex() == cellCompare.getRowIndex()){
					return snippet;
				}
			}
		}
		return null;
	}
}
