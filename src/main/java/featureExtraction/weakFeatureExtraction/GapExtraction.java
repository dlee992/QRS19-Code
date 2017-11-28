package featureExtraction.weakFeatureExtraction;

import entity.Cluster;
import org.apache.poi.ss.usermodel.Cell;

import java.util.*;

public class GapExtraction {

	private Cluster cluster = null;
	private Map<Integer, Set<Cell>> columnSet = new HashMap<Integer, Set<Cell>>();
	private Map<Integer, Set<Cell>> rowSet = new HashMap<Integer, Set<Cell>>();
	
	public GapExtraction(Cluster cluster) {
		this.cluster = cluster;
	}

	public void indexSet() {

		List<Cell> cells = cluster.getClusterCells();

		Comparator<Cell> compareCol = new Comparator<Cell>() {
			@SuppressWarnings("Since15")
			public int compare(Cell o1, Cell o2) {
				return Integer.compare(o1.getColumnIndex(), o2.getColumnIndex());
			}
		};

		Comparator<Cell> compareRow = new Comparator<Cell>() {
			@SuppressWarnings("Since15")
			public int compare(Cell o1, Cell o2) {
				return Integer.compare(o1.getRowIndex(), o2.getRowIndex());
			}
		};

		for (Cell cell : cells) {
			int col = cell.getColumnIndex();
			int row = cell.getRowIndex();

			if (!columnSet.containsKey(col)) {
				columnSet.put(col, new TreeSet<Cell>(compareRow));
			}
			if (!rowSet.containsKey(row)) {
				rowSet.put(row, new TreeSet<Cell>(compareCol));
			}
			columnSet.get(col).add(cell);
			rowSet.get(row).add(cell);
		}
	}

	public Map<Cell, Gap> getFeature() {
		Map<Cell, Gap> gaps = new HashMap<Cell, Gap>();

		for (Map.Entry<Integer, Set<Cell>> col : columnSet.entrySet()) {

			Set<Cell> cells = col.getValue();
			Cell[] cellsList = new Cell[cells.size()];
			cells.toArray(cellsList);

			for (int i = 1; i < cellsList.length; i++) {
				int gapIndex;
				Cell currentCell = cellsList[i];
				Cell preCell = cellsList[i - 1];

				gapIndex = Math.abs(preCell.getRowIndex()
						- currentCell.getRowIndex());

				Gap gap;

				if (gapIndex != 0) {
					gap = new Gap(0, gapIndex, cluster);

					if (i == 1) {
						gaps.put(cellsList[0], gap);
					}
					gaps.put(currentCell, gap);
				}

			}

			for (Map.Entry<Integer, Set<Cell>> row : rowSet.entrySet()) {
				Set<Cell> cells1 = row.getValue();
				Cell[] cellsList1 = new Cell[cells1.size()];
				cells1.toArray(cellsList1);

				for (int i = 1; i < cellsList1.length; i++) {
					int gapIndex = 0;
					Cell currentCell = cellsList1[i];
					Cell preCell = cellsList1[i - 1];

					if (preCell != null) {
						gapIndex = Math.abs(preCell.getColumnIndex()
								- currentCell.getColumnIndex());
					}
					Gap gap;
					if (gapIndex != 0) {
						gap = new Gap(gapIndex, 0, cluster);
						if (i == 1) {
							gaps.put(cellsList1[0], gap);
						}
						gaps.put(currentCell, gap);
					}
				}
			}
		}
		return gaps;
	}

	public Set<Gap> cellGetGapFeature(Cell cell) {
		Set<Gap> gaps = new HashSet<Gap>();

		Set<Cell> sameColCells = columnSet.get(cell.getColumnIndex());

		int row = cell.getRowIndex();
		if (sameColCells != null && !sameColCells.isEmpty()) {
			Cell[] cellsList = new Cell[sameColCells.size()];
			sameColCells.toArray(cellsList);

			for (Cell aCellsList : cellsList) {
				int current = aCellsList.getRowIndex();
				Gap gapNext;

				int indexNext = Math.abs(current - row);
				gapNext = new Gap(0, indexNext, cluster);
				gaps.add(gapNext);
			}
		}
		Set<Cell> sameRowCells = rowSet.get(cell.getRowIndex());

		int col = cell.getColumnIndex();

		if (sameRowCells != null && !sameRowCells.isEmpty()) {

			Cell[] cellsList1 = new Cell[sameRowCells.size()];
			sameRowCells.toArray(cellsList1);

			for (Cell aCellsList1 : cellsList1) {
				int current = aCellsList1.getColumnIndex();
				Gap gapNext;

				int indexNext = Math.abs(current - col);
				gapNext = new Gap(indexNext, 0, cluster);
				gaps.add(gapNext);
			}
		}

		return gaps;
	}

}
