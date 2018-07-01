package extraction.weakFeatureExtraction;

import org.apache.poi.ss.util.CellReference;

import java.util.List;

public class Alliance {

	private String allianceName;
	private List<CellReference> precedents;
	
	Alliance(String formulaString) {
		this.setAllianceName(formulaString);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((allianceName == null) ? 0 : allianceName.hashCode());
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
		Alliance other = (Alliance) obj;
		if (allianceName == null) {
			if (other.allianceName != null)
				return false;
		} else if (!allianceName.equals(other.allianceName))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "Alliance [allianceName=" + allianceName + "]";
	}

	private void setAllianceName(String allianceName) {
		this.allianceName = allianceName;
	}

	List<CellReference> getPrecedents() {
		return precedents;
	}

	void setPrecedents(List<CellReference> precedents) {
		this.precedents = precedents;
	}
}
