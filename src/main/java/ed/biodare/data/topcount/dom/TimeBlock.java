/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biodare.data.topcount.dom;

import ed.biodare.data.topcount.err.FormatException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tzielins
 */
public class TimeBlock {

    Map<Integer,TimeRow> rows;
    
    public TimeBlock(List<TimeRow> rows) throws FormatException {
        
        this.rows = new HashMap<>();
        for (TimeRow row : rows)
            this.rows.put(row.getRowNr(),row);
        
        if (this.rows.size() != rows.size())
            throw new FormatException("Duplicate row ids in time block");
    }

    public Map<Integer, TimeRow> getRows() {
        return rows;
    }

    public void setRows(Map<Integer, TimeRow> rows) {
        this.rows = rows;
    }
    
    
}
