package clustering.bootstrappingClustering;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellReference;

import java.util.ArrayList;
import java.util.List;

class CellDependency {
	public Cell cell = null;
	List<CellReference> precedentCells = new ArrayList<CellReference>();

	CellDependency(Cell cell){
		this.cell = cell;
	}
	
	public Cell getCell() {
		return cell;
	}

	public void setCell(Cell cell) {
		this.cell = cell;
	}

	List<CellReference> getPrecedentCells() {
		return precedentCells;
	}

	@Override
	public String toString() {
		CellReference cr = new CellReference (cell.getRowIndex(),cell.getColumnIndex());
		String cellAddStr = cr.formatAsString();

		String precedentCellsStr = null;
		for (CellReference cellTemp: precedentCells){
			precedentCellsStr = "["+cellTemp.formatAsString()+"]"+precedentCellsStr;
		}
		return "CellDependency [cell=" + cellAddStr + ", precedentCells="
				+ precedentCellsStr + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cell == null) ? 0 : cell.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CellDependency other = (CellDependency) obj;
		CellReference cr = new CellReference (cell);
		CellReference crOther = new CellReference (other.cell);
		if (cell == null) {
			if (other.cell != null)
				return false;
		} else if (!cr.equals(crOther))
			return false;
		return true;
	}
}
