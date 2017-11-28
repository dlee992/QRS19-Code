package featureExtraction;

import featureExtraction.weakFeatureExtraction.*;
import entity.CellFeature;
import entity.Cluster;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import utility.BasicUtility;

import java.io.*;
import java.util.*;

//import static programEntry.GP.O4_1;
//import static programEntry.GP.O5;
//import static programEntry.GP.arg5;


public class FeatureExtraction {
	
	private List<Cluster> stageIClusters = null;
	private Sheet sheet =null;
	
	private List<Cluster> seedCluster = new ArrayList<Cluster>();
	private List<Cluster> nonSeedCluster = new ArrayList<Cluster>();
	
	private List<CellReference> seedCellRefs = new ArrayList<CellReference>();
	
	private List<CellReference> nonSeedCellRefs = new ArrayList<CellReference>();
	private List<Cell> nonSeedCells = new ArrayList<Cell>();
	
	private List<String> featureVectorForClustering = new ArrayList<String>();
	
	private List<CellFeature> cellFeatureList = new ArrayList<CellFeature> ();
	
	private List<CellReference> cellRefsVector = new ArrayList<CellReference>();
	private List<Cluster> clusterVector = new ArrayList<Cluster>();

	private Set<CellReference> dataCellSet = new HashSet<CellReference>();
	
	public FeatureExtraction(Sheet sheet, List<Cluster> stageIClusters){
		this.sheet = sheet;
		this.stageIClusters = stageIClusters;
	}

	public void featureExtractionFromSheet(List<Cell> dataCells) {
		for (Cluster cluster: stageIClusters){
			if (cluster.getClusterCellRefs().size()>1){
				cluster.setSeedOrNot(true);
				seedCluster.add(cluster);
			}
			else {
				cluster.setSeedOrNot(false);
				nonSeedCluster.add(cluster);
			}
		}
		
		SnippetExtraction se = new SnippetExtraction(sheet);
		// cacheck and amcheck have the same class Snippet.
		List<Snippet> snippets = se.extractSnippet();

		// todo: replace amcheck cell arrays with cacheck cell arrays.
		List<CellArray> allCAs = null;
//		if (!O5) {
			CellArrayExtraction smellyCAExtract = new CellArrayExtraction(sheet, snippets);
			allCAs = smellyCAExtract.extractCellArray();
			printCA(allCAs);
//		}
//		else {
//			todo: transform CAResult to CellArray structure.
//			Tips: ThirdParty.CACheck/CellArray contains more information than weakFeatureExtraction/CellArray.
//			Wrapper wrapper = new Wrapper(sheet, arg5);
//			allCAs = wrapper.processSheet();
//			printCA(allCAs);
//		}

		featureExtractionFromCluster(snippets, allCAs);
		FeatureExtractionFromDataCells(dataCells, snippets, allCAs);
	}

