package entity;

import org.apache.poi.ss.util.CellReference;

public class Smell {
	private CellReference cr = null;
	
	public boolean isMissingFormulaSmell = false;
	public boolean isDissimilarFormulaSmell = false;
	public boolean isDissimilarOperationSmell = false;
	public boolean isDissimilarCellReferenceSmell = false;
	public boolean isHardCodedConstantSmell = false;
	
	public Smell(CellReference cr){
		this.cr = cr;
	}

	@Override
	public String toString() {
		if (isMissingFormulaSmell)
			return "MissingFormula";
		else if (isDissimilarOperationSmell)
			return "DissimilarOperation";
		else if (isDissimilarCellReferenceSmell)
			return "DissimilarCellReference";
		else if (isHardCodedConstantSmell)
			return "HardCodedConstant";

		return null;
	}
	
	public CellReference getCr() {
		return cr;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cr == null) ? 0 : cr.hashCode());
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
		Smell other = (Smell) obj;
		if (cr == null) {
			if (other.cr != null)
				return false;
		} else if (!cr.equals(other.cr))
			return false;
		return true;
	}
}
