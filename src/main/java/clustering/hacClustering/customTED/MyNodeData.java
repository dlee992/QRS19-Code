package clustering.hacClustering.customTED;

import java.util.ArrayList;
import java.util.List;

public class MyNodeData {

    /*
     * TODO: label在表示cell时，表示其绝对引用位置；在表示其他时，就是本身的字符含义.
     * TODO: froms用来记录当isCell==true时,不同形式的单元格表达.
     */
    private String label;
    public boolean isCell = false;
    public String parentCell = "";
    public List<String> forms = new ArrayList<>();


    private MyNodeData(String label) {
        this.label = label;
    }


    public MyNodeData(String label, String parentCell) {
        this.label = label;
        this.parentCell = parentCell;
    }


    public String getLabel() {
        return this.label;
    }
}
