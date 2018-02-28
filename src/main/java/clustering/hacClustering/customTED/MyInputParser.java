package clustering.hacClustering.customTED;

import node.Node;
import parser.InputParser;
import util.FormatUtilities;

import java.util.Vector;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyInputParser<D> {

    private Logger logger = Logger.getLogger("MyInputParser");

    public MyInputParser() { }

    /*
     * TODO: parentCell必须是绝对引用形式，不然后面的计算条件无法满足。
     */
    public Node<MyNodeData> fromString(String s, String parentCell) {
//        logger.info("parentCell = " + parentCell);

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


        String regex = "(\\d+)";
        Matcher matcher = Pattern.compile(regex).matcher(parentCell);

        matcher.find();
        int parentRow = Integer.parseInt(matcher.group());
        matcher.find();
        int parentColumn = Integer.parseInt(matcher.group());

        //TODO: 有一种可能是，如果RC[1]或者R[1]C，那么就缺少一个数字。
        String regex2 = "(-?\\d+)";
        matcher = Pattern.compile(regex2).matcher(nodeData.label);

        int currentRow, currentColumn;
        char c = nodeData.label.charAt(1);
        if (c == 'C') {
            currentRow = 0;
        }
        else {
            matcher.find();
            currentRow = Integer.parseInt(matcher.group());
        }

        int indexC = nodeData.label.indexOf('C');
        if (indexC == nodeData.label.length()-1) {
            currentColumn = 0;
        }
        else {
            matcher.find();
            currentColumn = Integer.parseInt(matcher.group());
        }

        //计算Row的相对和绝对
        int absoluteRow, relativeRow;
        c = nodeData.label.charAt(1);
        if (c >= '0' && c <= '9') {
            absoluteRow = currentRow;
            relativeRow = absoluteRow - parentRow;
        }
        else {
            relativeRow = currentRow;
            absoluteRow = parentRow + relativeRow;
        }

        //计算Column的相对和绝对
        int absoluteColumn, relativeColumn;

        if (indexC == nodeData.label.length()-1 || nodeData.label.charAt(indexC+1) == '[') {
            relativeColumn = currentColumn;
            absoluteColumn = parentColumn + relativeColumn;
        }
        else {
            absoluteColumn = currentColumn;
            relativeColumn = absoluteColumn - parentColumn;
        }

        nodeData.forms.add("R" + absoluteRow +"C" + absoluteColumn);
        nodeData.forms.add("R" + absoluteRow +"C[" + relativeColumn + "]");
        nodeData.forms.add("R[" + relativeRow +"]C" + absoluteColumn);
        nodeData.forms.add("R[" + relativeRow +"]C[" + relativeColumn + "]");

//        System.out.println("label = " + nodeData.label);
//        for (String form:
//             nodeData.forms) {
//            System.out.println("form = " + form);
//        }
//        System.out.println();
    }

    public static void main(String args[]) {
        //TODO：测试regular expression
        MyInputParser<MyNodeData> inputParser = new MyInputParser<>();
        Node<MyNodeData> astInstanceOne = inputParser.fromString("{DIV{R[-1]C}{R[-1]C[-1]}}", "C9");
        Node<MyNodeData> astInstanceTwo = inputParser.fromString("{DIV{R[-1]C}{R[-1]C[-2]}}", "D9");


    }
}
