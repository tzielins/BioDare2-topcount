/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.bd.parser.topcount;

import ed.bd.parser.topcount.dom.DataBlock;
import ed.bd.parser.topcount.dom.DataEntry;
import ed.bd.parser.topcount.dom.IntToken;
import ed.bd.parser.topcount.dom.TimeBlock;
import ed.bd.parser.topcount.dom.TimeRow;
import ed.bd.parser.topcount.dom.TimeToken;
import ed.bd.parser.topcount.dom.Token;
import ed.bd.parser.topcount.dom.ValueBlock;
import ed.bd.parser.topcount.dom.ValueRow;
import ed.bd.parser.topcount.err.DataFinished;
import ed.bd.parser.topcount.err.FormatException;
import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author tzielins
 */
public class TopCountPlateParserTest {
    
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();
    
    double EPS = 1E-6;
    public TopCountPlateParserTest() {
    }


    @Test
    public void isSuitableFormatDistinguishes() throws Exception {
        
        TopCountPlateParser instance = new TopCountPlateParser();
        Path dir = Paths.get(this.getClass().getResource("col1609").toURI());
        assertTrue(Files.isDirectory(dir));
        
        Path file = dir.resolve("1609.484");
        assertTrue(Files.isRegularFile(file));
        
        assertFalse(instance.isSuitableFormat(file));
        
        file = Paths.get(this.getClass().getResource("plate0626.txt").toURI());
        assertTrue(instance.isSuitableFormat(file));
        
        file = Paths.get(this.getClass().getResource("mixed.zip").toURI());
        assertFalse(instance.isSuitableFormat(file));        
    }
        
    /**
     * Test of consumeHeader method, of class TopCountParser.
     */
    @Test
    public void testConsumeHeader() throws Exception {
        System.out.println("consumeHeader");
        
        String in = "    	1	2	3	4	5	6	7	8	9	10	11	12";
        BufferedReader buf = new BufferedReader(new StringReader(in));
        Tokenizer tokenizer = new SimpleTokenizer(buf);        
        
        TopCountPlateParser instance = new TopCountPlateParser();
        int expResult = 12;
        int result = instance.consumeHeader(tokenizer);
        assertEquals(expResult, result);
        
        in = "    												\n" +
"    												\n" +
"    	1	2	3	4	5	6	7	8	9	10	11	12\n" +
"  A 	11:57:47 AM	   	11:58:21 AM	   	11:58:54 AM	   	11:57:47 AM	   	11:58:21 AM	    	11:58:54 AM	    ";

        buf = new BufferedReader(new StringReader(in));
        tokenizer = new SimpleTokenizer(buf);         
        expResult = 12;
        result = instance.consumeHeader(tokenizer);
        assertEquals(expResult, result);
        
        in = "  A 	2178	   	1021	   	685	   	4244	   	935	    	308	    \n" +
"  B 	3968	   	1277	   	685	   	3696	   	1451	    	288	";
        buf = new BufferedReader(new StringReader(in));
        tokenizer = new SimpleTokenizer(buf);         
        expResult = 12;
        try {
            result = instance.consumeHeader(tokenizer);
            fail("Format exceptin expected");
            assertEquals(expResult, result);
        } catch (FormatException e) {
            
        }
        
        in = "\t\t   \t";
        buf = new BufferedReader(new StringReader(in));
        tokenizer = new SimpleTokenizer(buf);         
        expResult = 12;
        try {
            result = instance.consumeHeader(tokenizer);
            fail("Data finish expected");
            assertEquals(expResult, result);
        } catch (DataFinished e) {
            
        }                
    }
    
