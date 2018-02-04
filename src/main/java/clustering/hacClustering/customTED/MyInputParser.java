package clustering.hacClustering.customTED;

import node.Node;
import util.FormatUtilities;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyInputParser<D> {

    public MyInputParser() { }

    /*
     * TODO: parentCell必须是绝对引用形式，不然后面的计算条件无法满足。
     */
    public Node<MyNodeData> fromString(String s, String parentCell) {
        //这一步只是去掉s字符串两端的多余字符。
        s = s.substring(s.indexOf("{"), s.lastIndexOf("}") + 1);

        Node<MyNodeData> node = new Node<>(new MyNodeData(FormatUtilities.getRoot(s), parentCell));

        Vector c = FormatUtilities.getChildren(s);

        /*
         * TODO: 这里要更新子节点的 parentCell
         * TODO: 一种是直接继承；另一种就是以当前节点(绝对位置)为以后的父节点
         */
        String updateParent = parentCell;

        String regex = "^R\\[?-?\\d*\\]?C\\[?-?\\d*\\]?$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(node.getNodeData().getLabel());

        //TODO: 如果this node是一个cell，那么需要为它计算4种单元格表现形式。
        //TODO: 但是如果这个根节点，那么它就没有所谓的四种表现形式，只有绝对形式。
        if (matcher.find()) {
            MyNodeData nodeData = node.getNodeData();
            nodeData.isCell = true;
            generateForms(nodeData, parentCell);
            updateParent = nodeData.forms.get(0);
        }

        for(int i = 0; i < c.size(); ++i) {
            node.addChild(fromString((String)c.elementAt(i), updateParent));
        }

        return node;
    }

    private void generateForms(MyNodeData nodeData, String parentCell) {
        String regex = "^(\\d+)";
        Matcher matcher = Pattern.compile(regex).matcher(parentCell);

        matcher.find();
        int parentRow = Integer.getInteger(matcher.group());
        matcher.find();
        int parentColumn = Integer.getInteger(matcher.group());

        //计算Row的相对和绝对
        int absoluteRow, relativeRow;


        //计算Column的相对和绝对
        int absoluteColumn, relativeColumn;


        nodeData.forms.add("R" + absoluteRow +"C" + absoluteColumn);
        nodeData.forms.add("R" + absoluteRow +"C[" + relativeColumn + "]");
        nodeData.forms.add("R[" + relativeRow +"]C" + absoluteColumn);
        nodeData.forms.add("R[" + relativeRow +"]C[" + relativeColumn + "]");
    }

    public static void main(String args[]) {
        //TODO：测试regular expression

    }
}
