package clustering.bootstrappingClustering;

import entity.CellFeature;
import featureExtraction.weakFeatureExtraction.Alliance;
import featureExtraction.weakFeatureExtraction.Gap;
import featureExtraction.weakFeatureExtraction.Header;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.poi.ss.util.CellReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static Benchmarks.TestEUSES.TIMEOUT;

//import static programEntry.GP.O4_2;
//import static programEntry.GP.weightParameter;


public class FeatureCellMatrix {

	private List<CellReference> cellRefsVector = null;
	private List<String> featureVector = new ArrayList<String>();

	private long timeout = (long) (TIMEOUT * 1_000_000_000.0);
	private long beginTime;

	
	public FeatureCellMatrix(List<String> featureVector, List<CellReference> cellRefsVector, long beginTime) {
		this.setCellRefsVector(cellRefsVector);
		this.setFeatureVector(featureVector);
		this.beginTime = beginTime;
	}

	private void setCellRefsVector(List<CellReference> cellRefsVector) {
		this.cellRefsVector = cellRefsVector;
	}

	private void setFeatureVector(List<String> featureVector) {
		this.featureVector = featureVector;
	}

	public RealMatrix matrixCreationForClustering(List<CellFeature> cellFeatureList) throws InterruptedException {
		RealMatrix rm = new Array2DRowRealMatrix(featureVector.size(),cellRefsVector.size());
		
		for (CellFeature ft: cellFeatureList){

			if (Thread.interrupted()) {
				throw new InterruptedException();
			}
			
			if (cellRefsVector.contains(ft.getCellReference())) {

				int cellVecIndex = cellRefsVector.indexOf(ft.getCellReference());
				
				List<String> contents = new ArrayList<String> ();
				
				if (ft.getHeaders()!=null){
					for (Header hd: ft.getHeaders()){
						if (hd!=null){
							contents.add(hd.toString());
					}
				}

				}
				if (ft.getReferencedByAlliances() != null){
					for (Alliance alliance: ft.getReferencedByAlliances()){
						if (alliance!=null){
							contents.add(alliance.toString());
						}
					}
				}
				
				if (ft.getColOrRowBased() == 1){
					contents.add("ColBased");
				}
				if (ft.getColOrRowBased() == 0){
					contents.add("RowBased");
				}
				
				if (ft.getIndex()!=null){
					contents.add("RowIndex"+ft.getIndex()[0]);
					contents.add("ColIndex"+ft.getIndex()[1]);
				}
			
				Set<Gap> gaps= ft.getGaps();
				if (gaps !=null){
					for (Gap gap: gaps){
						if (gap!=null){
							contents.add(gap.toString());
						}
					}
				}
	
				if (ft.getSn()!=null){
					contents.add(ft.getSn().toString());
				}
					
				if (ft.getCa()!=null){
					contents.add(ft.getCa().toString());
				}
				
				rm = setEntryValue(contents, cellVecIndex, rm);
			}
		}

		return rm;
	}



	private RealMatrix setEntryValue(List<String> contents, int cellVecIndex, RealMatrix rm) {
		for (String content: contents){
			if (featureVector.contains(content)){
				if (content.contains("Gap")) {
					rm.setEntry(featureVector.indexOf(content), cellVecIndex, 0.5);
				}
//				else if (content.contains("Index")) {
					//TODO: want to restrict cell address (row & column index) weight
//					rm.setEntry(featureVector.indexOf(content), cellVecIndex, 0.5);
//				}
				/*
				TODO: if the header string contains "total" or "gross" substring, promote its weight.
				 */
//				else if (O4_2 && content.contains("Header")) {
//					if (content.toLowerCase().contains("total") ||
//							content.toLowerCase().contains("gross"))
//						rm.setEntry(featureVector.indexOf(content), cellVecIndex, weightParameter);
//					else
//						rm.setEntry(featureVector.indexOf(content), cellVecIndex, 1);
//				}
				else
					rm.setEntry(featureVector.indexOf(content), cellVecIndex, 1);

			}
		}
		return rm;
	}



	public RealMatrix matrixCreationForDetection(List<CellFeature> featureList) {
		RealMatrix rm = new Array2DRowRealMatrix(featureVector.size(), cellRefsVector.size());
		
		for (CellFeature ft: featureList){
			
			if (cellRefsVector.contains(ft.getCellReference())){
				
				int cellVecIndex = cellRefsVector.indexOf(ft.getCellReference());
				
				List<String> contents = new ArrayList<String> ();
			
				if (ft.getOpTokens()!=null){
					String ops = "";
					for (String opToken : ft.getOpTokens()){
						ops = ops + opToken + ",";
					}
					if ( !ops.equals("")){
						contents.add("Operation=" + ops);
					}
				}
				
				if (ft.getRefTokens()!=null&& !ft.getRefTokens().isEmpty()){
					String refs = "";
					for (String refToken : ft.getRefTokens()){
						refs = refs + refToken + ",";
					}
					if (!refs.equals("")){
						contents.add("RefToken" + refs);
					}
				}
				if (!ft.isScalarInFormula()){
					contents.add("ScalarConstantPtg");
				}
				
				rm = setEntryValue(contents, cellVecIndex, rm);
			}
		}
		
		return rm;
	}
}