    @Test
    public void testReadValueRow() throws Exception {
        System.out.println("ReadValueRow");
        
        String in = "  C 	     10149 	      6310 	     10704 ";
        BufferedReader buf = new BufferedReader(new StringReader(in));
        Tokenizer tokenizer = new SimpleTokenizer(buf);        
        
        String expRowId = "C";
        int expRowNr = 3;
        List<? extends Token> expColumns = Arrays.asList(new IntToken(10149),new IntToken(6310),new IntToken(10704));
        TopCountPlateParser instance = new TopCountPlateParser();
        ValueRow result = instance.readValueRow(tokenizer);
        
        assertEquals(expRowId,result.getRowId());
        assertEquals(expRowNr,result.getRowNr());
        assertEquals(expColumns,result.getColumns());
        
        in = "    	     1      	     2      	     3      	 ";
        buf = new BufferedReader(new StringReader(in));
        tokenizer = new SimpleTokenizer(buf);        
        
        result = instance.readValueRow(tokenizer);
        assertNull(result);
        
        
        in = "  E 	07:46:12 PM 	07:47:02 PM 	07:46:12 PM";
        buf = new BufferedReader(new StringReader(in));
        tokenizer = new SimpleTokenizer(buf);        
        
        try {
            result = instance.readValueRow(tokenizer);
            fail("Format exception expected");
            assertEquals(expRowId,result.getRowId());
            assertEquals(expRowNr,result.getRowNr());
            assertEquals(expColumns,result.getColumns());
        } catch (FormatException e) {}

        in = "  B 	5033	   	1349";
        buf = new BufferedReader(new StringReader(in));
        tokenizer = new SimpleTokenizer(buf);        
   
        expRowId = "B";
        expRowNr = 2;
        expColumns = Arrays.asList(new IntToken(5033),Token.newEmpty(),new IntToken(1349));
        result = instance.readValueRow(tokenizer);
        
        assertEquals(expRowId,result.getRowId());
        assertEquals(expRowNr,result.getRowNr());
        assertEquals(expColumns,result.getColumns());
        
    }    
    
    
    @Test
    public void testReadTimeRow() throws Exception {
        System.out.println("ReadTimeRow");
        
        String in = "  E 	07:46:12 PM 	07:47:02 PM     ";
        BufferedReader buf = new BufferedReader(new StringReader(in));
        Tokenizer tokenizer = new SimpleTokenizer(buf);        
        
        String expRowId = "E";
        int expRowNr = 5;
        List<? extends Token> expColumns = Arrays.asList(new TimeToken("07:46:12 PM",12+7+46.0/60.0+12.0/60.0/60.0),new TimeToken("07:47:02 PM",12+7+47.0/60.0+2/60.0/60.0));
        TopCountPlateParser instance = new TopCountPlateParser();
        TimeRow result = instance.readTimeRow(tokenizer);
        
        assertEquals(expRowId,result.getRowId());
        assertEquals(expRowNr,result.getRowNr());
        assertEquals(expColumns,result.getColumns());
 
        in = "  A 	11:57:47 AM	   	11:58:21 AM	   ";
        buf = new BufferedReader(new StringReader(in));
        tokenizer = new SimpleTokenizer(buf);        
        
        expRowId = "A";
        expRowNr = 1;
        expColumns = Arrays.asList(new TimeToken("11:57:47 AM",11+57.0/60.0+47/60.0/60.0),Token.newEmpty(),new TimeToken("11:58:21 AM",11+58/60.0+21/60.0/60.0),Token.newEmpty());
        result = instance.readTimeRow(tokenizer);
        
        assertEquals(expRowId,result.getRowId());
        assertEquals(expRowNr,result.getRowNr());
        assertEquals(expColumns,result.getColumns());        
         
        in = "    	     1      	     2      	     3      	 ";
        buf = new BufferedReader(new StringReader(in));
        tokenizer = new SimpleTokenizer(buf);        
        
        result = instance.readTimeRow(tokenizer);
        assertNull(result);

        in = "    \n" +
        "    ";
        buf = new BufferedReader(new StringReader(in));
        tokenizer = new SimpleTokenizer(buf);        
        
        result = instance.readTimeRow(tokenizer);
        assertNull(result);
        
        
        in = "F 	     15467 	      9805 	     18307 	      5938 ";
        buf = new BufferedReader(new StringReader(in));
        tokenizer = new SimpleTokenizer(buf);        
        
        try {
            result = instance.readTimeRow(tokenizer);
            fail("Format exception expected");
            assertEquals(expRowId,result.getRowId());
            assertEquals(expRowNr,result.getRowNr());
            assertEquals(expColumns,result.getColumns());
        } catch (FormatException e) {}
        
    }    
    
