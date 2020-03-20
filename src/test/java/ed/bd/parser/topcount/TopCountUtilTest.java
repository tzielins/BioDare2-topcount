/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.bd.parser.topcount;

import ed.bd.parser.topcount.dom.DataBlock;
import ed.bd.parser.topcount.dom.DataEntry;
import ed.bd.parser.topcount.err.FormatException;
import ed.robust.dom.data.TimeSeries;
import ed.robust.dom.util.Pair;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author tzielins
 */
public class TopCountUtilTest {
    
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    
    static final double EPS = 1E-6;
    
    TopCountUtil instance;
    
    public TopCountUtilTest() {
    }
    
    @Before
    public void setUp() {
        instance = new TopCountUtil();
    }

    @Test
    public void getNamesOfFilePartsWorks() throws Exception {
        Path dir = testFolder.newFolder("parent").toPath();
        
        String name1 = "0023";
        String name2 = "0025";
        
        Files.createFile(dir.resolve(name1+".11"));
        Files.createFile(dir.resolve(name1+".01"));
        Files.createFile(dir.resolve(name2+".01"));
        Files.createFile(dir.resolve(name2+".02"));
        Files.createFile(dir.resolve(name2+".03"));
        Files.createFile(dir.resolve(name1+".03"));
        Files.createFile(dir.resolve(name1+".04"));
        
        List<String> exp = Arrays.asList(name1+".01",name1+".03",name1+".04",name1+".11");
        List<String> res = instance.getNamesOfFileParts(dir, name1);
        
        assertEquals(exp,res);
        
    }
    
    @Test
    public void getNamesOfFilePartsSkipsNotNumericalExtensions() throws Exception {
               
        Path dir = testFolder.newFolder("parent").toPath();
        
        String name1 = "0023";
        String name2 = "0025";
        
        Files.createFile(dir.resolve(name1+".11"));
        Files.createFile(dir.resolve(name1+".01"));
        Files.createFile(dir.resolve(name2+".01"));
        Files.createFile(dir.resolve(name2+".02"));
        Files.createFile(dir.resolve(name2+".03"));
        Files.createFile(dir.resolve(name1+".xls"));
        Files.createFile(dir.resolve(name1+".04"));
        
        List<String> exp = Arrays.asList(name1+".01",name1+".04",name1+".11");
        List<String> res = instance.getNamesOfFileParts(dir, name1);
        
        assertEquals(exp,res);
        
    }
    
    @Test
    public void platePatternAcceptsValidFname() {
        String plate = "0023";        
        String[] fNames = {"0023.1","0023.001","0023.101"};
        
        Pattern pattern = instance.platePattern(plate);
        
        for (String fName:fNames) {
            assertTrue(fName,pattern.matcher(fName).matches());
        }
    }
    
    @Test
    public void platePatternRejectsValidFname() {
        String plate = "0023";        
        String[] fNames = {"0022.1","0023.0.001","0023.xls"};
        
        Pattern pattern = instance.platePattern(plate);
        
        for (String fName:fNames) {
            assertFalse(fName,pattern.matcher(fName).matches());
        }
    } 
    
    @Test
    public void testJoinBlocks() throws Exception {
        //System.out.println("join blocks");
        
        
        List<DataBlock> blocks = new ArrayList<>();
        
        Map<Pair<Integer, Integer>,TimeSeries> expTable = new HashMap<>();
        Map<Pair<Integer, Integer>,TimeSeries> result = instance.join(blocks);
        assertEquals(expTable,result);
        
        Map<Pair<Integer, Integer>, DataEntry> table = new HashMap<>();        
        DataEntry entry = new DataEntry();
        entry.setTime(1);
        entry.setValue(12);
        table.put(new Pair<>(1,1),entry);
        entry = new DataEntry();
        entry.setTime(23);
        entry.setValue(1);
        table.put(new Pair<>(2,3),entry);        
        blocks.add(new DataBlock(table));
        
        table = new HashMap<>();     
        entry = new DataEntry();
        entry.setTime(2);
        entry.setValue(13);
        table.put(new Pair<>(1,1),entry);
        entry = new DataEntry();
        entry.setTime(25);
        entry.setValue(2);
        table.put(new Pair<>(2,3),entry);        
        blocks.add(new DataBlock(table));        
        
        table = new HashMap<>();        
        entry = new DataEntry();
        entry.setTime(3);
        entry.setValue(14);
        table.put(new Pair<>(1,1),entry);
        entry = new DataEntry();
        entry.setTime(27);
        entry.setValue(3);
        table.put(new Pair<>(2,3),entry);        
        blocks.add(new DataBlock(table));  
        
        TimeSeries ser = new TimeSeries();
        ser.add(1,12);
        ser.add(2,13);
        ser.add(3,14);        
        expTable.put(new Pair<>(1,1),ser);
        
        ser = new TimeSeries();
        ser.add(23,1);
        ser.add(25,2);
        ser.add(27,3);
        expTable.put(new Pair<>(2,3),ser);        
        
        result = instance.join(blocks);
        assertEquals(expTable,result);        
    }
    
