/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biodare.data.topcount;


import ed.biodare.data.topcount.dom.DataBlock;
import ed.biodare.data.topcount.dom.DataEntry;
import ed.biodare.data.topcount.err.FormatException;
import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.util.Pair;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author tzielins
 */
public class TopCountUtil {
    
    public List<String> getNamesOfFileParts(Path dir, String plateNr) throws IOException {
        
        final String prefix = plateNr+".";
        final int pLength = prefix.length();
        
        final Pattern pattern = platePattern(plateNr);
        
        return Files.list(dir)
                .map( p -> p.getFileName().toString())
                .filter( n-> pattern.matcher(n).matches())
                .sorted(
                        (n1,n2) -> {
                            Integer d1 = Integer.parseInt(n1.substring(pLength));
                            Integer d2 = Integer.parseInt(n2.substring(pLength));
                            return d1.compareTo(d2);
                        }
                )
                .collect(Collectors.toList());
    }
    
    public final Pattern platePattern(String plateNr) {
        
        return Pattern.compile(plateNr+"\\.\\d+");
    }
    
    
    public Map<Pair<Integer,Integer>,TimeSeries> blocksToPlateMap(List<DataBlock> dataBlocks, boolean checkFramesCompletness) throws FormatException {
     
        validateDataBlocks(dataBlocks, checkFramesCompletness);
        
        fixTimes(dataBlocks, checkFramesCompletness);
        
        Map<Pair<Integer,Integer>,TimeSeries> plate = join(dataBlocks);
        
        return plate;
        
    }
    
    protected void validateDataBlocks(List<DataBlock> blocks, boolean checkFramesCompletness) throws FormatException {
        if (blocks.isEmpty()) throw new FormatException("No data blocks were read");
        DataBlock pattern = blocks.get(0);
        Set<Pair<Integer,Integer>> keys = pattern.keys();
        List<Integer> cols = pattern.getCols();
        List<Integer> rows = pattern.getRows();
        int ix = 1;
        for (DataBlock block : blocks) {
            if (checkFramesCompletness) {
                if (!cols.equals(block.getCols())) throw new FormatException("Mismatch in column numbers in the block "+ix);
                if (!rows.equals(block.getRows())) throw new FormatException("Mismatch in row numbers in the block "+ix);
                if (!keys.equals(block.getTable().keySet())) throw new FormatException("Mismatch in row/col numbers in the block "+ix);
            } else {
                if (!keys.containsAll(block.getTable().keySet())) {
                    Set<Pair<Integer,Integer>> extraKeys = new HashSet<>(block.getTable().keySet());
                    extraKeys.removeAll(keys);
                    throw new FormatException("Unexpected wells entries in the block "+ix+"; "+extraKeys);
                }
            }
            ix++;
        }
    } 
    
    protected void fixTimes(List<DataBlock> dataBlocks, boolean checkFramesCompletness) {
        
        Set<Pair<Integer,Integer>> keys = dataBlocks.get(0).keys();
        
        for (Pair<Integer,Integer> key : keys) {
                double inc = 0;
                double prev = 0;
                for (DataBlock block : dataBlocks) {
                    DataEntry entry = block.getEntry(key);
                    if (entry == null) {
                        if(checkFramesCompletness) throw new IllegalStateException("Missing time point detected for well: "+key);
                        else continue;
                    }
                    double time = entry.getTime();
                    if (time < prev) inc+=24;
                    prev = time;
                    entry.setTime(time+inc);
                }
            
        }
    }
    
    protected Map<Pair<Integer, Integer>, TimeSeries> join(List<DataBlock> dataBlocks) {
        
        Map<Pair<Integer, Integer>, TimeSeries> table = new TreeMap<>(fullPairComparator());
        if (dataBlocks.isEmpty()) return table;
        
        for (Pair<Integer,Integer> key : dataBlocks.get(0).keys()) table.put(key,new TimeSeries());
        
        for (DataBlock block : dataBlocks) {
            for (Pair<Integer,Integer> key : block.keys()) {
                TimeSeries ser = table.get(key);
                if (ser == null) throw new IllegalStateException("Unexpected entry for "+key+" in the middle of the data file");
                DataEntry entry = block.getEntry(key);
                ser.add(entry.getTime(),entry.getValue());
            }
        }
        return table;
        
    }    
    
    protected <L extends Comparable<L>,R extends Comparable<R>> Comparator<Pair<L,R>> fullPairComparator() {
    
        return Comparator.comparing( (Pair<L,R> pair) -> pair.getLeft()).thenComparing((Pair<L,R> pair) -> pair.getRight());
    }    
    
}