    @Test
    public void testReadValueBlock() throws Exception {
        System.out.println("ReadValueBlcok");
        
        TopCountPlateParser instance = new TopCountPlateParser();
        
        String in = "    	     1     	     2     	     3     	     4     	     5     	     6     	     7     	     8     	     9     	    10     	    11     	    12 \n" +
"  A 	     17275 	       205 	       187 	     21592 	     17144 	     20251 	     34379 	      8149 	      6699 	      9693 	       341 	     14584 \n" +
"  B 	     44589 	     14728 	       168 	     46152 	       163 	       360 	       336 	     14536 	       336 	     20643 	     49437 	     17824 \n" +
"  C 	      9693 	      4413 	       157 	     18525 	     19336 	     16960 	       152 	       232 	     25835 	     27203 	     26952 	       240 ";
        BufferedReader buf = new BufferedReader(new StringReader(in));
        Tokenizer tokenizer = new SimpleTokenizer(buf);        
        
        int expRows = 3;
        int expCols = 12;
        
        ValueBlock block = instance.readValueBlock(tokenizer);
        assertNotNull(block);
        assertEquals(expRows,block.getRows().size());
        assertEquals(expCols,block.getRows().get(1).getColumns().size());
        
        in = "    	1	2	3	4	5	6	7	8	9	10	11	12\n" +
"  A 	2687	   	933	   	508	   	6486	   	1063	    	256	    \n" +
"  B 	5033	   	1349	   	613	   	5055	   	1594	    	196	    \n" +
"  C 	3503	   	1129	   	917	   	3037	   	1682	    	719	    \n" +
"  D 	2927	   	1157	   	785	   	2454	   	1391	    	655	    \n" +
"  E 	2819	   	1105	   	705	   	2897	   	1307	    	659	    \n" +
"  F 	2334	   	865	   	605	   	2569	   	911	    	719	    \n" +
"  G 	1441	   	773	   	476	   	1766	   	867	    	555	    \n" +
"  H 	1602	   	729	   	472	   	1786	   	911	    	767	    \n" +
"    												";
        buf = new BufferedReader(new StringReader(in));
        tokenizer = new SimpleTokenizer(buf);        
        
        expRows = 8;
        expCols = 12;
        
        block = instance.readValueBlock(tokenizer);
        assertNotNull(block);
        assertEquals(expRows,block.getRows().size());
        assertEquals(expCols,block.getRows().get(1).getColumns().size());
        
        in = "												";
        buf = new BufferedReader(new StringReader(in));
        tokenizer = new SimpleTokenizer(buf);        
        
        expRows = 8;
        expCols = 12;
        
        block = instance.readValueBlock(tokenizer);
        assertNull(block);
        
        in = "    												\n" +
"    	1	2	3	4	5	6	7	8	9	10	11	12\n" +
"  A 	12:54:34 PM	   	12:55:08 PM	   	12:55:41 PM	   	12:54:34 PM	   	12:55:08 PM	    	12:55:41 PM	    \n" +
"  B 	12:54:38 PM	   	12:55:12 PM	   	12:55:45 PM	   	12:54:38 PM	   	12:55:12 PM	    	12:55:45 PM	    \n" +
"  C 	12:54:42 PM	   	12:55:16 PM	   	12:55:49 PM	   	12:54:42 PM	   	12:55:16 PM	    	12:55:49 PM	    ";
        buf = new BufferedReader(new StringReader(in));
        tokenizer = new SimpleTokenizer(buf);        
        
        expRows = 8;
        expCols = 12;

        try {
        block = instance.readValueBlock(tokenizer);
        fail("Format exception expected");
        assertNull(block);
        
        } catch (FormatException e) {}
        
    }    
    
    
    @Test
    public void testReadTimeBlock() throws Exception {
        System.out.println("ReadTimeBlcok");
        
        TopCountPlateParser instance = new TopCountPlateParser();
        
        String in = "    												\n" +
"    	1	2	3	4	5	6	7	8	9	10	11	12\n" +
"  A 	12:54:34 PM	   	12:55:08 PM	   	12:55:41 PM	   	12:54:34 PM	   	12:55:08 PM	    	12:55:41 PM	    \n" +
"  B 	12:54:38 PM	   	12:55:12 PM	   	12:55:45 PM	   	12:54:38 PM	   	12:55:12 PM	    	12:55:45 PM	    \n" +
"  C 	12:54:42 PM	   	12:55:16 PM	   	12:55:49 PM	   	12:54:42 PM	   	12:55:16 PM	    	12:55:49 PM	    ";
        BufferedReader buf = new BufferedReader(new StringReader(in));
        Tokenizer tokenizer = new SimpleTokenizer(buf);        
        
        int expRows = 3;
        int expCols = 12;
        
        TimeBlock block = instance.readTimeBlock(tokenizer);
        assertNotNull(block);
        assertEquals(expRows,block.getRows().size());
        assertEquals(expCols,block.getRows().get(1).getColumns().size());
        
        in = "    	     1      	     2      	     3      	     4      	     5      	     6      	     7      	     8      	     9      	     10     	     11     	     12 \n" +
"  A 	01:03:10 PM 	01:04:26 PM 	01:04:31 PM 	01:05:47 PM 	01:05:53 PM 	01:07:09 PM 	01:03:10 PM 	01:04:26 PM 	01:04:31 PM 	01:05:47 PM 	01:05:53 PM 	01:07:09 PM \n" +
"  B 	01:03:15 PM 	01:04:21 PM 	01:04:36 PM 	01:05:42 PM 	01:05:58 PM 	01:07:04 PM 	01:03:15 PM 	01:04:21 PM 	01:04:36 PM 	01:05:42 PM 	01:05:58 PM 	01:07:04 PM \n" +
"  C 	01:03:20 PM 	01:04:16 PM 	01:04:41 PM 	01:05:37 PM 	01:06:03 PM 	01:06:59 PM 	01:03:20 PM 	01:04:16 PM 	01:04:41 PM 	01:05:37 PM 	01:06:03 PM 	01:06:59 PM \n" +
"  D 	01:03:25 PM 	01:04:10 PM 	01:04:46 PM 	01:05:32 PM 	01:06:08 PM 	01:06:54 PM 	01:03:25 PM 	01:04:10 PM 	01:04:46 PM 	01:05:32 PM 	01:06:08 PM 	01:06:54 PM \n" +
"  E 	01:03:30 PM 	01:04:05 PM 	01:04:51 PM 	01:05:27 PM 	01:06:13 PM 	01:06:49 PM 	01:03:30 PM 	01:04:05 PM 	01:04:51 PM 	01:05:27 PM 	01:06:13 PM 	01:06:49 PM \n" +
"  F 	01:03:34 PM 	01:04:00 PM 	01:04:56 PM 	01:05:22 PM 	01:06:18 PM 	01:06:43 PM 	01:03:34 PM 	01:04:00 PM 	01:04:56 PM 	01:05:22 PM 	01:06:18 PM 	01:06:43 PM \n" +
"  G 	01:03:39 PM 	01:03:55 PM 	01:05:01 PM 	01:05:17 PM 	01:06:23 PM 	01:06:38 PM 	01:03:39 PM 	01:03:55 PM 	01:05:01 PM 	01:05:17 PM 	01:06:23 PM 	01:06:38 PM \n" +
"  H 	01:03:44 PM 	01:03:50 PM 	01:05:06 PM 	01:05:12 PM 	01:06:27 PM 	01:06:33 PM 	01:03:44 PM 	01:03:50 PM 	01:05:06 PM 	01:05:12 PM 	01:06:27 PM 	01:06:33 PM ";
        buf = new BufferedReader(new StringReader(in));
        tokenizer = new SimpleTokenizer(buf);        
        
        expRows = 8;
        expCols = 12;
        
        block = instance.readTimeBlock(tokenizer);
        assertNotNull(block);
        assertEquals(expRows,block.getRows().size());
        assertEquals(expCols,block.getRows().get(1).getColumns().size());
        
        in = "												";
        buf = new BufferedReader(new StringReader(in));
        tokenizer = new SimpleTokenizer(buf);        
        
        expRows = 8;
        expCols = 12;
        
        try {
        block = instance.readTimeBlock(tokenizer);
        fail("Format exception expected");
        assertNull(block);
        
        } catch (FormatException e) {}
        
        in = "    	     1     	     2     	     3     	     4     	     5     	     6     	     7     	     8     	     9     	    10     	    11     	    12 \n" +
"  A 	      7464 	       120 	       216 	     17525 	     20245 	     24597 	     46893 	       245 	     33475 	       211 	       269 	     10595 \n" +
"  B 	     62139 	     24197 	       245 	     89856 	       272 	       259 	       296 	     26531 	       352 	     36059 	     48051 	     14661 \n" +
"  C 	     15341 	     48824 	       219 	      2101 	     45859 	     34461 	       269 	       339 	     36779 	     12499 	     78032 	       280 ";
        buf = new BufferedReader(new StringReader(in));
        tokenizer = new SimpleTokenizer(buf);        
        
        expRows = 8;
        expCols = 12;

        try {
        block = instance.readTimeBlock(tokenizer);
        fail("Format exception expected");
        assertNull(block);
        
        } catch (FormatException e) {}
        
    }        
    
    
    @Test
    public void testDataBlock() throws Exception {
        System.out.println("DataBlock");
        
        TopCountPlateParser instance = new TopCountPlateParser();
        
        String in = "";
        BufferedReader buf = new BufferedReader(new StringReader(in));
        Tokenizer tokenizer = new SimpleTokenizer(buf); 

        DataBlock block = instance.readDataBlock(tokenizer);
        assertNull(block);
        
        in = "    	     1     	     2     	     3     	     4     	     5     	     6     	     7     	     8     	     9     	    10     	    11     	    12 \n" +
"  A 	      8750 	     11301 	      6870 	     16723 	      3837 	     12021 	      4890 	      7382 	      6456 	     10189 	     10672 	      7176 \n" +
"  B 	     11061 	      9408 	     11051 	     13392 	     17450 	      6493 	     14920 	      8304 	     14445 	     13323 	     12262 	     12134 \n" +
"  C 	     10149 	      6310 	     10704 	      8549 	     13726 	     10664 	     10814 	     12629 	      7003 	     13344 	     13077 	      8414 \n" +
"  D 	      8078 	      9917 	      4762 	      8936 	      3658 	     11117 	     12323 	     11782 	      7533 	     17515 	      9162 	      7142 \n" +
"  E 	     16477 	     10128 	      4386 	      7022 	      6234 	      7248 	      7872 	      8034 	      6981 	     12266 	     19155 	      7470 \n" +
"  F 	     15467 	      9805 	     18307 	      5938 	     10909 	      8216 	      3546 	     12262 	      4584 	     16650 	      9250 	     12352 \n" +
"  G 	      5090 	      6920 	      9646 	     12630 	     15398 	     15816 	     12355 	     10416 	     11632 	      4392 	      9707 	      6381 \n" +
"  H 	      8246 	      7899 	      8504 	      7000 	     10754 	      9210 	      9659 	     13995 	      5030 	     10970 	     22734 	     14594 \n" +
"    \n" +
"    \n" +
"    	     1      	     2      	     3      	     4      	     5      	     6      	     7      	     8      	     9      	     10     	     11     	     12 \n" +
"  A 	07:45:44 PM 	07:47:31 PM 	07:45:44 PM 	07:47:31 PM 	07:45:44 PM 	07:47:31 PM 	07:45:44 PM 	07:47:31 PM 	07:45:44 PM 	07:47:31 PM 	07:45:44 PM 	07:47:31 PM \n" +
"  B 	07:45:51 PM 	07:47:24 PM 	07:45:51 PM 	07:47:24 PM 	07:45:51 PM 	07:47:24 PM 	07:45:51 PM 	07:47:24 PM 	07:45:51 PM 	07:47:24 PM 	07:45:51 PM 	07:47:24 PM \n" +
"  C 	07:45:58 PM 	07:47:17 PM 	07:45:58 PM 	07:47:17 PM 	07:45:58 PM 	07:47:17 PM 	07:45:58 PM 	07:47:17 PM 	07:45:58 PM 	07:47:17 PM 	07:45:58 PM 	07:47:17 PM \n" +
"  D 	07:46:05 PM 	07:47:10 PM 	07:46:05 PM 	07:47:10 PM 	07:46:05 PM 	07:47:10 PM 	07:46:05 PM 	07:47:10 PM 	07:46:05 PM 	07:47:10 PM 	07:46:05 PM 	07:47:10 PM \n" +
"  E 	07:46:12 PM 	07:47:02 PM 	07:46:12 PM 	07:47:02 PM 	07:46:12 PM 	07:47:02 PM 	07:46:12 PM 	07:47:02 PM 	07:46:12 PM 	07:47:02 PM 	07:46:12 PM 	07:47:02 PM \n" +
"  F 	07:46:19 PM 	07:46:55 PM 	07:46:19 PM 	07:46:55 PM 	07:46:19 PM 	07:46:55 PM 	07:46:19 PM 	07:46:55 PM 	07:46:19 PM 	07:46:55 PM 	07:46:19 PM 	07:46:55 PM \n" +
"  G 	07:46:26 PM 	07:46:48 PM 	07:46:26 PM 	07:46:48 PM 	07:46:26 PM 	07:46:48 PM 	07:46:26 PM 	07:46:48 PM 	07:46:26 PM 	07:46:48 PM 	07:46:26 PM 	07:46:48 PM \n" +
"  H 	07:46:33 PM 	07:46:41 PM 	07:46:33 PM 	07:46:41 PM 	07:46:33 PM 	07:46:41 PM 	07:46:33 PM 	07:46:41 PM 	07:46:33 PM 	07:46:41 PM 	07:46:33 PM 	07:46:41 PM \n" +
"\n" +
"    	     1     	     2     	     3     	     4     	     5     	     6     	     7     	     8     	     9     	    10     	    11     	    12 ";
        buf = new BufferedReader(new StringReader(in));
        tokenizer = new SimpleTokenizer(buf); 

        block = instance.readDataBlock(tokenizer);
        assertNotNull(block);
        
        assertEquals(12,block.getCols().size());
        assertEquals(8,block.getRows().size());
        
        for (int row : block.getRows()) {
            for (int col : block.getCols()) {
                DataEntry entry = block.getEntry(row, col);
                assertNotNull(entry);
                assertTrue(entry.getTime() > 0);
                assertTrue(entry.getTime() < 24);
                assertTrue(entry.getValue() > 0);                
            }
        }
        
        in = "    	1	2	3	4	5	6	7	8	9	10	11	12\n" +
"  A 	1814	   	949	   	605	   	3449	   	735	    	232	    \n" +
"  B 	2995	   	933	   	597	   	2997	   	1027	    	236	    \n" +
"  C 	2162	   	861	   	617	   	2202	   	1163	    	611	    \n" +
"  D 	1898	   	821	   	537	   	1682	   	923	    	647	    \n" +
"  E 	1782	   	749	   	573	   	1894	   	915	    	627	    \n" +
"  F 	1606	   	741	   	557	   	1718	   	651	    	603	    \n" +
"  G 	1133	   	689	   	476	   	1371	   	711	    	543	    \n" +
"  H 	1277	   	653	   	452	   	1558	   	767	    	555	    \n" +
"    												\n" +
"    												\n" +
"    	1	2	3	4	5	6	7	8	9	10	11	12\n" +
"  A 	11:21:37 PM	   	11:22:10 PM	   	11:22:43 PM	   	11:21:37 PM	   	11:22:10 PM	    	11:22:43 PM	    \n" +
"  B 	11:21:41 PM	   	11:22:14 PM	   	11:22:47 PM	   	11:21:41 PM	   	11:22:14 PM	    	11:22:47 PM	    \n" +
"  C 	11:21:44 PM	   	11:22:18 PM	   	11:22:51 PM	   	11:21:44 PM	   	11:22:18 PM	    	11:22:51 PM	    \n" +
"  D 	11:21:48 PM	   	11:22:22 PM	   	11:22:55 PM	   	11:21:48 PM	   	11:22:22 PM	    	11:22:55 PM	    \n" +
"  E 	11:21:52 PM	   	11:22:26 PM	   	11:22:59 PM	   	11:21:52 PM	   	11:22:26 PM	    	11:22:59 PM	    \n" +
"  F 	11:21:56 PM	   	11:22:30 PM	   	11:23:03 PM	   	11:21:56 PM	   	11:22:30 PM	    	11:23:03 PM	    \n" +
"  G 	11:22:00 PM	   	11:22:34 PM	   	11:23:07 PM	   	11:22:00 PM	   	11:22:34 PM	    	11:23:07 PM	    \n" +
"  H 	11:22:04 PM	   	11:22:38 PM	   	11:23:11 PM	   	11:22:04 PM	   	11:22:38 PM	    	11:23:11 PM	    \n" +
"\n" +
"    	1	2	3	4	5	6	7	8	9	10	11	12\n" +
"  A 	1538	   	873	   	613	   	2961	   	751	    	248	    \n" +
"  B 	2679	   	941	   	613	   	2753	   	995	    	236	    ";
        buf = new BufferedReader(new StringReader(in));
        tokenizer = new SimpleTokenizer(buf); 

        block = instance.readDataBlock(tokenizer);
        assertNotNull(block);
        
        assertEquals(6,block.getCols().size());
        assertEquals(8,block.getRows().size());
        List<Integer> expCols = Arrays.asList(1,3,5,7,9,11);
        assertEquals(expCols,block.getCols());
        
        for (int row : block.getRows()) {
            for (int col : block.getCols()) {
                DataEntry entry = block.getEntry(row, col);
                assertNotNull(entry);
                assertTrue(entry.getTime() > 0);
                assertTrue(entry.getTime() < 24);
                assertTrue(entry.getValue() > 0);                
            }
        }

        //assertTrue(false);
    }
    
    
    
    
   
    
    
}