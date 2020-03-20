/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biodare.data.topcount;

import ed.biodare.data.topcount.dom.DataBlock;
import ed.biodare.data.topcount.dom.DataEntry;
import ed.biodare.data.topcount.dom.Token;
import ed.biodare.data.topcount.dom.TokenType;
import ed.robust.dom.util.Pair;
import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tzielins
 */
public class TopCountColumnParserTest {
    
    static final double EPS = 1E-6;
    
    public TopCountColumnParserTest() {
    }
    
    TopCountColumnParser instance;
    
    @Before
    public void setUp() {
        instance = new TopCountColumnParser();
    }

    @Test
    public void isSuitableFormatDistinguishes() throws Exception {
        
        Path dir = Paths.get(this.getClass().getResource("col1609").toURI());
        assertTrue(Files.isDirectory(dir));
        
        Path file = dir.resolve("1609.484");
        assertTrue(Files.isRegularFile(file));
        
        assertTrue(instance.isSuitableFormat(file));
        
        file = Paths.get(this.getClass().getResource("plate0626.txt").toURI());
        assertFalse(instance.isSuitableFormat(file));
        
        file = Paths.get(this.getClass().getResource("mixed.zip").toURI());
        assertFalse(instance.isSuitableFormat(file));        
        
    }
    
    @Test
    public void isGoodFirstLineDistinguishes() {
        
        String line = "  1 ,2067 ,10:15:21 AM ";
        assertTrue(instance.isGoodFirstLine(line));
        
        line = "  1 ,2067 ,1015";
        assertFalse(instance.isGoodFirstLine(line));
        
        line = "    	1	2	3	4	5	6	7	8	9	10	11	12";
        assertFalse(instance.isGoodFirstLine(line));
        
        line = "";
        assertFalse(instance.isGoodFirstLine(line));
    }
    
    @Test
    public void numberToCoordinatesWorks() {
        
        int[] numbers = {1,12,13,84,85,96};
        
        List<Pair<Integer,Integer>> expected = Arrays.asList(
            new Pair<>(1,1),
            new Pair<>(1,12),
            new Pair<>(2,1),
            new Pair<>(7,12),
            new Pair<>(8,1),
            new Pair<>(8,12)                
        );
        
        for (int i =0;i<numbers.length;i++) {
            int nr = numbers[i];
            Pair<Integer,Integer> exp = expected.get(i);
            Pair<Integer,Integer> res = instance.numberToCoordinates(nr);
            assertEquals(exp,res);            
        }
    }
    
    @Test
    public void rowsToDataBlockWorks() {
        
        List<Pair<Integer, DataEntry>> rows = Arrays.asList(
                new Pair<>(1,new DataEntry(1,2)),
                new Pair<>(13,new DataEntry(2,3)),
                new Pair<>(96,new DataEntry(3,4))
        );
        
        DataBlock res = instance.rowsToDataBlock(rows);
        
        assertEquals(Arrays.asList(1,2,8),res.getRows());
        assertEquals(Arrays.asList(1,12),res.getCols());
    }
    
    @Test
    public void readEntryRowReadCorrectRow() throws Exception {
        
        String in = 
" 95 ,  32 ,10:23:00 AM \n" +
" 96 ,  30 ,10:23:29 AM \n" +
"";
        BufferedReader buf = new BufferedReader(new StringReader(in));
        Tokenizer tokenizer = instance.makeTokenizer(buf);
        
        Pair<Integer, DataEntry> row = instance.readEntryRow(tokenizer, 94);
        
        assertEquals(95,(int)row.getLeft());
        assertEquals(32.0,row.getRight().getValue(),EPS);
        assertEquals(10.0+23.0/60.0,row.getRight().getTime(),EPS);
        
        assertTrue(tokenizer.getNext().is(TokenType.INTVAL));
    }
    
