package clustering.hacClustering;

import clustering.hacClustering.customTED.MyCostModel;
import clustering.hacClustering.customTED.MyInputParser;
import clustering.hacClustering.customTED.MyNodeData;
import convenience.RTED;
import distance.APTED;
import entity.Cluster;
import extraction.strongFeatureExtraction.AST;
import kernel.GP;
import node.Node;
import org.apache.poi.ss.usermodel.Sheet;
import util.LblTree;
import utility.BasicUtility;

import java.util.*;
import java.util.logging.Logger;

import static kernel.GP.addD;

public class HacClustering {
	private Logger logger = Logger.getLogger("HAC");

	private Map<String, List<String>> formulaInfoList;
	private double[][] distances;
	private List<String> formulaCellAdd;
	private int N = 0;
	private Sheet sheet;
	private long beginTime;


	public HacClustering(Sheet sheet, Map<String, List<String>> formulaInfoList, long beginTime) {
    	this.sheet = sheet;
		this.formulaInfoList = formulaInfoList;
		N = formulaInfoList.size();
		distances = new double[2*N][2*N];
		this.beginTime = beginTime;
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


	public void computeDistance() throws OutOfMemoryError {
    	/*
    	* TODO: 如果要修改TED过程，本质上只要修改rename操作时能够同时比较一个单元格的四种表示形式，
    	* TODO: 并且返回其中remove的最小代价(minimum cost)
    	*
    	* TODO: 先判断node类型；如果都是单元格，那么比较四种形式(4*4)；计算一个最小值(应该就是看字符串是否完全相同吧!)
    	 */


		int m = 0;
		formulaCellAdd = new ArrayList<>();

//		System.out.println("formulaInfoList size = " + formulaInfoList.size());
		int index = 0;
		for (Map.Entry<String, List<String>> itemOut : formulaInfoList.entrySet())
		{
			formulaCellAdd.add(itemOut.getKey());

			String[] left = new String [4];
			left[0]= itemOut.getKey();
			left[1] = itemOut.getValue().get(0);
			left[2] = itemOut.getValue().get(1);

			AST astLeft = new AST(left[1], left[0], sheet);
            String treeStr = "";
            Cluster clLeft = astLeft.createTree();
            if (clLeft != null) {
				treeStr = childrenSearch(clLeft);
            }
//            out.println(left[0] + "'s AST Tree = " + treeStr);
            String asTreeStrLeft = treeStr.replace(":", " to ");
            LblTree asTreeLeft = LblTree.fromString(asTreeStrLeft);

			int n = 0;
			for (Map.Entry<String, List<String>> itemIn : formulaInfoList.entrySet())
			{
			    if (m >= n) {
			        n++;
			        continue;
                }

                //System.out.println("tree distance comparision's index = " + ++index);

				String[] right = new String [4];
				right[0] = itemIn.getKey();
				right[1] = itemIn.getValue().get(0);
				right[2] = itemIn.getValue().get(1);

                AST astRight = new AST(right[1], right[0], sheet);
                Cluster clRight = astRight.createTree();

                if (clRight != null)
                    treeStr = childrenSearch(clRight);

                String asTreeStrRight = treeStr.replace(":", " to ");
                LblTree asTreeRight = LblTree.fromString(asTreeStrRight);

                int leftNodeNum2 = asTreeLeft.getNodeCount();
                int rightNodeNum2 = asTreeRight.getNodeCount();
                double nodeSum2 = leftNodeNum2 + rightNodeNum2;
                double astDist = (RTED.computeDistance(asTreeStrLeft,asTreeStrRight)) / nodeSum2;

//                out.printf("%s = %s, %s = %s\n", left[0], asTreeStrLeft, right[0], asTreeStrRight);

                //compute CDT.
                String dpTreeStrLeft = BasicUtility.cellDependencies(sheet, left[0], 0);
                LblTree dpTreeLeft = LblTree.fromString(dpTreeStrLeft);
                int leftNodeNum = dpTreeLeft.getNodeCount();

                String dpTreeStrRight = BasicUtility.cellDependencies(sheet, right[0], 0);
                LblTree dpTreeRight = LblTree.fromString(dpTreeStrRight);
                int rightNodeNum = dpTreeRight.getNodeCount();

                double nodeSum = leftNodeNum + rightNodeNum;

                //TODO: 下面这个语句会产生 java.lang.OutOfMemoryError: Java heap space,
				// 暂时不知道怎么修复,也不知道出现在哪个表里
				double dpDist = (RTED.computeDistance(dpTreeStrLeft, dpTreeStrRight)) / nodeSum;
//                out.printf("%s = %s, %s = %s\n", left[0], dpTreeStrLeft, right[0], dpTreeStrRight);

				//TODO: 似乎这些计算是不可避免的 讲道理下面被注释掉的才是和原文相符的计算过程
				distances[n][m] = distances[m][n] = astDist * dpDist;
				//distances[n][m] = distances[m][n] = (astDist + dpDist - astDist*dpDist);
				//distances[n][m] = distances[m][n] = (astDist + 0.001) * (dpDist + 0.001);
				//System.out.printf("distance(%s, %s) = %f\n", left[1], right[1], distances[n][m]);
				n++;
			}
			m++;
		}

		System.out.println("Tree Distance finished " + new BasicUtility().getCurrentTime());
	}


	public List<Cluster> clustering() throws OutOfMemoryError, InterruptedException {
		if (!addD)
			computeDistance();
    	else
    		newComputeDistance();

		return performClustering();
	}

	private void newComputeDistance() throws InterruptedException {
		int m = 0;
		formulaCellAdd = new ArrayList<>();

		MyInputParser<MyNodeData> inputParser = new MyInputParser<>();
		APTED<MyCostModel, MyNodeData> apted = new APTED<>(new MyCostModel(1, 1, 1));

//		System.out.println("formulaInfoList size = " + formulaInfoList.size());
		int index = 0;
		for (Map.Entry<String, List<String>> itemOut : formulaInfoList.entrySet())
		{
			formulaCellAdd.add(itemOut.getKey());

			String[] cellOne = new String [4];
			cellOne[0]= itemOut.getKey();
			cellOne[1] = itemOut.getValue().get(0);
			cellOne[2] = itemOut.getValue().get(1);

			//TODO: 辅助进行AST树计算的准备工作
			Cluster astOne  = new AST(cellOne[1], cellOne[0], sheet).createTree();
			String treeString = "";
			if (astOne != null) {
				treeString = childrenSearch(astOne);
			}
//            out.println(cellOne[0] + "'s AST Tree = " + treeString);
			String astStringOne = treeString.replace(":", " to ");

			//TODO: new API
			Node<MyNodeData> astInstanceOne = inputParser.fromString(astStringOne, r1c1(cellOne[0]));

			//TODO: 辅助进行dependency树计算的准备工作
			String cdtStringOne = BasicUtility.cellDependencies(sheet, cellOne[0], 0);
			Node<MyNodeData> cdtInstanceOne = inputParser.fromString(cdtStringOne, r1c1(cellOne[0]));
			int cdtNodeCount = cdtInstanceOne.getNodeCount();

			int n = 0;
			for (Map.Entry<String, List<String>> itemIn : formulaInfoList.entrySet())
			{
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}

				if (m >= n) {
					n++;
					continue;
				}

				//System.out.println("tree distance comparision's index = " + ++index);

				//TODO: 计算AST树的相似度
				String[] cellTwo = new String [4];
				cellTwo[0] = itemIn.getKey();
				cellTwo[1] = itemIn.getValue().get(0);
				cellTwo[2] = itemIn.getValue().get(1);

				AST astRight = new AST(cellTwo[1], cellTwo[0], sheet);
				Cluster clRight = astRight.createTree();

				if (clRight != null)
					treeString = childrenSearch(clRight);

				String astStringTwo = treeString.replace(":", " to ");
				Node<MyNodeData> astInstanceTwo = inputParser.fromString(astStringTwo, r1c1(cellTwo[0]));

				double nodeSum = astInstanceOne.getNodeCount() + astInstanceTwo.getNodeCount();

				double astDist = (apted.computeEditDistance(astInstanceOne, astInstanceTwo)) / nodeSum;

//                out.printf("%s = %s, %s = %s\n", cellOne[0], asTreeStrLeft, cellTwo[0], astStringTwo);

				//TODO: 计算Dependency树的相似度
				String cdtStringTwo = BasicUtility.cellDependencies(sheet, cellTwo[0], 0);
				//TODO: new API
				Node<MyNodeData> cdtInstanceTwo = inputParser.fromString(cdtStringTwo, r1c1(cellTwo[0]));

				nodeSum = cdtNodeCount + cdtInstanceTwo.getNodeCount();

				//TODO: 下面这个语句会产生 java.lang.OutOfMemoryError: Java heap space,
				// 暂时不知道怎么修复,也不知道出现在哪个表里
				double dpDist = (apted.computeEditDistance(cdtInstanceOne, cdtInstanceTwo)) / nodeSum;
//                out.printf("%s = %s, %s = %s\n", cellOne[0], cdtStringOne, cellTwo[0], cdtStringTwo);

				//TODO: 似乎这些计算是不可避免的 讲道理下面被注释掉的才是和原文相符的计算过程
				distances[n][m] = distances[m][n] = astDist + dpDist;
				//distances[n][m] = distances[m][n] = (astDist + dpDist - astDist*dpDist);
				//distances[n][m] = distances[m][n] = (astDist + 0.001) * (dpDist + 0.001);
				//System.out.printf("distance[%d][%d] = %f\n", n, m, (astDist + 0.001) * (dpDist + 0.001));
				n++;
			}
			m++;
		}

		System.out.println("Tree Distance finished " + new BasicUtility().getCurrentTime());


	}