    @Test
    public void testFixTimes() throws Exception {
        //System.out.println("fixTimes");
        

        boolean checkFrames = false;
        
        List<DataBlock> blocks = new ArrayList<>();
        
        Map<Pair<Integer, Integer>, DataEntry> table = new HashMap<>();        
        DataEntry entry = new DataEntry();
        entry.setTime(1);
        entry.setValue(12);
        table.put(new Pair<>(1,1),entry);
        entry = new DataEntry();
        entry.setTime(23);
        entry.setValue(1);
        table.put(new Pair<>(2,3),entry);        
        blocks.add(new DataBlock(table));
        
        table = new HashMap<>();     
        entry = new DataEntry();
        entry.setTime(2);
        entry.setValue(13);
        table.put(new Pair<>(1,1),entry);
        entry = new DataEntry();
        entry.setTime(22);
        entry.setValue(2);
        table.put(new Pair<>(2,3),entry);        
        blocks.add(new DataBlock(table));        
        
        table = new HashMap<>();        
        entry = new DataEntry();
        entry.setTime(3);
        entry.setValue(14);
        table.put(new Pair<>(1,1),entry);
        entry = new DataEntry();
        entry.setTime(1);
        entry.setValue(3);
        table.put(new Pair<>(2,3),entry);        
        blocks.add(new DataBlock(table));  
        
        List<DataBlock> expBlocks = new ArrayList<>();
        table = new HashMap<>();        
        entry = new DataEntry();
        entry.setTime(1);
        entry.setValue(12);
        table.put(new Pair<>(1,1),entry);
        entry = new DataEntry();
        entry.setTime(23);
        entry.setValue(1);
        table.put(new Pair<>(2,3),entry);        
        expBlocks.add(new DataBlock(table));        
        
        table = new HashMap<>();        
        entry = new DataEntry();
        entry.setTime(2);
        entry.setValue(13);
        table.put(new Pair<>(1,1),entry);
        entry = new DataEntry();
        entry.setTime(46);
        entry.setValue(2);
        table.put(new Pair<>(2,3),entry);        
        expBlocks.add(new DataBlock(table));        
        
        table = new HashMap<>();        
        entry = new DataEntry();
        entry.setTime(3);
        entry.setValue(14);
        table.put(new Pair<>(1,1),entry);
        entry = new DataEntry();
        entry.setTime(49);
        entry.setValue(3);
        table.put(new Pair<>(2,3),entry);        
        expBlocks.add(new DataBlock(table));        
        
        instance.fixTimes(blocks,checkFrames);
        for (int i = 0;i<blocks.size();i++) {
            DataBlock block = blocks.get(i);
            DataBlock expBlock = expBlocks.get(i);
            for (Pair<Integer,Integer> key : expBlock.keys()) {
                    DataEntry res = block.getEntry(key);
                    DataEntry exp = expBlock.getEntry(key);
                    assertEquals(exp.getTime(),res.getTime(),EPS);
                    assertEquals(exp.getValue(),res.getValue(),EPS);
                
            }
            
        }
    }
    
    @Test
    public void testValidateDataBlock() throws Exception {
        //System.out.println("validateDataBlock");
        
        boolean checkFrames = true;
        
        List<DataBlock> blocks = new ArrayList<>();
        try {
            instance.validateDataBlocks(blocks,checkFrames);
            fail("FormatExcpetion expected");
        } catch (FormatException e) {}
        
        
        Map<Pair<Integer, Integer>, DataEntry> table = new HashMap<>();
        table.put(new Pair<>(1,1),new DataEntry());
        
        blocks.add(new DataBlock(table));
        instance.validateDataBlocks(blocks,checkFrames);

        table = new HashMap<>();
        table.put(new Pair<>(1,2),new DataEntry());        
        blocks.add(new DataBlock(table));
        try {
            instance.validateDataBlocks(blocks,checkFrames);
            fail("FormatExcpetion expected");
        } catch (FormatException e) {}
        
        blocks.clear();
        table = new HashMap<>();
        table.put(new Pair<>(1,2),new DataEntry());        
        blocks.add(new DataBlock(table));
        table = new HashMap<>();
        table.put(new Pair<>(1,2),new DataEntry());        
        blocks.add(new DataBlock(table));
        instance.validateDataBlocks(blocks,checkFrames);
        
        blocks.clear();
        table = new HashMap<>();
        table.put(new Pair<>(1,2),new DataEntry());        
        table.put(new Pair<>(2,3),new DataEntry());        
        blocks.add(new DataBlock(table));
        table = new HashMap<>();
        table.put(new Pair<>(1,3),new DataEntry());        
        table.put(new Pair<>(2,2),new DataEntry());        
        blocks.add(new DataBlock(table));
        try {
            instance.validateDataBlocks(blocks,checkFrames);
            fail("FormatExcpetion expected");
        } catch (FormatException e) {}
        
        blocks.clear();
        table = new HashMap<>();
        table.put(new Pair<>(1,2),new DataEntry());        
        table.put(new Pair<>(2,3),new DataEntry());        
        blocks.add(new DataBlock(table));
        table = new HashMap<>();
        table.put(new Pair<>(1,2),new DataEntry());        
        table.put(new Pair<>(2,3),new DataEntry());        
        blocks.add(new DataBlock(table));
        instance.validateDataBlocks(blocks,checkFrames);
    }
    
    
    
}
