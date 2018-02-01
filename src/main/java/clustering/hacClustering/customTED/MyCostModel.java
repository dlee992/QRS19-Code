package clustering.hacClustering.customTED;

import costmodel.CostModel;
import node.Node;


public class MyCostModel implements CostModel<MyNodeData> {

    private float delCost;
    private float insCost;
    private float renCost;


    public MyCostModel(float delCost, float insCost, float renCost) {
        this.delCost = delCost;
        this.insCost = insCost;
        this.renCost = renCost;
    }


    @Override
    public float del(Node<MyNodeData> node) {
        return this.delCost;
    }

    @Override
    public float ins(Node<MyNodeData> node) {
        return this.insCost;
    }

    @Override
    /*
     * TODO: 这里需要针对单元格类型的节点特殊处理!
     */
    public float ren(Node<MyNodeData> n1, Node<MyNodeData> n2) {
        MyNodeData nd1 = n1.getNodeData(), nd2 = n2.getNodeData();

        if (nd1.isCell && nd2.isCell) {
            float ret = this.renCost;

            for (String form1:
                 nd1.forms) {
                for (String form2:
                     nd2.forms) {
                    if (form1.equals(form2)) ret = 0.0F;
                }
            }

            return ret;
        }
        else {
            return nd1.getLabel().equals(nd2.getLabel()) ? 0.0F : this.renCost;
        }
    }
}
