package clustering.bootstrappingClustering;

import entity.CellFeature;
import entity.Cluster;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.poi.ss.util.CellReference;

import java.util.List;


public class CellClusterMatrix {

	private List<CellReference> cellRefsVector = null;
	private List<Cluster> clusterVector = null;
	
	CellClusterMatrix(List<CellReference> cellRefsVector, List<Cluster> clusterVector){
		this.cellRefsVector = cellRefsVector;
		this.clusterVector = clusterVector;
	}
	
	public CellClusterMatrix() {}

	RealMatrix matrixCreation(List<CellFeature> featureList){
		RealMatrix rm = new Array2DRowRealMatrix(cellRefsVector.size(), clusterVector.size());
		for (CellFeature ft: featureList){
			if (ft.getCluster()!=null){
				rm.setEntry(cellRefsVector.indexOf(ft.getCellReference()), clusterVector.indexOf(ft.getCluster()), 1);
				}
					}
		return rm;
	}

	RealMatrix computeMatrix(RealMatrix featureCellM, RealMatrix featureClusterM) {
		RealMatrix cellClusterM = featureCellM.transpose().multiply(featureClusterM);
		cellClusterM = cellClusterM.scalarMultiply((double)1/featureClusterM.getRowDimension());
		return cellClusterM;
	}

	public RealMatrix matrixCreation(boolean[] taggedCells) {
		RealMatrix rm = new Array2DRowRealMatrix(taggedCells.length, 2);
		
		for (int index = 0; index< taggedCells.length;index++){
			if (!taggedCells[index]){
				rm.setEntry(index, 0, 1);
			}
			else{
				rm.setEntry(index, 1, 1);
			}
		}
		return rm;
	}
}
