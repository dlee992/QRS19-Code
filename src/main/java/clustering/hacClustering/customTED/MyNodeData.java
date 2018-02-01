package clustering.hacClustering.customTED;

import node.StringNodeData;

import java.util.ArrayList;
import java.util.List;

public class MyNodeData extends StringNodeData {

    //TODO: froms用来记录当isCell==true时,不同形式的单元格表达.
    public boolean isCell = false;
    public String parentCell = "";
    public List<String> forms = new ArrayList<>();

    public MyNodeData(String label) {
        super(label);
    }
}
