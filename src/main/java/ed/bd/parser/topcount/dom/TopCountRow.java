/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.bd.parser.topcount.dom;

import java.util.List;

/**
 *
 * @author tzielins
 */
public class TopCountRow {
    int rowNr;
    String rowId;
    List<Token> columns;
    
    TopCountRow(int rowNr, String rowId, List<Token> columns) {
        this.rowNr = rowNr;
        this.rowId = rowId;
        this.columns = columns;        
    }

    public int getRowNr() {
        return rowNr;
    }

    public void setRowNr(int rowNr) {
        this.rowNr = rowNr;
    }

    public String getRowId() {
        return rowId;
    }

    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    public List<Token> getColumns() {
        return columns;
    }

    public void setColumns(List<Token> columns) {
        this.columns = columns;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(rowId).append(",");
        for (Token token : columns) sb.append(token.textVal).append(",");
        return sb.toString();
    }
    
    
}
