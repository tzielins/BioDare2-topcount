/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biodare.data.topcount;


import ed.biodare.data.topcount.dom.DoubleToken;
import ed.biodare.data.topcount.dom.IntToken;
import ed.biodare.data.topcount.dom.TextToken;
import ed.biodare.data.topcount.dom.TimeToken;
import ed.biodare.data.topcount.dom.Token;
import ed.biodare.data.topcount.err.FormatException;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author tzielins
 */
public class SimpleTokenizer implements Tokenizer {

    final BufferedReader inStream;
    
    final Deque<Token> tokens;
    final Token EOFToken;
    final Token EOLnToken;
    final Token EmptyToken;
    
    final Pattern intPattern;
    final Pattern doublePattern;
    final Pattern datePattern;
    
    
    int lineNr;
    String SEP = "\t";

    SimpleTokenizer(File file, String SEP) throws IOException {
        this(Files.newBufferedReader(file.toPath(), Charset.forName("US-ASCII")),SEP);        
    }
    
    SimpleTokenizer(File file) throws IOException {
        this(file,"\t");
    }

    SimpleTokenizer(BufferedReader inStream) {
        this(inStream,"\t");
    }
    
    SimpleTokenizer(BufferedReader inStream,String SEP) {
        this.SEP = SEP;
        this.inStream = inStream;
        tokens = new LinkedList<>();
        EOFToken = Token.newEOF();
        EOLnToken = Token.newEOLN();
        EmptyToken = Token.newEmpty();
        
        intPattern = Pattern.compile("\\-?\\d+");
        doublePattern = Pattern.compile("\\-?\\d+\\.\\d+");
        datePattern = Pattern.compile("(\\d\\d?):(\\d\\d?):(\\d\\d?)\\s?(AM|PM)?");//Pattern.compile(getDatePattern());
        
        
        lineNr = 0;
    }

    
    @Override
    public void close() throws IOException {
        inStream.close();
    }

    @Override
    public Token getNext() throws IOException, FormatException {
        if (tokens.isEmpty()) readTokens();
        return tokens.removeFirst();
    }

    @Override
    public int getLineNr() {
        return lineNr;
    }

    void readTokens() throws IOException, FormatException {
        String line = inStream.readLine();
        //System.out.println(lineNr+1+": "+line);
        if (line == null) {
            tokens.add(EOFToken);
        } else {
            lineNr++;
            tokens.addAll(parseTokens(line));
        }
    }
    
    List<Token> parseTokens(String line) throws FormatException {
        
        List<Token> list = new ArrayList<>(12);
        String[] parts = line.split(SEP,-1);
        for (String part : parts) {
            list.add(parseToken(part));
        }
        list.add(EOLnToken);
        return list;
    }

    Token parseToken(String part) throws FormatException {
        part = part.trim();
        if (part.isEmpty()) return EmptyToken;
        
        if (intPattern.matcher(part).matches())
            return new IntToken(part);
        
        if (doublePattern.matcher(part).matches())
            return new DoubleToken(part);
        
        Matcher m = datePattern.matcher(part);
        if (m.matches())
            return new TimeToken(part,parseTime(part,m));
        return new TextToken(part);
    }

    @Override
    public void pushBack(Token token) {
        tokens.addFirst(token);
    }
    
    double parseTime(String part,Matcher m) throws FormatException {
        
        boolean H24 = false;
        boolean AM = false;
        if (!m.matches())
            throw new FormatException("Wrong time format: "+part);
        
        if (m.group(4) == null || m.group(4).isEmpty()) H24 = true;
        else AM = m.group(4).equals("AM");
        
        
        double hours = Double.parseDouble(m.group(1));
        double minutes = Double.parseDouble(m.group(2));
        double sec = Double.parseDouble(m.group(3));
        //System.out.println(hours+":"+minutes+":"+sec);
        hours += minutes/60.0;
        hours += sec/3600.0;
        
        if (!H24) {
            if (hours >= 12) {
                hours-=12;
            }
            if (!AM) hours+=12;
        }
        
        return hours;
    }
    

    

    
}