	/*
	 * TODO: 貌似在什么地方有类似功能的代码 但是我不记得那个method call了。
	 */
    private String r1c1(String s) {
//    	logger.info("A1 style = " + s);
        int row = 0;
        int column = 0;
        int i = 0;
        while (s.charAt(i) >= 'A' && s.charAt(i) <= 'Z') {
            column = column * 26 + (s.charAt(i)-'A'+1);
            ++i;
        }
        --column;

        while (i < s.length()) {
        	row = row * 10 + (s.charAt(i) - '0');
        	++i;
		}
        --row;

        return "R" + row + "C" + column;
    }

	private List<Cluster> performClustering() throws InterruptedException {
		//TODO: cluster initialization
		List<Cluster> clusters = new ArrayList<>();
		for (String formulaCell : formulaCellAdd)
			clusters.add(new Cluster(formulaCell));

		//TODO: clustering process
		if (GP.clusterOptimization)
			return optimizedClusteringCoreProcess(clusters);
		else
			return originalClusteringCoreProcess(clusters);
	}

	private List<Cluster> originalClusteringCoreProcess(List<Cluster> clusters) throws InterruptedException {

    	int clusterIndex = clusters.size();
		double minDist = 0;
		double eps = 0.02;
		while (minDist <= eps) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}

