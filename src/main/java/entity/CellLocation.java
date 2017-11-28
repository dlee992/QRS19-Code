package entity;

/**
 * Created by nju-lida on 16-7-5.
 */
public class CellLocation {
    private String sheet_name;
    private int column;
    private int row;
    private boolean isCellArea;
    private String cellArea;

    public CellLocation(String sheet_name, int row, int column) {
        this.sheet_name = sheet_name;
        this.column = column;
        this.row = row;
        isCellArea = false;
    }

    public CellLocation(String sheet_name, String cellArea) {
        this.sheet_name = sheet_name;
        this.cellArea = cellArea;
        isCellArea = true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + column;
        result = prime * result + row;
        result = prime * result
                + ((sheet_name == null) ? 0 : sheet_name.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return  sheet_name + "!"+
                "row[" + row + "]"+
                "col[" + column + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CellLocation other = (CellLocation) obj;
        if (column != other.column)
            return false;
        if (row != other.row)
            return false;
        if (sheet_name == null) {
            if (other.sheet_name != null)
                return false;
        } else if (!sheet_name.equals(other.sheet_name))
            return false;
        return true;
    }

    public String getSheet_name() {
        return sheet_name;
    }

    public int getColumn() {
        return column;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }
}
