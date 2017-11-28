package clustering.bootstrappingClustering;

import org.apache.commons.math3.analysis.function.Max;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.descriptive.summary.Sum;

public class FeatureClusterMatrix {
	
	private RealMatrix featureCellM;
	private RealMatrix cellClusterM;
	
	public FeatureClusterMatrix(RealMatrix featureCellM, RealMatrix cellClusterM) {
		this.featureCellM = featureCellM;
		this.cellClusterM = cellClusterM;
	}
	
	public RealMatrix matrixCreation() {
		RealMatrix fClusterM = featureCellM.multiply(cellClusterM);
		fClusterM = fClusterM.scalarMultiply((double) 1 / cellClusterM.getRowDimension());
		return fClusterM;
	}


	//TODO: this computation is Ochiai coefficient. K = n(A \join B) / sqrt(n(A)*n(B)).
	RealMatrix computeOcc(RealMatrix rm) {
		double total = 0;
		double[] totalX = new double[rm.getRowDimension()];
		double[] totalY = new double[rm.getColumnDimension()];
		Sum sum = new Sum();

		for (int i = 0; i < rm.getRowDimension(); i++) {
			totalX[i] = sum.evaluate(rm.getRow(i));
			for (int j = 0; j < rm.getColumnDimension(); j++) {
				total += rm.getEntry(i, j);
				totalY[j] = sum.evaluate(rm.getColumn(j));
			}
		}

		for (int i = 0; i < rm.getRowDimension(); i++) {
			for (int j = 0; j < rm.getColumnDimension(); j++) {
				double pxy = rm.getEntry(i, j) / total;
				double temp = pxy
						/ Math.sqrt(((totalX[i]) / total)
								* ((totalY[j]) / total));

				rm.setEntry(i, j, temp);
			}
		}
		return rm;
	}

	//TODO: this method wants to compute normalized point-wise mutual information.
    //
	public RealMatrix computeNPMI(RealMatrix rm) {
		double total = 0;
		double[] totalX = new double[rm.getRowDimension()];
		double[] totalY = new double[rm.getColumnDimension()];
		Sum sum = new Sum();
		Max max = new Max();

		for (int i = 0; i < rm.getRowDimension(); i++) {
			totalX[i] = sum.evaluate(rm.getRow(i));
			for (int j = 0; j < rm.getColumnDimension(); j++) {
				total += rm.getEntry(i, j);
				totalY[j] = sum.evaluate(rm.getColumn(j));
			}
		}

		for (int i = 0; i < rm.getRowDimension(); i++) {
			for (int j = 0; j < rm.getColumnDimension(); j++) {
				double pxy = rm.getEntry(i, j) / total;
				double temp = pxy / ((totalX[i] / total) * (totalY[j] / total));
				double newValue = 0;
				if (temp > 1) {
					newValue = (Math.log(temp)) / (-Math.log(pxy));
				}
				rm.setEntry(i, j, newValue);
			}
		}
		
		double maxValue = 0;

		for (int i = 0; i < rm.getRowDimension(); i++) {
			for (int j = 0; j < rm.getColumnDimension(); j++) {
				maxValue = max.value(rm.getEntry(i, j), maxValue);
			}
		}

		for (int i = 0; i < rm.getRowDimension(); i++) {
			for (int j = 0; j < rm.getColumnDimension(); j++) {
				rm.setEntry(i, j, rm.getEntry(i, j) / maxValue);
			}
		}
		return rm;
	}
	
}
