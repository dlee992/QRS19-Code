package extraction.weakFeatureExtraction;

public class CellArray {
	public int rowOrColumn;
	public boolean isRowCA;
	public int start;
	public int end;

	CellArray(int rowOrColumn, boolean isRowCA, int caStart, int caEnd) {
		this.rowOrColumn = rowOrColumn;
		this.isRowCA = isRowCA;
		this.start = caStart;
		this.end = caEnd;
	}
	
	@Override
	public String toString() {
		String ext;
		if (isRowCA) {
			ext = "CellArray=[" + rowOrColumn + ", " + start + "] ------ " + "["
					+ rowOrColumn + ", " + end + "]";
		} else {
			ext = "CellArray=[" + start + ", " + rowOrColumn + "] ------ " + "[" + end
					+ ", " + rowOrColumn + "]";
		}

		return ext;
	}
}
