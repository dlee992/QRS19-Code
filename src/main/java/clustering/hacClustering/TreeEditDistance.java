package clustering.hacClustering;

import convenience.RTED;
import entity.Cluster;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Sheet;
import utility.BasicUtility;
import util.LblTree;

import static java.lang.System.out;


public class TreeEditDistance {
	private static Logger logger = LogManager.getLogger(TreeEditDistance.class.getName());

	public Sheet sheet;

	private BasicUtility bu = new BasicUtility();

	public TreeEditDistance(Sheet sheet) {
		this.sheet = sheet;
	}
	
	public void compute(String[] left, String[] right) {
		//compute AST.
//		return 1- (1-astDist) * (1-dpDist);

	}

	public String childrenSearch(Cluster cl) {
		StringBuilder ret = new StringBuilder();
		ret.append("{").append(cl.getName());
		if (!cl.isLeaf()) {
			for (Cluster child : cl.getChildren()) {
				ret.append(childrenSearch(child));
			}
		}
		ret.append("}");

		return ret.toString();
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
