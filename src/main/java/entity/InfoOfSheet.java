package entity;

import org.apache.poi.ss.usermodel.Cell;

import java.util.List;
import java.util.Map;

/**
 * Created by lida on 2016/11/2.
 */
public class InfoOfSheet {
    private Map<String, List<String>> formulaMap;
    private List<Cell> dataCells;

    public InfoOfSheet(Map<String, List<String>> formulaMap, List<Cell> dataCells) {
        this.formulaMap = formulaMap;
        this.dataCells    = dataCells;
    }

    public Map<String, List<String>> getFormulaMap() {
        return formulaMap;
    }

    public List<Cell> getDataCells() {
        return dataCells;
    }
}
