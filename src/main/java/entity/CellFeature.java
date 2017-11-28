package entity;

import featureExtraction.weakFeatureExtraction.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.util.CellReference;

import java.util.List;
import java.util.Set;

public class CellFeature {

	private CellReference cellReference = null;
	private Cell cell = null;
	private Cluster cluster = null;
	
	private List<Alliance> referencedByAlliances = null;
	private int colOrRowBased = -1;
	private int[] index = null;
	private Snippet sn = null;
	private CellArray ca = null;
	private Set<Gap> gaps = null;
	
	private List<Header> headers = null;
	private SpecialHeader specialHeader = null;
	
	private List<List<String>> tokens = null;
	private List<String> opTokens = null;
	private List<String> refTokens = null;
	private boolean scalarInFormula = false;
	
	public CellFeature(CellReference cellRef) {
		this.setCellReference(cellRef);
	}

	@Override
	public String toString() {
		return "cellReference=" + cellReference.formatAsString() +
                "\nheader=" + headers+
                "\nindex[0]=" + index[0] + " index[1]=" + index[1]+
                "\ncellAlliance=" + referencedByAlliances +
                "\nsnippet=" + sn +
                "\ncellArray=" + ca +
                "\ngaps=" + gaps;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((cellReference == null) ? 0 : cellReference.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CellFeature other = (CellFeature) obj;
		if (cellReference == null) {
			if (other.cellReference != null)
				return false;
		} else if (!cellReference.equals(other.cellReference))
			return false;
		return true;
	}

	public CellReference getCellReference() {
		return cellReference;
	}

	private void setCellReference(CellReference cellReference) {
		this.cellReference = cellReference;
	}

	public Cell getCell() {
		return cell;
	}

	public void setCell(Cell cell) {
		this.cell = cell;
	}

	public List<Header> getHeaders() {
		return headers;
	}

	public void setHeaders(List<Header> headers) {
		this.headers = headers;
	}

	public List<Alliance> getReferencedByAlliances() {
		return referencedByAlliances;
	}

	public void setReferencedByAlliances(List<Alliance> referencedByAlliances) {
		this.referencedByAlliances = referencedByAlliances;
	}

	public int getColOrRowBased() {
		return colOrRowBased;
	}

	public void setColOrRowBased(int colOrRowBased) {
		this.colOrRowBased = colOrRowBased;
	}

	public int[] getIndex() {
		return index;
	}

	public void setIndex(int[] index) {
		this.index = index;
	}

	public Snippet getSn() {
		return sn;
	}

	public void setSn(Snippet sn) {
		this.sn = sn;
	}

	public CellArray getCa() {
		return ca;
	}

	public void setCa(CellArray ca) {
		this.ca = ca;
	}

	public Cluster getCluster() {
		return cluster;
	}

	public void setCluster(Cluster cluster) {
		this.cluster = cluster;
	}

	public Set<Gap> getGaps() {
		return gaps;
	}

	public void setGaps(Set<Gap> gaps) {
		this.gaps = gaps;
	}

	public void setTokens(List<List<String>> tokens) {
		this.tokens = tokens;
	}

	public List<String> getOpTokens() {
		return opTokens;
	}

	public void setOpTokens(List<String> opTokens) {
		this.opTokens = opTokens;
	}

	public List<String> getRefTokens() {
		return refTokens;
	}

	public void setRefTokens(List<String> refTokens) {
		this.refTokens = refTokens;
	}

	public boolean isScalarInFormula() {
		return scalarInFormula;
	}

	public void setScalarInFormula(boolean scalarInFormula) {
		this.scalarInFormula = scalarInFormula;
	}

	public void setSpecialHeader(SpecialHeader specialHeader) {
		this.specialHeader = specialHeader;
	}
}
