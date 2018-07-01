package extraction.weakFeatureExtraction;

import org.apache.poi.ss.usermodel.Cell;

/**
 * Author : Da Li
 * Created data : 十一月 19, 2016
 * Package Name: ${PACKAGE_NAME}
 */
public class SpecialHeader {
	public Cell cell;
	private String strHeader;
	private int orientation;

	public SpecialHeader(String strHeader, int orientation){
		this.strHeader = "total/gross";
		this.orientation = orientation;
	}

	@Override
	public String toString() {
		return "Header [strHeader=" + strHeader + ", orientation=" + orientation + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + orientation;
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
		SpecialHeader other = (SpecialHeader) obj;
		if (orientation != other.orientation)
			return false;
		if (strHeader == null) {
			if (other.strHeader != null)
				return false;
		} else if (!strHeader.equals(other.strHeader))
			return false;
		return true;
	}
}
