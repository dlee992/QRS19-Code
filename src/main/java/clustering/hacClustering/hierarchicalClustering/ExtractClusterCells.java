package clustering.hacClustering.hierarchicalClustering;

import entity.Cluster;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.ArrayList;
import java.util.List;

public class  ExtractClusterCells{
	private HSSFSheet sheet = null;
	private String pointsInCluster = null;

	public ExtractClusterCells(HSSFSheet sheet){
		this.sheet = sheet;
	}
	
	public List<Cell> extractCells(Cluster cluster){
		List<Cell> cellList = new ArrayList<Cell>();
		String strPoint = "";
		
		for (String name : cluster
				.getChildrenName(cluster)) {
			
			int trimPosition = name
					.indexOf('!');
			int length = name.length();
		 strPoint = name.substring(
					trimPosition + 1, length)
					+ "," + strPoint;
			
			Cell cell = convertAddToCell(name);
			cellList.add(cell);
		}
		setPointsInCluster(strPoint);
	return cellList;
	}
	
public Cell convertAddToCell(String address) {
		CellReference cellRef = new CellReference (address);
		Short cellCol= cellRef.getCol();
		int cellRow = cellRef.getRow();
		Row row = sheet.getRow(cellRow);
		Cell cell = row.getCell(cellCol);
		return cell;
					}

public String getPointsInCluster(){
	return pointsInCluster;
}

public void setPointsInCluster(String pointsInCluster){
	this.pointsInCluster = pointsInCluster;
}

}

