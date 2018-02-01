package clustering.hacClustering.customTED;

import node.Node;
import parser.InputParser;
import util.FormatUtilities;

import java.util.Vector;

public class MyInputParser<D> {

    public MyInputParser() { }



    public Node<MyNodeData> fromString(String s, String parentCell) {
        s = s.substring(s.indexOf("{"), s.lastIndexOf("}") + 1);
        Node<MyNodeData> node = new Node<>(new MyNodeData(FormatUtilities.getRoot(s)));

        Vector c = FormatUtilities.getChildren(s);

        for(int i = 0; i < c.size(); ++i) {
            node.addChild(this.fromString((String)c.elementAt(i)));
        }

        return node;
    }

}
