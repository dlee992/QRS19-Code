package extraction.weakFeatureExtraction;

import org.apache.poi.ss.usermodel.Cell;

public class Header {
	
	private Cell cell;

	private String strHeader;
	private int level;
	private int orientation;
	
	Header(Cell cell, String strHeader, int orientation, int level){
		this.cell = cell;
		this.strHeader = strHeader;
		this.orientation = orientation;
		this.level = level;
	}

	public int getOrientation() {
		return orientation;
	}

	public String getStrHeader() {
		return strHeader;
	}

	@Override
	public String toString() {
		return "Header [strHeader=" + strHeader + ", orientation="
				+ orientation + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + orientation;
		result = prime * result + level;
		result = prime * result
				+ ((strHeader == null) ? 0 : strHeader.hashCode());
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
		Header other = (Header) obj;
		if (orientation != other.orientation)
			return false;
		if (level != other.level)
			return false;
		if (strHeader == null) {
			if (other.strHeader != null)
				return false;
		} else if (!strHeader.equals(other.strHeader))
			return false;
		return true;
	}

	public int getLevel() {
		return level;
	}

	public Cell getCell() {
		return cell;
	}
}