    @Test
    public void readEntryRowReturnsNullOnNewBlock() throws Exception {
        
        String in = 
" 1 ,  32 ,10:23:00 AM \n" +
" 2 ,  30 ,10:23:29 AM \n" +
"";
        BufferedReader buf = new BufferedReader(new StringReader(in));
        Tokenizer tokenizer = instance.makeTokenizer(buf);
        
        Pair<Integer, DataEntry> row = instance.readEntryRow(tokenizer, 96);
        
        assertNull(row);
        Token token = tokenizer.getNext();
        assertTrue(tokenizer.getNext().is(TokenType.INTVAL));        
        assertEquals(1,token.getIntVal());
    }
    
    @Test
    public void readDataBlockGivesNullOnEnd() throws Exception {
        
        String in = 
"  " +
"";
        
        BufferedReader buf = new BufferedReader(new StringReader(in));
        Tokenizer tokenizer = instance.makeTokenizer(buf);

        DataBlock block = instance.readDataBlock(tokenizer);
        
        assertNull(block);
        
        
    }
    
    @Test
    public void readDataBlockReadsBlock() throws Exception {
        
        String in = 
"  1 ,2067 ,10:15:21 AM \n" +
"  2 ,1948 ,10:18:24 AM \n" +
"  3 ,2062 ,10:18:37 AM \n" +
"  4 ,2440 ,10:21:40 AM \n" +
"  5 ,2383 ,10:21:52 AM \n" +
"  6 ,2420 ,10:24:55 AM \n" +
"  7 ,1565 ,10:15:21 AM \n" +
"  8 ,1567 ,10:18:24 AM \n" +
"  9 ,1397 ,10:18:37 AM \n" +
" 10 ,1438 ,10:21:40 AM \n" +
" 11 ,1052 ,10:21:52 AM \n" +
" 12 ,1475 ,10:24:55 AM \n" +
" 13 ,2116 ,10:15:34 AM \n" +
" 14 ,1904 ,10:18:12 AM \n" +
" 15 ,1985 ,10:18:49 AM \n" +
" 16 ,2301 ,10:21:27 AM \n" +
" 17 ,2214 ,10:22:05 AM \n" +
" 18 ,2373 ,10:24:43 AM \n" +
" 19 ,1452 ,10:15:34 AM \n" +
" 20 ,1504 ,10:18:12 AM \n" +
" 21 ,1227 ,10:18:49 AM \n" +
" 22 ,1137 ,10:21:27 AM \n" +
" 23 ,1035 ,10:22:05 AM \n" +
" 24 ,1512 ,10:24:43 AM \n" +
" 25 ,1984 ,10:15:46 AM \n" +
" 26 ,1976 ,10:18:00 AM \n" +
" 27 ,1944 ,10:19:01 AM \n" +
" 28 ,2220 ,10:21:15 AM \n" +
" 29 ,2036 ,10:22:17 AM \n" +
" 30 ,1901 ,10:24:31 AM \n" +
" 31 ,1359 ,10:15:46 AM \n" +
" 32 ,1266 ,10:18:00 AM \n" +
" 33 , 921 ,10:19:01 AM \n" +
" 34 , 963 ,10:21:15 AM \n" +
" 35 ,1065 ,10:22:17 AM \n" +
" 36 ,1243 ,10:24:31 AM \n" +
" 37 ,2230 ,10:15:58 AM \n" +
" 38 ,1801 ,10:17:47 AM \n" +
" 39 ,1802 ,10:19:13 AM \n" +
" 40 ,2320 ,10:21:03 AM \n" +
" 41 ,2065 ,10:22:29 AM \n" +
" 42 ,1678 ,10:24:18 AM \n" +
" 43 ,1326 ,10:15:58 AM \n" +
" 44 ,1066 ,10:17:47 AM \n" +
" 45 , 870 ,10:19:13 AM \n" +
" 46 , 919 ,10:21:03 AM \n" +
" 47 ,1196 ,10:22:29 AM \n" +
" 48 ,1253 ,10:24:18 AM \n" +
" 49 ,1333 ,10:16:10 AM \n" +
" 50 ,1263 ,10:17:35 AM \n" +
" 51 ,1191 ,10:19:25 AM \n" +
" 52 ,1613 ,10:20:51 AM \n" +
" 53 ,1306 ,10:22:41 AM \n" +
" 54 ,1523 ,10:24:06 AM \n" +
" 55 , 988 ,10:16:10 AM \n" +
" 56 , 768 ,10:17:35 AM \n" +
" 57 , 773 ,10:19:25 AM \n" +
" 58 , 823 ,10:20:51 AM \n" +
" 59 , 736 ,10:22:41 AM \n" +
" 60 , 799 ,10:24:06 AM \n" +
" 61 ,1352 ,10:16:22 AM \n" +
" 62 ,1192 ,10:17:23 AM \n" +
" 63 ,1212 ,10:19:37 AM \n" +
" 64 ,1577 ,10:20:38 AM \n" +
" 65 ,1255 ,10:22:53 AM \n" +
" 66 ,1445 ,10:23:54 AM \n" +
" 67 , 724 ,10:16:22 AM \n" +
" 68 , 772 ,10:17:23 AM \n" +
" 69 , 743 ,10:19:37 AM \n" +
" 70 , 695 ,10:20:38 AM \n" +
" 71 , 648 ,10:22:53 AM \n" +
" 72 , 613 ,10:23:54 AM \n" +
" 73 ,1204 ,10:16:34 AM \n" +
" 74 ,1214 ,10:17:11 AM \n" +
" 75 ,1189 ,10:19:49 AM \n" +
" 76 ,1563 ,10:20:26 AM \n" +
" 77 ,1159 ,10:23:05 AM \n" +
" 78 ,1449 ,10:23:42 AM \n" +
" 79 , 699 ,10:16:34 AM \n" +
" 80 , 531 ,10:17:11 AM \n" +
" 81 , 741 ,10:19:49 AM \n" +
" 82 , 750 ,10:20:26 AM \n" +
" 83 , 655 ,10:23:05 AM \n" +
" 84 , 676 ,10:23:42 AM \n" +
" 85 ,  65 ,10:16:46 AM \n" +
" 86 ,  72 ,10:16:58 AM \n" +
" 87 ,  47 ,10:20:01 AM \n" +
" 88 ,  48 ,10:20:14 AM \n" +
" 89 ,  39 ,10:23:17 AM \n" +
" 90 ,  39 ,10:23:29 AM \n" +
" 91 ,  58 ,10:16:46 AM \n" +
" 92 ,  65 ,10:16:58 AM \n" +
" 93 ,  39 ,10:20:01 AM \n" +
" 94 ,  41 ,10:20:14 AM \n" +
" 95 ,  32 ,10:23:17 AM \n" +
" 96 ,  30 ,10:23:29 AM \n" +
"";
        
        BufferedReader buf = new BufferedReader(new StringReader(in));
        Tokenizer tokenizer = instance.makeTokenizer(buf);

        DataBlock block = instance.readDataBlock(tokenizer);
        
        assertNotNull(block);
        
        for (int row = 1;row<=8;row++) {
            for (int col =1; col<=12;col++) {
                DataEntry entry = block.getEntry(row, col);
                assertNotNull(entry);
                assertTrue(entry.getValue() > 1);
                assertTrue(entry.getTime() > 10);
            }
        }
        
    }
    
    @Test
    public void readDataBlocksReadsFromFile() throws Exception {
        
        Path dir = Paths.get(this.getClass().getResource("col1609").toURI());
        assertTrue(Files.isDirectory(dir));
        
        Path file = dir.resolve("1609.484");
        assertTrue(Files.isRegularFile(file));
        
        try (BufferedReader in = TopCountParser.makeReader(file)) {
            List<DataBlock> blocks = instance.readDataBlocks(in);
            assertEquals(1,blocks.size());
            
            DataBlock block = blocks.get(0);
            assertEquals(2067, block.getEntry(1, 1).getValue(),EPS);
            assertEquals(1475, block.getEntry(1, 12).getValue(),EPS);
            assertEquals(30, block.getEntry(8, 12).getValue(),EPS);
        }
    }
    
    
}