			minDist = 0.5;
			Cluster clusterLeft = null;
			Cluster clusterRight = null;

			for (int i=0;i<=clusters.size()-2;i++) {
				for (int j=i+1;j<=clusters.size()-1;j++) {
					if (Thread.interrupted()) {
						throw new InterruptedException();
					}

					double tmpDist = computeDist(clusters.get(i), clusters.get(j));
					if (tmpDist < minDist) {
						minDist = tmpDist;
						clusterLeft = clusters.get(i);
						clusterRight = clusters.get(j);
					}
				}
			}

			if (minDist <= eps) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}

				//System.out.println("Merge two different clusters.");
				Cluster pCluster = new Cluster("#"+(++clusterIndex));
				pCluster.addChild(clusterLeft);
				pCluster.addChild(clusterRight);
				assert clusterLeft != null;
				clusterLeft.setParent();
				clusterRight.setParent();

				clusters.add(pCluster);
				clusters.remove(clusterLeft);
				clusters.remove(clusterRight);
			}
		}

		return clusters;
	}

	private List<Cluster> optimizedClusteringCoreProcess(List<Cluster> clusters) throws InterruptedException {
	    /*
	     * TODO: 显然可以加一个优化：预先把完全一样的格放在同一个类中
	     */
		ArrayList<Integer> joinList = new ArrayList<>();
		/*
		* 0: 这个类从未被加入过stack
		* 1: 这个类正在stack中
		* 2: 这个类已经从stack中remove
		 */
		List<Integer> visitedStack = new ArrayList<>(2*N);

		int clusterIndex = 0;
		int size = clusters.size();
		for (int j = 0; j < 2*N; j++) {
			visitedStack.add(0);
			joinList.add(-1);
		}
		for (int round = 0; round < size; round++) {
			if (joinList.get(round) >= 0) continue;

			int toAddCount = 1;
			joinList.set(round, round);

			for (int j = round +1; j < size; j++) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}

				if (distances[round][j] == 0.000001) {
					joinList.set(j, round);
					++toAddCount;
				}
			}

			if (toAddCount > 1) {
				//System.out.println("Merge some same clusters.");
				String name = "#" + ++clusterIndex;
				Cluster parent = new Cluster(name);
				for (int j = size-1; j >=0; j--) {
					if (joinList.get(j) == round) {
						Cluster child = clusters.get(j);
						child.merged = true;
						parent.addChild(child);
						//child.setParent(parent);
					}
				}

				clusters.add(parent);
				formulaCellAdd.add(name);
			}
			else {
				joinList.set(round, -1);
			}
		}


		int upperBound = clusters.size(); //所有叶子类和新生成的中间类的总个数
		for (int round = size-1; round >= 0; round--) {
			if (joinList.get(round) >= 0) {
				//TODO:下面的注释是一个隐患
				//clusters.remove(round);
				visitedStack.set(round, 2);
			}
		}

		//重新计算distances数组
		for (int i = size; i < clusters.size(); i++) {
			for (int j = 0; j < size; j++) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}

				if (visitedStack.get(j) == 2) continue;
				duplicateCode(clusters, i, j);
			}
			for (int j = i+1; j < clusters.size(); j++) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}

				duplicateCode(clusters, i, j);
			}
		}



		System.out.println("Pre-processing is finished " + new BasicUtility().getCurrentTime());

		//TODO:采用 nearest-neighbor chain algorithm, 时间/空间复杂度都是O(n^2).
		//算法链接: https://en.wikipedia.org/wiki/Nearest-neighbor_chain_algorithm.

		//初始化队列
		Deque<Cluster> stack = new ArrayDeque<>();
		int point = 0;
		double threshold = 0.02;

		while (point < upperBound) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}

			//如果队列为空 加入一个未被访问过的类
			if (stack.isEmpty()) {
				while (point < upperBound && visitedStack.get(point) == 2)
					++point;

				if (point >= upperBound) break;
				stack.add(clusters.get(point));
				visitedStack.set(point, 1);
			}

			//对队列头元素，寻找它的最近邻
			Cluster sourceCluster = stack.getLast();

			int source = clusters.indexOf(sourceCluster), minimumTarget = -1;
			double minimumDist = 1;

			for (int target = 0; target < clusters.size(); ++target) {
				if (target == source || visitedStack.get(target) == 2) continue;

				if (distances[source][target] <= threshold && distances[source][target] < minimumDist) {
					minimumDist = distances[source][target];
					minimumTarget = target;
				}
			}

			//如果不存在满足约束的目标类，直接移除并继续迭代
			if (minimumDist == 1) {
				Cluster removeCluster = stack.removeLast();
				int remove = clusters.indexOf(removeCluster);
				visitedStack.set(remove, 2);
				continue;
			}

			assert minimumTarget != -1;

			Cluster targetCluster = clusters.get(minimumTarget);
			//处理这个最近邻：加入到stack中，或者一起remove
			if (visitedStack.get(minimumTarget) == 0) {
				//把这个目标加入stack中
				stack.add(targetCluster);
				visitedStack.set(minimumTarget, 1);
			}
			else if (visitedStack.get(minimumTarget) == 1) {
				//生成新的类
				Cluster mergedCluster = new Cluster("#"+(++clusterIndex));

				mergedCluster.addChild(sourceCluster);
				mergedCluster.addChild(targetCluster);
				sourceCluster.merged = targetCluster.merged = true;

				assert sourceCluster != null;



				//修改stack状态
				stack.removeLast();
				stack.removeLast();
				visitedStack.set(source, 2);
				visitedStack.set(minimumTarget, 2);

				//更新distances数组
				for (int i = 0; i < clusters.size(); i++) {
					if (Thread.interrupted()) {
						throw new InterruptedException();
					}

					Cluster cluster = clusters.get(i);
					if (cluster.merged) continue;
					double dist = computeDist(cluster, mergedCluster);
					distances[i][clusters.size()] = distances[clusters.size()][i] = dist;
				}

				//更新clusters列表和最大值
				clusters.add(mergedCluster);
				++upperBound;

			}
			//继续迭代
		}

		//remove已经被合并但仍然存在于clusters中的类
		for (int i = clusters.size() -1; i >= 0; --i) {
			if (Thread.interrupted()) {
				throw new InterruptedException();
			}

			Cluster cluster = clusters.get(i);
			if (cluster.merged) clusters.remove(cluster);
		}

		return clusters;

	}

	private void duplicateCode(List<Cluster> clusters, int i, int j) throws InterruptedException {
		Cluster cluster_i = clusters.get(i);
		Cluster cluster_j = clusters.get(j);

		int index_i = formulaCellAdd.indexOf(cluster_i.getName());
		int index_j = formulaCellAdd.indexOf(cluster_j.getName());

		double dist = computeDist(cluster_i, cluster_j);
		distances[index_i][index_j] = distances[index_j][index_i] = dist;
	}

	private double computeDist(Cluster clusterLeft, Cluster clusterRight) throws InterruptedException {
		double sum = 0;
		List<Cluster> leafLefts = clusterLeft.getChildrenCluster(clusterLeft);
		List<Cluster> leafRights = clusterRight.getChildrenCluster(clusterRight);

//		System.out.println("formulaCellAdd size = " + formulaCellAdd.size());

		for (Cluster leafLeft : leafLefts) {
			for (Cluster leafRight : leafRights) {
				if (Thread.interrupted()) {
					throw new InterruptedException();
				}

				int leafIndexLeft = formulaCellAdd.indexOf(leafLeft.getName());
				int leafIndexRight = formulaCellAdd.indexOf(leafRight.getName());

				try {
					sum += distances[leafIndexLeft][leafIndexRight];
				}
				catch (ArrayIndexOutOfBoundsException e) {
					if (leafIndexLeft == -1) {
						System.out.printf("leafLeft = %s\n", leafLeft.getName());
					}
					if (leafIndexRight == -1) {
						System.out.printf("leafRight = %s\n", leafRight.getName());
					}

					//FIXME: refer to BasicUtility.java line 374.
					return formulaCellAdd.size() * formulaCellAdd.size();
				}
			}

		}
		sum = sum/(leafLefts.size()*leafRights.size());
		
		return sum;
	}

}
