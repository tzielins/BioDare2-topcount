/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biodare.data.topcount.dom;

import ed.robust.dom.util.Pair;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author tzielins
 */
public class DataBlock {

    Map<Pair<Integer, Integer>, DataEntry> table;
    List<Integer> rows;
    List<Integer> cols;
    public DataBlock(Map<Pair<Integer, Integer>, DataEntry> table) {
        if (table.isEmpty()) throw new IllegalArgumentException("Table cannot be empty");
        this.table = table;
        
        Set<Integer> rowNrs = new HashSet<>();
        Set<Integer> colNrs = new HashSet<>();
        for (Pair<Integer,Integer> key : table.keySet()) {
            rowNrs.add(key.getLeft());
            colNrs.add(key.getRight());
        }
        rows = new ArrayList<>(rowNrs);
        cols = new ArrayList<>(colNrs);
        Collections.sort(rows);
        Collections.sort(cols);
    }

    public Map<Pair<Integer, Integer>, DataEntry> getTable() {
        return table;
    }

    public List<Integer> getRows() {
        return rows;
    }

    public List<Integer> getCols() {
        return cols;
    }
    
    public DataEntry getEntry(int row,int col) {
        return getEntry(new Pair<>(row,col));
    }
    
    public DataEntry getEntry(Pair<Integer,Integer> key) {
        return table.get(key);
    }
    
    public Set<Pair<Integer,Integer>> keys() {
        return table.keySet();
    }
}
