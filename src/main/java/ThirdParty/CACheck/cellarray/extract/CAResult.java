package ThirdParty.CACheck.cellarray.extract;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;

import ThirdParty.CACheck.CellArray;
import ThirdParty.CACheck.cellarray.inference.Constraints;

public class CAResult {
	public String excel = null;
	public String worksheet = null;
	public CellArray cellArray = null;

	public Constraints constraints = null;

	public boolean isSameRowOrColumn = false;
	public boolean isFullRowOrColumn = true;
	public boolean hasConstants = false;
	public boolean isOverlap = false;
	public boolean isAllOverlap = false;
	public int oppositeInputs = 0;

	public boolean isAmbiguous = false;
	public boolean isMissing = false;
	public boolean isInconsistent = false;
	public List<Object> pattern = null;
	public List<Cell> errorCells = new ArrayList<Cell>();
	public List<Cell> correctCells = new ArrayList<Cell>();
	public List<Cell> ambiguousCells = new ArrayList<Cell>();
	public List<Cell> missingCells = new ArrayList<Cell>();
	public List<Cell> smellyFormulaCells = new ArrayList<Cell>();
	public List<Cell> referChangedCells = new ArrayList<Cell>();

	public List<Cell> plainValueCells = new ArrayList<Cell>();
	public List<Cell> inconsistentFormulaCells = new ArrayList<Cell>();

	public float percentage = 1.0f;

	public boolean isFixInStage1 = false;
	
	public List<CAResult> compensateCAs = new ArrayList<CAResult>();

	public boolean isOpposite() {
		if (AmbiguousDetector.oppositeInputs(this, this.pattern, false) >= 3) {
			return true;
		} else if (AmbiguousDetector.oppositeInputs(this, this.pattern, true) > 1) {
			return true;
		}
		return false;
	}

	public boolean isFixInStage1() {
		if (isAmbiguous && isFixInStage1) {
			return true;
		}
		return false;
	}

	public String toString() {
		if (cellArray != null)
			return cellArray.toString();
		return null;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof CAResult) {
			return toString().equals(obj.toString());
		} else {
			return false;
		}
	}
}
