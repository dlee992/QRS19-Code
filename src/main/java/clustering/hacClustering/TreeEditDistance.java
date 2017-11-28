package clustering.hacClustering;

import convenience.RTED;
import entity.Cluster;
import featureExtraction.strongFeatureExtraction.AST;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import utility.BasicUtility;
import util.LblTree;

import static java.lang.System.out;


public class TreeEditDistance {
	private static Logger logger = LogManager.getLogger(TreeEditDistance.class.getName());

	private Sheet sheet;
	private String treeStr;
	private BasicUtility bu = new BasicUtility();

	public TreeEditDistance(Sheet sheet) {
		this.sheet = sheet;
	}
	
	public double compute(String[] left, String[] right) {
		//compute AST.
		AST astLeft = new AST(left[1], left[0], sheet);

		treeStr = "";
		Cluster clLeft = astLeft.createTree();

		if (clLeft != null)
			childrenSearch(clLeft);

		String asTreeStrLeft = treeStr.replace(":", " to ");

		LblTree asTreeLeft = LblTree.fromString(asTreeStrLeft);

		treeStr = "";
		AST astRight = new AST(right[1], right[0], sheet);
		Cluster clRight = astRight.createTree();

		if (clRight != null)
			childrenSearch(clRight);

		String asTreeStrRight = treeStr.replace(":", " to ");
		LblTree asTreeRight = LblTree.fromString(asTreeStrRight);

		int leftNodeNum2 = asTreeLeft.getNodeCount();
		int rightNodeNum2 = asTreeRight.getNodeCount();
		double nodeSum2 = leftNodeNum2 + rightNodeNum2;

		double astDist = (RTED.computeDistance(asTreeStrLeft,asTreeStrRight)) / nodeSum2;

//		out.printf("%s = %s, %s = %s\n", left[0], asTreeStrLeft, right[0], asTreeStrRight);
		
		//compute CDT.
		
		String dpTreeStrLeft = bu.cellDependencies(sheet, left[0], 0);

		LblTree dpTreeLeft = LblTree.fromString(dpTreeStrLeft);
		int leftNodeNum = dpTreeLeft.getNodeCount();
		
		
		String dpTreeStrRight = bu.cellDependencies(sheet, right[0], 0);
		
		LblTree dpTreeRight = LblTree.fromString(dpTreeStrRight);
		int rightNodeNum = dpTreeRight.getNodeCount();
		
		double nodeSum = leftNodeNum + rightNodeNum;

		double dpDist = (RTED.computeDistance(dpTreeStrLeft,
				dpTreeStrRight)) / nodeSum;

//
        if (left[0].equals("T14") && right[0].equals("U26")) {
            out.printf("distance = %f\n", 1- (1-astDist) * (1-dpDist));
            out.printf("%s = %s, %s = %s\n", left[0], asTreeStrLeft, right[0], asTreeStrRight);
            out.printf("%s = %s, %s = %s\n", left[0], dpTreeStrLeft, right[0], dpTreeStrRight);
        }

//		return 1- (1-astDist) * (1-dpDist);
	    return (astDist + 0.001) * (dpDist+ 0.001);
	}

	private void childrenSearch(Cluster cl) {
		treeStr = treeStr + "{" + cl.getName();
		if (!cl.isLeaf()) {
			for (Cluster child : cl.getChildren()) {
				childrenSearch(child);
			}
		}
		treeStr = treeStr + "}";
	}

    public static void mainx(String[] argv) {
        String asTreeStrLeft = "{a{b}{c}}";
		//"{RR{R[-5]C}{R[-4]C}{R[-3]C}{R[-2]C}{R[-1]C}{R[-5]C[1]}{R[-4]C[1]}{R[-3]C[1]}{R[-2]C[1]}{R[-1]C[1]}}";
        String asTreeStrRight = "{aa{bb}{cc}}";
		//"{RR{NOTES TO FS!R[-5]C}{NOTES TO FS!R[-4]C}{NOTES TO FS!R[-3]C}{NOTES TO FS!R[-2]C}{NOTES TO FS!R[-1]C}" +
		//"{NOTES TO FS!R[-5]C[1]}{NOTES TO FS!R[-4]C[1]}{NOTES TO FS!R[-3]C[1]}{NOTES TO FS!R[-2]C[1]}{NOTES TO FS!R[-1]C[1]}}";
        LblTree asTreeLeft = LblTree.fromString(asTreeStrLeft);
        LblTree asTreeRight = LblTree.fromString(asTreeStrRight);
        int leftNodeNum2 = asTreeLeft.getNodeCount();
        int rightNodeNum2 = asTreeRight.getNodeCount();
        double nodeSum2 = leftNodeNum2 + rightNodeNum2;

        double astDist = (RTED.computeDistance(asTreeStrLeft,asTreeStrRight)) / nodeSum2;

        out.println(astDist);
    }

}
