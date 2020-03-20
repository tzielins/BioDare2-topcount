/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biodare.data.topcount;

import ed.biodare.data.topcount.dom.IntToken;
import ed.biodare.data.topcount.dom.TextToken;
import ed.biodare.data.topcount.dom.TimeToken;
import ed.biodare.data.topcount.dom.Token;
import ed.biodare.data.topcount.dom.TokenType;
import ed.biodare.data.topcount.err.FormatException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tzielins
 */
public class SimpleTokenizerTest {
    
    double EPS = 1E-6;
    public SimpleTokenizerTest() {
    }

    /**
     * Test of close method, of class SimpleTokenizer.
     */
    //@Test
    public void testClose() throws Exception {
        System.out.println("close");
        SimpleTokenizer instance = null;
        instance.close();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNext method, of class SimpleTokenizer.
     */
    @Test
    public void testGetNext() throws Exception {
        System.out.println("getNext");
        String in = "       328 \n" +
"    \n" +
"    \n" +
"    	     1";
        BufferedReader buf = new BufferedReader(new StringReader(in));
        SimpleTokenizer instance = new SimpleTokenizer(buf);
        
        List<Token> expResults = Arrays.asList(new IntToken(328),Token.newEOLN(),Token.newEmpty(),Token.newEOLN(),Token.newEmpty(),Token.newEOLN(),Token.newEmpty(),new IntToken(1),Token.newEOLN());
        List<Token> results = new ArrayList<>();
        for (;;) {
            Token result = instance.getNext();
            if (result.is(TokenType.EOF)) break;
            results.add(result);
        }
        assertEquals(expResults, results);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getLineNr method, of class SimpleTokenizer.
     */
    @Test
    public void testGetLineNr() throws IOException, FormatException {
        System.out.println("getLineNr");
        String in = "       328 \n" +
"    \n" +
"    \n" +
"    	     1";
        BufferedReader buf = new BufferedReader(new StringReader(in));
        SimpleTokenizer instance = new SimpleTokenizer(buf);
        int expResult = 0;
        int result = instance.getLineNr();
        assertEquals(expResult, result);
        
        Token token = instance.getNext();
        expResult = 1;
        result = instance.getLineNr();
        assertEquals(expResult, result); 
        
        while (!token.is(TokenType.EOF)) {
            result = instance.getLineNr();
            assertEquals(expResult, result);            
            if (token.is(TokenType.EOLn)) expResult++;
            token = instance.getNext();
        }

        assertEquals(4, result);            
        
    }

    /**
     * Test of readTokens method, of class SimpleTokenizer.
     */
    //@Test
    public void testReadTokens() throws Exception {
        System.out.println("readTokens");
        SimpleTokenizer instance = null;
        instance.readTokens();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of parseTokens method, of class SimpleTokenizer.
     */
    @Test
    public void testParseTokens() throws FormatException {
        System.out.println("parseTokens");
        SimpleTokenizer instance = new SimpleTokenizer((BufferedReader)null);
        String line = "";

        List expResult = Arrays.asList(Token.newEmpty(),Token.newEOLN());
        List result = instance.parseTokens(line);
        assertEquals(expResult, result);

        line = "    	1	2	3	4";
        expResult = Arrays.asList(Token.newEmpty(),new IntToken(1),new IntToken(2),new IntToken(3),new IntToken(4),Token.newEOLN());
        result = instance.parseTokens(line);
        assertEquals(expResult, result);        

        line = "  A 	2687	   	933	   	508	";
        expResult = Arrays.asList(new TextToken("A"),new IntToken(2687),Token.newEmpty(),new IntToken(933),Token.newEmpty(),new IntToken(508),Token.newEmpty(),Token.newEOLN());
        result = instance.parseTokens(line);
        assertEquals(expResult, result);        
        
        line = "  B 	01:00:00 AM	   	11:30:15 AM";
        expResult = Arrays.asList(new TextToken("B"),new TimeToken("01:00:00 AM",1),Token.newEmpty(),new TimeToken("11:30:15 AM",11.0+30.0/60+15.0/60.0/60.0),Token.newEOLN());
        result = instance.parseTokens(line);
        //System.out.println(expResult);
        //System.out.println(result);
        assertEquals(expResult, result);        
        //fail("The test case is a prototype.");
    }

    /**
     * Test of parseToken method, of class SimpleTokenizer.
     */
    @Test
    public void testParseEmptyToken() throws FormatException {
        System.out.println("parse Empty tokens");
        SimpleTokenizer instance = new SimpleTokenizer((BufferedReader)null);

        String part = "";
        String expText = "";
        TokenType expType = TokenType.EMPTY;
        
        Token result = instance.parseToken(part);        
        assertEquals(expType, result.type);
        assertEquals(expText,result.getTextVal());
        
        part = "     ";
        result = instance.parseToken(part);        
        assertEquals(expType, result.type);
        assertEquals(expText,result.getTextVal());        
        
        part = " \t\n";
        result = instance.parseToken(part);        
        assertEquals(expType, result.type);
        assertEquals(expText,result.getTextVal());        
        
        /*part = null;
        result = instance.parseToken(part);        
        assertEquals(expType, result.type);
        assertEquals(expText,result.getTextVal());        
        */ 
        //fail("The test case is a prototype.");
    }
    
    @Test
    public void testParseIntToken() throws FormatException {
        System.out.println("parse Int tokens");
        SimpleTokenizer instance = new SimpleTokenizer((BufferedReader)null);

        String part = "345";
        String expText = "345";
        int expVal = 345;
        TokenType expType = TokenType.INTVAL;
        
        Token result = instance.parseToken(part);        
        assertEquals(expType, result.type);
        assertEquals(expText,result.getTextVal());
        assertEquals(expVal, result.getIntVal());
        
        part = "     2      ";
        expText = "2";
        expVal = 2;
        result = instance.parseToken(part);        
        assertEquals(expType, result.type);
        assertEquals(expText,result.getTextVal());        
        assertEquals(expVal, result.getIntVal());
        
        part = "       115 ";
        expText = "115";
        expVal = 115;
        result = instance.parseToken(part);        
        assertEquals(expType, result.type);
        assertEquals(expText,result.getTextVal());        
        assertEquals(expVal, result.getIntVal());
        
        part = "       -115 ";
        expText = "-115";
        expVal = -115;
        result = instance.parseToken(part);        
        assertEquals(expType, result.type);
        assertEquals(expText,result.getTextVal());        
        assertEquals(expVal, result.getIntVal());
        //fail("The test case is a prototype.");
    }
    
    @Test
    public void testParseDoubleToken() throws FormatException {
        System.out.println("parse double tokens");
        SimpleTokenizer instance = new SimpleTokenizer((BufferedReader)null);

        String part = "345.12";
        String expText = "345.12";
        double expVal = 345.12;
        TokenType expType = TokenType.DOUBLEVAL;
        
        Token result = instance.parseToken(part);        
        assertEquals(expType, result.type);
        assertEquals(expText,result.getTextVal());
        assertEquals(expVal, result.getDoubleVal(),EPS);
        
        part = "     2.12      ";
        expText = "2.12";
        expVal = 2.12;
        result = instance.parseToken(part);        
        assertEquals(expType, result.type);
        assertEquals(expText,result.getTextVal());        
        assertEquals(expVal, result.getDoubleVal(),EPS);
        
        part = "       115.0 ";
        expText = "115.0";
        expVal = 115;
        result = instance.parseToken(part);        
        assertEquals(expType, result.type);
        assertEquals(expText,result.getTextVal());        
        assertEquals(expVal, result.getDoubleVal(),EPS);
        
        part = "       -115.01 ";
        expText = "-115.01";
        expVal = -115.01;
        result = instance.parseToken(part);        
        assertEquals(expType, result.type);
        assertEquals(expText,result.getTextVal());        
        assertEquals(expVal, result.getDoubleVal(),EPS);
        //fail("The test case is a prototype.");
    }
    
    @Test
    public void testParseTimeToken() throws FormatException {
        System.out.println("parse Time tokens");
        SimpleTokenizer instance = new SimpleTokenizer((BufferedReader)null);

        String part = "11:07:41 AM ";
        String expText = "11:07:41 AM";
        TokenType expType = TokenType.TIME;
        
        Token result = instance.parseToken(part);        
        assertEquals(expType, result.type);
        assertEquals(expText,result.getTextVal());
        
        part = "01:03:15 PM ";
        expText = "01:03:15 PM";
        result = instance.parseToken(part);        
        assertEquals(expType, result.type);
        assertEquals(expText,result.getTextVal());        
        
        part = "1:19:50 PM";
        expText = "1:19:50 PM";
        result = instance.parseToken(part);        
        assertEquals(expType, result.type);
        assertEquals(expText,result.getTextVal());  
        
        part = "12:06:59";
        expText = "12:06:59";
        result = instance.parseToken(part);        
        assertEquals(expType, result.type);
        assertEquals(expText,result.getTextVal());        

        part = "06:32:53";
        expText = "06:32:53";
        result = instance.parseToken(part);        
        assertEquals(expType, result.type);
        assertEquals(expText,result.getTextVal());        
        
        part = "22:27:14 ";
        expText = "22:27:14";
        result = instance.parseToken(part);        
        assertEquals(expType, result.type);
        assertEquals(expText,result.getTextVal());        
     }
    
    @Test
    public void testParseTextToken() throws FormatException {
        System.out.println("parse Text tokens");
        SimpleTokenizer instance = new SimpleTokenizer((BufferedReader)null);

        String part = "  A ";
        String expText = "A";
        TokenType expType = TokenType.TEXT;
        
        Token result = instance.parseToken(part);        
        assertEquals(expType, result.type);
        assertEquals(expText,result.getTextVal());
        
        part = "  D ";
        expText = "D";
        result = instance.parseToken(part);        
        assertEquals(expType, result.type);
        assertEquals(expText,result.getTextVal());        
        
     }

    /*@Test
    public void testAMDatePatterns() throws FormatException {
        
        SimpleTokenizer instance = new SimpleTokenizer((BufferedReader)null);
        
        String in = "";
        in = "05:40:47 PM";        
        Matcher m = instance.AMPattern.matcher(in);
        assertTrue(m.matches());
        
        in = "11:00:15 AM";        
        m = instance.AMPattern.matcher(in);
        assertTrue(m.matches());
        
        in = "12:55:43 PM";        
        m = instance.AMPattern.matcher(in);
        assertTrue(m.matches());

        in = "02:24:10 AM";        
        m = instance.AMPattern.matcher(in);
        assertTrue(m.matches());        
        
        in = "02:24:10 ";        
        m = instance.AMPattern.matcher(in);
        assertFalse(m.matches());        
        
        in = "02:24:10";        
        m = instance.AMPattern.matcher(in);
        assertFalse(m.matches());        
    }    
    
    @Test
    public void test24HDatePatterns() throws FormatException {
        
        SimpleTokenizer instance = new SimpleTokenizer((BufferedReader)null);
        
        String in = "";
        in = "23:23:32";        
        Matcher m = instance.H24Pattern.matcher(in);
        assertTrue(m.matches());
        
        in = "00:11:13";        
        m = instance.H24Pattern.matcher(in);
        assertTrue(m.matches());
        
        in = "12:06:59";        
        m = instance.H24Pattern.matcher(in);
        assertTrue(m.matches());
        
        in = "02:24:10 AM";        
        m = instance.H24Pattern.matcher(in);
        assertFalse(m.matches());        
        
        in = "08:37:40 PM";        
        m = instance.H24Pattern.matcher(in);
        assertFalse(m.matches());        
        
        in = "02:24:10 AM ";        
        m = instance.H24Pattern.matcher(in);
        assertFalse(m.matches());        
        
        in = "08:37:40 PM ";        
        m = instance.H24Pattern.matcher(in);
        assertFalse(m.matches());        
        
    } */  
    
    @Test
    public void testParseTime() throws FormatException {
        System.out.println("parse Time value");
        SimpleTokenizer instance = new SimpleTokenizer((BufferedReader)null);

        String part = "11:15:30 AM";
        Matcher m = instance.datePattern.matcher(part);
        double expTime = 11+15.0/60.0+30.0/(60.0*60.0);
        
        double result = instance.parseTime(part,m);
        assertEquals(expTime, result,EPS);
        
        part = "12:25:00 PM";
        expTime = 12+25.0/60.0;
        m = instance.datePattern.matcher(part);
        result = instance.parseTime(part,m);
        assertEquals(expTime, result,EPS);        
        
        part = "1:19:50 PM";
        expTime = 13+19.0/60.0+50/60.0/60.0;
        
        m = instance.datePattern.matcher(part);
        result = instance.parseTime(part,m);
        assertEquals(expTime, result,EPS);        
        
        part = "12:16:20 AM";
        expTime = 0+16.0/60.0+1.0/3.0/60.0;
        
        m = instance.datePattern.matcher(part);
        result = instance.parseTime(part,m);
        assertEquals(expTime, result,EPS);        
        
        part = "13:03:07";
        expTime = 13+3.0/60.0+7.0/60/60.0;
        
        m = instance.datePattern.matcher(part);
        result = instance.parseTime(part,m);
        assertEquals(expTime, result,EPS);        
        
        part = "23:23:32";
        expTime = 23+23.0/60.0+32.0/60.0/60.0;
        
        m = instance.datePattern.matcher(part);
        result = instance.parseTime(part,m);
        assertEquals(expTime, result,EPS);        
        
        part = "00:11:13";
        expTime = 00+11.0/60.0+13.0/60.0/60.0;
        
        m = instance.datePattern.matcher(part);
        result = instance.parseTime(part,m);
        assertEquals(expTime, result,EPS);        
    }    
    
    
    
}