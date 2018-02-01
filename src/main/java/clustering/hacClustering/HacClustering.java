package clustering.hacClustering;

import convenience.RTED;
import entity.Cluster;
import featureExtraction.strongFeatureExtraction.AST;
import util.LblTree;
import utility.BasicUtility;

import java.util.*;
import java.util.logging.Logger;

public class HacClustering {
	private Logger logger = Logger.getLogger("HAC");

	private Map<String, List<String>> formulaInfoList;
	private double[][] distances;
	private List<String> formulaCellAdd;
	private int N = 0;

    public HacClustering(Map<String, List<String>> formulaInfoList) {
		this.formulaInfoList = formulaInfoList;
		N = formulaInfoList.size();
		distances = new double[2*N][2*N];
	}

	public void computeDistance(TreeEditDistance ted) throws OutOfMemoryError {
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

			AST astLeft = new AST(left[1], left[0], ted.sheet);
            String treeStr = "";
            Cluster clLeft = astLeft.createTree();
            if (clLeft != null) {
				treeStr = ted.childrenSearch(clLeft);
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

                AST astRight = new AST(right[1], right[0], ted.sheet);
                Cluster clRight = astRight.createTree();

                if (clRight != null)
                    treeStr = ted.childrenSearch(clRight);

                String asTreeStrRight = treeStr.replace(":", " to ");
                LblTree asTreeRight = LblTree.fromString(asTreeStrRight);

                int leftNodeNum2 = asTreeLeft.getNodeCount();
                int rightNodeNum2 = asTreeRight.getNodeCount();
                double nodeSum2 = leftNodeNum2 + rightNodeNum2;
                double astDist = (RTED.computeDistance(asTreeStrLeft,asTreeStrRight)) / nodeSum2;

//                out.printf("%s = %s, %s = %s\n", left[0], asTreeStrLeft, right[0], asTreeStrRight);

                //compute CDT.
                String dpTreeStrLeft = BasicUtility.cellDependencies(ted.sheet, left[0], 0);
                LblTree dpTreeLeft = LblTree.fromString(dpTreeStrLeft);
                int leftNodeNum = dpTreeLeft.getNodeCount();

                String dpTreeStrRight = BasicUtility.cellDependencies(ted.sheet, right[0], 0);
                LblTree dpTreeRight = LblTree.fromString(dpTreeStrRight);
                int rightNodeNum = dpTreeRight.getNodeCount();

                double nodeSum = leftNodeNum + rightNodeNum;

                //TODO: 下面这个语句会产生 java.lang.OutOfMemoryError: Java heap space,
				// 暂时不知道怎么修复,也不知道出现在哪个表里
				double dpDist = (RTED.computeDistance(dpTreeStrLeft, dpTreeStrRight)) / nodeSum;
//                out.printf("%s = %s, %s = %s\n", left[0], dpTreeStrLeft, right[0], dpTreeStrRight);

				//TODO: 似乎这些计算是不可避免的 讲道理下面被注释掉的才是和原文相符的计算过程
				//distances[n][m] = distances[m][n] = (astDist + dpDist - astDist*dpDist);
				distances[n][m] = distances[m][n] = (astDist + 0.001) * (dpDist + 0.001);
				//System.out.printf("distance[%d][%d] = %f\n", n, m, (astDist + 0.001) * (dpDist + 0.001));
				n++;
			}
			m++;
		}

		System.out.println("Tree Distance finished " + new BasicUtility().getCurrentTime());
	}

	public List<Cluster> clustering(TreeEditDistance ted) throws OutOfMemoryError {
    	computeDistance(ted);
    	return performClustering();
	}

	public List<Cluster> clusteringWrapper(List<Cluster> caCheckCluster, Set<String> caCheckFormula) {

		//TODO: cluster initialization
		List<Cluster> clusters = new ArrayList<>();

		for (Cluster cluster: caCheckCluster) {
			clusters.add(cluster);
		}

		for (String aFormulaCellAdd : formulaCellAdd)
		{
			if (!caCheckFormula.contains(aFormulaCellAdd)) {
				clusters.add(new Cluster(aFormulaCellAdd));
			}
		}
		System.out.println();

		//TODO: clustering process
		clusteringCoreProcess(clusters);

		return clusters;
	}

	private List<Cluster> performClustering() {
		//TODO: cluster initialization
		List<Cluster> clusters = new ArrayList<>();
		for (String formulaCell : formulaCellAdd)
			clusters.add(new Cluster(formulaCell));

		//TODO: clustering process
		return clusteringCoreProcess(clusters);
	}

	private List<Cluster> clusteringCoreProcess(List<Cluster> clusters)
	{
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
				if (visitedStack.get(j) == 2) continue;
				duplicateCode(clusters, i, j);
			}
			for (int j = i+1; j < clusters.size(); j++) {
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
			Cluster cluster = clusters.get(i);
			if (cluster.merged) clusters.remove(cluster);
		}

		return clusters;

		/*
		double minDist = 0;
		double eps = 0.02;
		while (minDist <= eps) {
			minDist = 0.5;
			Cluster clusterLeft = null;
			Cluster clusterRight = null;

			for (int i=0;i<=clusters.size()-2;i++) {
				for (int j=i+1;j<=clusters.size()-1;j++) {
					double tmpDist = computeDist(clusters.get(i), clusters.get(j));
					if (tmpDist < minDist) {
						minDist = tmpDist;
						clusterLeft = clusters.get(i);
						clusterRight = clusters.get(j);
					}
				}
			}

			if (minDist <= eps) {
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
		*/
	}

	private void duplicateCode(List<Cluster> clusters, int i, int j) {
		Cluster cluster_i = clusters.get(i);
		Cluster cluster_j = clusters.get(j);

		int index_i = formulaCellAdd.indexOf(cluster_i.getName());
		int index_j = formulaCellAdd.indexOf(cluster_j.getName());

		double dist = computeDist(cluster_i, cluster_j);
		distances[index_i][index_j] = distances[index_j][index_i] = dist;
	}

	private double computeDist(Cluster clusterLeft, Cluster clusterRight) {
		double sum = 0;
		List<Cluster> leafLefts = clusterLeft.getChildrenCluster(clusterLeft);
		List<Cluster> leafRights = clusterRight.getChildrenCluster(clusterRight);

//		System.out.println("formulaCellAdd size = " + formulaCellAdd.size());

		for (Cluster leafLeft : leafLefts) {
			for (Cluster leafRight : leafRights) {
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
