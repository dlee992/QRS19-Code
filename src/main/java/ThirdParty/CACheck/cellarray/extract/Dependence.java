package ThirdParty.CACheck.cellarray.extract;

import java.util.List;

import org.apache.poi.ss.usermodel.Cell;

import ThirdParty.CACheck.R1C1Cell;

public class Dependence {
	public Cell cell;
	public List<R1C1Cell> dependedCells;

	public Dependence(Cell cell, List<R1C1Cell> dependedCells) {
		this.cell = cell;
		this.dependedCells = dependedCells;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Cell[" + cell.getRowIndex() + "," + cell.getColumnIndex()
				+ "]");
		sb.append("-->");

		for (R1C1Cell tmp : dependedCells) {
			sb.append(tmp + ",");
		}

		sb.deleteCharAt(sb.length() - 1);
		sb.append(";");

		return sb.toString();
	}

	private String dependCells() {
		StringBuffer sb = new StringBuffer();

		for (R1C1Cell tmp : dependedCells) {
			sb.append(tmp + ", ");
		}

		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Dependence) {
			return ((Dependence) obj).dependCells().equals(dependCells());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		// Right hash.
		// return dependCells().hashCode();

		// Here, I don't want the hash.
		return super.hashCode();
	}
}
