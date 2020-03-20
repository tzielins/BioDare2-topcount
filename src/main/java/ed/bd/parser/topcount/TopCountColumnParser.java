/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.bd.parser.topcount;

import ed.bd.parser.topcount.dom.DataBlock;
import ed.bd.parser.topcount.dom.DataEntry;
import ed.bd.parser.topcount.dom.Token;
import ed.bd.parser.topcount.dom.TokenType;
import ed.bd.parser.topcount.err.FileException;
import ed.bd.parser.topcount.err.FormatException;
import ed.robust.dom.util.Pair;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 * @author tzielins
 */
class TopCountColumnParser implements TopCountParser {
   
    final TopCountUtil util = new TopCountUtil();
    
    
    int BLOCK_SIZE = 96;
    String SEP = ",";
    
    @Override
    public boolean isSuitableFormat(BufferedReader in) throws FileException {
        
        try {
            String line = in.readLine();
            return isGoodFirstLine(line);
        } catch (IOException e) {
            return false;
        }
    }
    
    protected boolean isGoodFirstLine(String line) {
        
        if (line == null || line.isEmpty()) return false;
        //Pattern pattern = Pattern.compile("\\s*\\d+\\s*,\\d+\\s*,\\d+:\\.*");
        Pattern pattern = Pattern.compile("\\s*\\d+\\s*,\\d+\\s*,\\d+:.*");
        return pattern.matcher(line).matches();
    }
    
    
    protected final Tokenizer makeTokenizer(BufferedReader inStream) {
        return new SimpleTokenizer(inStream, SEP);
    }
    
    @Override
    public List<DataBlock> readDataBlocks(BufferedReader inStream) throws FileException, FormatException {
        
        List<DataBlock> blocks = new ArrayList<>();
        
        try (Tokenizer tokenizer = makeTokenizer(inStream)) {
            
            for(;;) {
                DataBlock block = readDataBlock(tokenizer);
                if (block == null) break;
                blocks.add(block);
            }
        } catch (IOException e) {
            throw new FileException(e.getMessage(),e);
        }
        
        return blocks;
    }    

    protected DataBlock readDataBlock(Tokenizer tokenizer) throws IOException, FormatException {
        
        int last = 0;
        List<Pair<Integer,DataEntry>> rows = new ArrayList<>(BLOCK_SIZE);
        
        for (int i =0; i< BLOCK_SIZE; i++) {
            
            Pair<Integer,DataEntry> row = readEntryRow(tokenizer,last);
            if (row == null) break;
            rows.add(row);
            last = row.getLeft();
        }
        
        if (rows.isEmpty()) return null;
        return rowsToDataBlock(rows);
    }

    protected Pair<Integer, DataEntry> readEntryRow(Tokenizer tokenizer, int previous) throws IOException, FormatException {
        
        Token token = tokenizer.getNext();
        if (!token.is(TokenType.INTVAL)) //throw new FormatException("Row id expected instead of "+token.getTextVal(),tokenizer.getLineNr());
        {
            tokenizer.pushBack(token);
            return null;
        }    
        //we are getting next block
        if (token.getIntVal() <= previous) {
            tokenizer.pushBack(token);
            return null;            
        }
        
        int row = token.getIntVal();
        
        token = tokenizer.getNext();
        if (!token.is(TokenType.INTVAL)) {
            throw new FormatException("Value expected instead of: "+token.getTextVal(),tokenizer.getLineNr());
        }
        int value = token.getIntVal();
        
        token = tokenizer.getNext();
        if (!token.is(TokenType.TIME)) {
            throw new FormatException("Time expected instead of: "+token.getTextVal(),tokenizer.getLineNr());            
        }
        double time = token.getTimeVal();
        
        token = tokenizer.getNext();
        if (!token.is(TokenType.EOLn)) {
            throw new FormatException("EOL expected instead of: "+token.getTextVal(),tokenizer.getLineNr());            
        }        
        
        return new Pair<>(row,new DataEntry(time, value));
    }

    protected DataBlock rowsToDataBlock(List<Pair<Integer, DataEntry>> rows) {
        
        Map<Pair<Integer,Integer>,DataEntry> table = new HashMap<>();
        
        rows.forEach( row -> {
            Pair<Integer,Integer> key = numberToCoordinates(row.getLeft());
            table.put(key,row.getRight());
        });
        
        return new DataBlock(table);
    }

    protected Pair<Integer, Integer> numberToCoordinates(int nr) {
        
        nr = nr -1;
        int row = 1 + nr / 12;
        int col = 1 + (nr % 12);
        return new Pair<>(row,col);
    }


    
  
    
}
