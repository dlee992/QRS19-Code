package thirdparty.CACheck.util.analysis;


public class CA {
	public String category = null;
	public String excel = null;
	public String worksheet = null;
	public String cellArray = null;

	public boolean isSameRowOrColumn = false;
	public int oppositeInputs = 0;
	
	public int TP = 2; // default 2 means, it is not set.

	public boolean isAmbiguous = false;
	public boolean isMissing = false;
	public boolean isInconsistent = false;

	public int missingCells = 0;
	public int smellyFormulaCells = 0;
	public int errorCells = 0;
	public float percentage = 0;
	public int falseRepair = 0;
	
	public boolean hasConstants = false;
	public boolean referChanged = false;
}