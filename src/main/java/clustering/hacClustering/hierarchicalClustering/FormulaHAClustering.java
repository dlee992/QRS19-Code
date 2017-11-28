package clustering.hacClustering.hierarchicalClustering;

import clustering.hacClustering.TreeEditDistance;
import entity.Cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FormulaHAClustering {
	private Map<String, List<String>> formulaInfoList =null;
	double[][] distances;
	
	public FormulaHAClustering(Map<String, List<String>> formulaInfoList) {
		this.formulaInfoList = formulaInfoList;
	}



	public List<Cluster> clustering(TreeEditDistance measure) {

		
		distances = new double[formulaInfoList.size()][formulaInfoList
                                         				.size()];
		long beginTime =0;
		long endTime = 0;
		
		List <Cluster> clusters = new ArrayList<Cluster> ();
		int m =0;
		String[] formulaCellAdd = new String[formulaInfoList.size()];
 
			try{for (Map.Entry<String, List<String>> itemOut : formulaInfoList
					.entrySet()) {
				formulaCellAdd[m] = itemOut.getKey();
				int n = 0;
				
				String[] left = new String [4];
				left[0]= itemOut.getKey();
				left[1] = itemOut.getValue().get(0);
				left[2] = itemOut.getValue().get(1);
				left[3] = itemOut.getValue().get(2);
				
				double sumRow = 0;
				for (Map.Entry<String, List<String>> itemIn : formulaInfoList
					.entrySet()){
				String[] right = new String [4];
				right[0] = itemIn.getKey();
				right[1] = itemIn.getValue().get(0);
				right[2] = itemIn.getValue().get(1);
				right[3] = itemIn.getValue().get(2);
					distances[m][n] = measure.compute(
							left,right);
					sumRow += distances[m][n];
					n++;
				}
				//System.out.println("Row"+m+" Sum="+sumRow);
				m++;	
				}
				
			
			
			}catch(Exception e){
				e.printStackTrace();
			}
			double eps = 0.02;
			if (distances.length != 0) {
				ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
				System.out.println("First stage clustering begin:");
				beginTime = System.currentTimeMillis();
				clusters = alg.performClustering(distances, formulaCellAdd,
						new AverageLinkageStrategy(), eps);
			}
			endTime = System.currentTimeMillis();
	System.out.println("Fist stage clustering done");
	System.out.println("Time for clustering: " + (endTime
		 - beginTime) / 1000 + "s");
	
			return clusters;
		}
	
	public List<Cluster> clusteringWODistanceCalculation(double[][] distances) {

		long beginTime =0;
		long endTime = 0;
		
		List <Cluster> clusters = new ArrayList<Cluster> ();
		int m =0;
		String[] formulaCellAdd = new String[formulaInfoList.size()];
 
	for (Map.Entry<String, List<String>> itemOut : formulaInfoList
					.entrySet()) {
				formulaCellAdd[m] = itemOut.getKey();
				m++;
			}

			double eps = 0.2;
			if (distances.length != 0) {
				ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
				System.out.println("First stage clustering begin:");
				beginTime = System.currentTimeMillis();
				clusters = alg.performClustering(distances, formulaCellAdd,
						new AverageLinkageStrategy(), eps);
			}
			endTime = System.currentTimeMillis();
	System.out.println("Fist stage clustering done");
	System.out.println("Time for clustering: " + (endTime
		 - beginTime) / 1000 + "s");
	
			return clusters;
		}

	public double[][] getDistances() {
		return distances;
	}

	public void setDistances(double[][] distances) {
		this.distances = distances;
	}
	
	public static void mainx(String[] args) {
		ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
		
		double[][] distances = new double[][]{
			{0,	17,	21,	31,	23},
			{17,0, 30, 34, 21},
			{21,	30,	0,	28,	39},
			{31,	34,	28,	0,	43},
			{23,	21,	39,	43,	0}};
		String[] formulaCellAdd = new String[] {"a","b","c","d","e"};	
		double eps = 32;
		List<Cluster> clusters = alg.performClustering(distances, formulaCellAdd,
				new AverageLinkageStrategy(), eps);
		System.out.println(clusters.size());
	}
}
