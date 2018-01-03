package featureExtraction.weakFeatureExtraction;

import utility.BasicUtility;
import utility.FormulaParsing;
import entity.R1C1Cell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CellArrayExtraction {

	private Sheet sheet;
	private List<Snippet> snippets;
	private BasicUtility bu = new BasicUtility();
	
	public CellArrayExtraction(Sheet sheet, List<Snippet> snippets) {
		this.sheet = sheet;
		this.snippets = snippets;
	}


	public CellArrayExtraction() {
	}


	public List<CellArray> extractCellArray() {
		List<CellArray> cellArrays = new ArrayList<CellArray>();
		for (Snippet snippet : snippets) {
			List<CellArray> cas = extract(snippet);
			if (cas.size() > 0) {
				cellArrays.addAll(cas);
			}
		}

		postProcess2(cellArrays);
		return cellArrays;
	}


	private List<CellArray> extract(Snippet snippet) {
		List<CellArray> cas = new ArrayList<CellArray>();
		List<CellArray> rowCAs = extractRowCellArray(snippet);
		List<CellArray> colCAs = extractColumnCellArray(snippet);

		cas.addAll(rowCAs);
		cas.addAll(colCAs);

		postProcess1(cas);

		return cas;
	}

	private List<CellArray> extractColumnCellArray(Snippet snippet) {
		List<CellArray> cas = new ArrayList<CellArray>();

		for (int col = snippet.left; col <= snippet.right; col++) {
			int startRow = snippet.up;
			boolean possibleCA = false;
			for (int row = snippet.up; row <= snippet.bottom; row++) {
				Cell cell = sheet.getRow(row).getCell(col);
				if (cell != null && cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
					List<R1C1Cell> paras = bu.extractParameters(row, col, cell.getCellFormula());
					if (possibleMember(paras, false)) {
						possibleCA = true;
						continue;
					}

					if (row > startRow && possibleCA) {
						CellArray ext = new CellArray(col, false, startRow,
								row - 1);
						cas.add(ext);
					}

					startRow = row + 1;
					possibleCA = false;
				}
			}

			if (possibleCA) {
				CellArray ext = new CellArray(col, false, startRow,
						snippet.bottom);
				cas.add(ext);
			}
		}

		return cas;
	}

	private List<CellArray> extractRowCellArray(Snippet snippet) {
		List<CellArray> cas = new ArrayList<CellArray>();

		for (int row = snippet.up; row <= snippet.bottom; row++) {
			int startColumn = snippet.left;
			boolean possibleCA = false;
			for (int col = snippet.left; col <= snippet.right; col++) {
				Cell cell = sheet.getRow(row).getCell(col);
				if (cell != null && cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
					//
					List<R1C1Cell> paras = bu.extractParameters(row, col, cell.getCellFormula());
					if (possibleMember(paras, true)) {
						possibleCA = true;
						continue;
					}

					if (col > startColumn && possibleCA) {
						CellArray ca = new CellArray(row, true, startColumn, col - 1);
						cas.add(ca);
					}

					startColumn = col + 1;
					possibleCA = false;
				}
			}

			if (possibleCA) {
				CellArray ext = new CellArray(row, true, startColumn,
						snippet.right);
				cas.add(ext);
			}
		}

		return cas;
	}

	private boolean possibleMember(List<R1C1Cell> paras, boolean rowCA) {
		// no inputs, means just some computation on data.
		if (paras.size() == 0) {
			return false;
		}

		boolean possibleMember = true;
		for (R1C1Cell para : paras) {
			if (para.rowRelative || para.columnRelative) {
				if (rowCA) {
					if (para.column != para.curColumn) {
						possibleMember = false;
						break;
					}
				} else {
					if (para.row != para.curRow) {
						possibleMember = false;
						break;
					}
				}
			} else {
				possibleMember = false;
				break;
			}
		}

		return possibleMember;
	}


	private void postProcess1(List<CellArray> cas) {
		for (int i = 0; i < cas.size();) {
			CellArray ca = cas.get(i);
			if (ca.end - ca.start < 1) {
				cas.remove(i);
				continue;
			}

			int start = ca.start;
			boolean separated = false;
			for (int j = ca.start; j <= ca.end; j++) {
				Cell cell;
				if (ca.isRowCA) {
					cell = sheet.getRow(ca.rowOrColumn).getCell(j);
				} else {
					cell = sheet.getRow(j).getCell(ca.rowOrColumn);
				}

				if (cell == null || cell.getCellType() == Cell.CELL_TYPE_BLANK) {
					if (j - start <= 1) {
						ca.start = j + 1;
					} else {
						CellArray temp;
						if (ca.isRowCA) {
							temp = new CellArray(ca.rowOrColumn, true, start,
									j - 1);
						} else {
							temp = new CellArray(ca.rowOrColumn, false, start,
									j - 1);
						}
						cas.add(temp);
						separated = true;
					}
					start = j + 1;
				}
			}
			if (separated || ca.end - ca.start < 1) {
				cas.remove(i);
			} else {
				i++;
			}
		}
	}

	private void postProcess2(List<CellArray> cas) {
		// delete the extension which have extra worksheet references.
		// delete the extension which have IF.
		// delete the extension which have round
		// delete the extension which have absolute address
		// delete the extension which have string
		
		for (int i = 0; i < cas.size(); i++) {
			CellArray ca = cas.get(i);
			for (int j = ca.start; j <= ca.end; j++) {
				Cell cell;
				if (ca.isRowCA) {
					cell = sheet.getRow(ca.rowOrColumn).getCell(j);
				} else {
					cell = sheet.getRow(j).getCell(ca.rowOrColumn);
				}

				boolean deleted = false;
				if (cell != null
						&& cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
					String formula = cell.getCellFormula();
					if (formula.contains("!")
							|| formula.toLowerCase().contains("if")
							|| formula.toLowerCase().contains("round")
							|| formula.toLowerCase().contains("$")
							|| formula.toLowerCase().contains("max")
							|| formula.toLowerCase().contains("min")) {
						deleted = true;
					}
				}


                try {
                    Set<R1C1Cell> inputs = new FormulaParsing().generateInputs(sheet, ca);
                    for (R1C1Cell input : inputs) {
						assert cell != null;
						bu.getNumericalValue(sheet, input,
                                cell.getRowIndex(), cell.getColumnIndex());
                    }
                    bu.getNumericalValue(cell);
                } catch (Exception e) {
                    deleted = true;
                    //System.out.println("       --------deleted = true-----");
                }

				if (deleted) {
					cas.remove(ca);
					i--;
					break;
				}
			}
		}
	}


	public CellArray getFeature(Cell cell, List<CellArray> allCAs) {
		for (CellArray ca: allCAs){
			int col = cell.getColumnIndex();
			int row = cell.getRowIndex();
			
			for (int i = ca.start; i <= ca.end; i++) {
				if (ca.isRowCA) {
					if (row == ca.rowOrColumn && col == i)
						return ca;
				} else {
					if (col == ca.rowOrColumn && row == i)
						return ca;
				}
			}
		}
		
		return null;
	}
}