	private void printCA(List<CellArray> allCAs)  {
		File dir = new File(System.getProperty("user.dir") + System.getProperty("file.separator")
				+ "CellArray");
		if (!dir.exists())
			dir.mkdir();

		File file = new File(dir + System.getProperty("file.separator") +
				new BasicUtility().getCurrentTime()+ sheet.getSheetName() + ".txt");

		BufferedWriter bufferedWriter = null;
		try {

			bufferedWriter = new BufferedWriter(new FileWriter(file));
			bufferedWriter.append(sheet.getSheetName());
			bufferedWriter.newLine();
			int index = 0;
			for (CellArray ca : allCAs){
				index ++;
				bufferedWriter.append("index = " + index + ", " + ca.toString());
				bufferedWriter.newLine();
			}
			bufferedWriter.close();

		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	private void featureExtractionFromCluster(List<Snippet> snippets, List<CellArray> allCAs) {
		GapExtraction ge;
		Map<Cell, Gap> gaps = null;
		
		AllianceExtraction ae = new AllianceExtraction(sheet);
		List<Alliance> allianceList = ae.allianceExtraction();
		
		for (Cluster cluster: stageIClusters) {
			if (cluster.isSeedOrNot()) {
				clusterVector.add(cluster);
				
				seedCellRefs.addAll(cluster.getClusterCellRefs());
				
				ge = new GapExtraction(cluster);
				ge.indexSet();
				gaps = ge.getFeature();
			}
			else {
				nonSeedCellRefs.addAll(cluster.getClusterCellRefs());
				nonSeedCells.addAll(cluster.getClusterCells());
			}
			
			for (Cell cell : cluster.getClusterCells()) {
				CellReference cellRef = new CellReference(cell);
				cellRefsVector.add (cellRef);

				if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
					dataCellSet.add(cellRef);
				}
				
				CellFeature ft = new CellFeature (cellRef);
				ft.setCell(cell);
				
				HeaderExtraction he = new HeaderExtraction(sheet, cell);
				List<Header> headers = he.getFeature();
				ft.setHeaders(headers);

				List<Alliance> cellAlliance = ae.getFeature(allianceList, cell);
				ft.setReferencedByAlliances(cellAlliance);
				
				OtherExtraction oe = new OtherExtraction();
				if (cell.getCellType() == Cell.CELL_TYPE_FORMULA)
					ft.setColOrRowBased(oe.columnOrRowBasedCell(cell));
				
				int[] in = new int[2];
				in[0] = cell.getRowIndex();
				in[1] = cell.getColumnIndex();
				ft.setIndex(in);
				
				SnippetExtraction se = new SnippetExtraction(sheet);
				ft.setSn(se.cellSnippetBelong(cell, snippets));
				
				CellArrayExtraction cae = new CellArrayExtraction();
				CellArray ca = cae.getFeature(cell, allCAs);
				ft.setCa(ca);
				
				if (cluster.isSeedOrNot()) {
					ft.setCluster(cluster);
					
					if (!headers.isEmpty()){
						for (Header hd : headers){
							String hdStr = hd.toString();
							if (!featureVectorForClustering.contains(hdStr)) {
								featureVectorForClustering.add(hdStr);
							}
						}

//						if (O4_1) {
//							SpecialHeader specialHeader = null;
//							for (Header header : headers) {
//								if (header.getStrHeader().toLowerCase().contains("total") ||
//										header.getStrHeader().toLowerCase().contains("gross")) {
//									specialHeader = new SpecialHeader(header.getStrHeader(), header.getOrientation());
//									break;
//								}
//							}
//							if (specialHeader != null) {
//								ft.setSpecialHeader(specialHeader);
//								String specialHeaderStr = specialHeader.toString();
//								if (! featureVectorForClustering.contains(specialHeaderStr)) {
//									featureVectorForClustering.add(specialHeaderStr);
//								}
//							}
//						}
					}

					if (cellAlliance != null){
						for (Alliance alliance: cellAlliance){
							String allianceStr = alliance.toString();
							if ( ! featureVectorForClustering.contains(allianceStr)){
								featureVectorForClustering.add(allianceStr);
							}
						}
					}
					
					if (gaps!=null && !gaps.isEmpty()){
						Set<Gap> gps= new HashSet<Gap>();
						for (Map.Entry<Cell, Gap> entry:gaps.entrySet()){
								gps.add(entry.getValue());
						}
						ft.setGaps(gps);
						for (Gap gap: gps){
							if (gap!=null){
								String gapStr = gap.toString();
								if (!featureVectorForClustering.contains(gapStr)){
									featureVectorForClustering.add(gapStr);
								}
							}
						}
					}
					
					if (!featureVectorForClustering.contains("RowIndex" + in [0])){
						featureVectorForClustering.add("RowIndex" + in[0]);
					}
					if (!featureVectorForClustering.contains("ColIndex" + in [1])){
						featureVectorForClustering.add("ColIndex" + in[1]);	
					}
					
					if (!featureVectorForClustering.contains("RowBased") && ft.getColOrRowBased()==0){
						featureVectorForClustering.add("RowBased");
					}
					if (!featureVectorForClustering.contains("ColBased") && ft.getColOrRowBased()==1){
						featureVectorForClustering.add("ColBased");
					}
					
					Snippet st = ft.getSn();
					if (st!=null && featureVectorForClustering.contains(st.toString())){
						featureVectorForClustering.add(st.toString());
					}
					
					if (ca!=null && !featureVectorForClustering.contains(ca.toString())){
						featureVectorForClustering.add(ca.toString());
					}
				}
				else {
					Set<Gap> gapSet = new HashSet<Gap>();
					
					for (Cluster clusterTemp: seedCluster){
						GapExtraction geTmp = new GapExtraction(clusterTemp);
						geTmp.indexSet();
						gapSet.addAll(geTmp.cellGetGapFeature(cell));
					}
					
					ft.setGaps(gapSet);
				}
				
				cellFeatureList.add(ft);
			}
		}
	}
	
	private void FeatureExtractionFromDataCells(List<Cell> dataCells, List<Snippet> snippets, List<CellArray> allCAs) {
		AllianceExtraction ae = new AllianceExtraction(sheet);
		List<Alliance> allianceList = ae.allianceExtraction();
		
		for (Cell cell: dataCells){
			CellReference cellRef = new CellReference(cell);

			if (dataCellSet.contains(cellRef)) continue;

			cellRefsVector.add(cellRef);
			nonSeedCellRefs.add(cellRef);
			nonSeedCells.add(cell);
			
			CellFeature ft = new CellFeature (cellRef);
			ft.setCell(cell);
			
			HeaderExtraction he = new HeaderExtraction(sheet, cell);
			List<Header> headers = he.getFeature();
			ft.setHeaders(headers);

//			if (O4_1) {
//				SpecialHeader specialHeader = null;
//				for (Header header : headers) {
//					if (header.getStrHeader().toLowerCase().contains("total") ||
//							header.getStrHeader().toLowerCase().contains("gross")) {
//						specialHeader = new SpecialHeader(header.getStrHeader(), header.getOrientation());
//						break;
//					}
//				}
//				if (specialHeader != null) {
//					ft.setSpecialHeader(specialHeader);
//					String specialHeaderStr = specialHeader.toString();
//					if (! featureVectorForClustering.contains(specialHeaderStr)) {
//						featureVectorForClustering.add(specialHeaderStr);
//					}
//				}
//			}
			
			List<Alliance> cellAlliance = ae.getFeature(allianceList, cell);
			ft.setReferencedByAlliances(cellAlliance);
			
			int[] in = new int[2];
			in[0] = cell.getRowIndex();
			in[1] = cell.getColumnIndex();
			ft.setIndex(in);
			
			SnippetExtraction se = new SnippetExtraction(sheet);
			ft.setSn(se.cellSnippetBelong(cell, snippets));
			
			CellArrayExtraction cae = new CellArrayExtraction();
			CellArray ca = cae.getFeature(cell, allCAs);
			ft.setCa(ca);
			
			Set<Gap> gapSet = new HashSet<Gap>();
			
			for (Cluster clusterTemp: seedCluster){
				GapExtraction geTmp = new GapExtraction(clusterTemp);
				geTmp.indexSet();
				gapSet.addAll(geTmp.cellGetGapFeature(cell));
			}
			
			ft.setGaps(gapSet);

			cellFeatureList.add(ft);
		}
	}

	public List<CellReference> getCellRefsVector() {
		return cellRefsVector;
	}

	public List<Cluster> getSeedCluster() {
		return seedCluster;
	}

	public List<CellReference> getNonSeedCellRefs() {
		return nonSeedCellRefs;
	}

	public List<Cell> getNonSeedCells() {
		return nonSeedCells;
	}

	public List<String> getFeatureVectorForClustering() {
		return featureVectorForClustering;
	}

	public List<CellFeature> getCellFeatureList() {
		return cellFeatureList;
	}

	public List<Cluster> getClusterVector() {
		return clusterVector;
	}

}
