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
public class ValueBlock {

    Map<Integer,ValueRow> rows;
    public ValueBlock(List<ValueRow> rows) throws FormatException {
        
        this.rows = new HashMap<>();
        for (ValueRow row : rows)
            this.rows.put(row.getRowNr(),row);
        
        if (this.rows.size() != rows.size())
            throw new FormatException("Duplicate row ids in value block");
    }

    public Map<Integer, ValueRow> getRows() {
        return rows;
    }
    
    
    
}
