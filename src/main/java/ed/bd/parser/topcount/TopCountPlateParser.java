/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.bd.parser.topcount;

import ed.bd.parser.topcount.err.DataFinished;
import ed.bd.parser.topcount.dom.DataBlock;
import ed.bd.parser.topcount.dom.DataEntry;
import ed.bd.parser.topcount.dom.TimeBlock;
import ed.bd.parser.topcount.dom.TimeRow;
import ed.bd.parser.topcount.dom.Token;
import ed.bd.parser.topcount.dom.ValueBlock;
import ed.bd.parser.topcount.dom.ValueRow;
import ed.robust.dom.util.Pair;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static ed.bd.parser.topcount.dom.TokenType.*;
import ed.bd.parser.topcount.dom.TopCountRow;
import ed.bd.parser.topcount.err.FileException;
import ed.bd.parser.topcount.err.FormatException;
import java.io.BufferedReader;
import java.util.HashMap;

/**
 *
 * @author tzielins
 */
class TopCountPlateParser implements TopCountParser {
    
    final TopCountUtil util = new TopCountUtil();
    final String SEP = "\t";
    
    static final String ROWIDNUMBERS="_ABCDEFGHIJKLMN";
    
    
    
    @Override
    public boolean isSuitableFormat(BufferedReader in) throws FileException {
        
        try {
            String line = in.readLine();
            if (line == null || line.isEmpty()) return false;
            line = line.trim();
            if (!line.startsWith("1") && !line.endsWith("12")) return false;
            line = in.readLine();
            if (line == null || line.isEmpty()) return false;        
            line = line.trim();
            return line.startsWith("A");
        } catch (IOException e) {
            return false;
        }
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

    DataBlock readDataBlock(Tokenizer tokenizer) throws FormatException, IOException {
        
        ValueBlock values = readValueBlock(tokenizer);
        if (values == null) return null;
        TimeBlock times = readTimeBlock(tokenizer);
        
        DataBlock dataBlock = makeDataBlock(values,times);
        return dataBlock;
    }

    DataBlock makeDataBlock(ValueBlock values, TimeBlock times) throws FormatException {
        
        if (values.getRows().size() != times.getRows().size())
            throw new FormatException("Value block and time block have different number of rows: "+values.getRows().size()+"!="+times.getRows().size());
        
        Map<Pair<Integer,Integer>,DataEntry> table = new HashMap<>();
        
        //reading times
        for (Integer rowNr : times.getRows().keySet()) {
            List<Token> columns = times.getRows().get(rowNr).getColumns();
            for (int col = 0;col<columns.size();col++) {
                Token token = columns.get(col);
                if (token.is(TIME)) {
                    DataEntry entry = new DataEntry();
                    entry.setTime(token.getTimeVal());
                    Pair<Integer,Integer> key = new Pair<>(rowNr,col+1);
                    table.put(key,entry);
                } else if (!token.is(EMPTY)) throw new FormatException("Expected time or empty cell in the time block");
            }
        }
        //reading vals
        for (Integer rowNr : values.getRows().keySet()) {
            List<Token> columns = values.getRows().get(rowNr).getColumns();
            for (int col = 0;col<columns.size();col++) {
                
                Token token = columns.get(col);
                if (token.is(INTVAL)) {
                    Pair<Integer,Integer> key = new Pair<>(rowNr,col+1);
                    DataEntry entry = table.get(key);
                    if (entry == null) throw new FormatException("Missing time for value at: "+key);                    
                    entry.setValue(token.getIntVal());
                } else if (!token.is(EMPTY)) throw new FormatException("Expected int value or empty cell in the time block");
            }
        }
        
        return new DataBlock(table);
    }

    ValueBlock readValueBlock(Tokenizer tokenizer) throws FormatException, IOException {
        
        int cols;
        try {
            cols = consumeHeader(tokenizer);
        } catch (DataFinished e) {
            return null;
        }
        
        List<ValueRow> rows = new ArrayList<>();
        for (;;) {
            ValueRow row = readValueRow(tokenizer);
            if (row == null) break;
            rows.add(row);
        }
        validateRows(rows,cols);
        return new ValueBlock(rows);
    }

    TimeBlock readTimeBlock(Tokenizer tokenizer) throws FormatException, IOException  {
        try {
        int cols = consumeHeader(tokenizer);
        
        List<TimeRow> rows = new ArrayList<>();
        for (;;) {
            TimeRow row = readTimeRow(tokenizer);
            if (row == null) break;
            rows.add(row);
        }
        validateRows(rows,cols);
        return new TimeBlock(rows);
        } catch (DataFinished e) {
            throw new FormatException("Unexpeted data finish in time block");
        }
    }

    int consumeHeader(Tokenizer tokenizer) throws DataFinished, FormatException, IOException {
        
        Token token = tokenizer.getNext();
        while (token.is(EOLn) || token.is(EMPTY)) token = tokenizer.getNext();
        if (token.is(EOF)) throw new DataFinished();
        List<Integer> columns = new ArrayList<>();
        while (token.is(INTVAL)) {
            columns.add(token.getIntVal());
            token = tokenizer.getNext();
        }
        if (!token.is(EOLn)) {
            throw new FormatException("Unexpected token: "+token.toString()+" in block header",tokenizer.getLineNr());
        }
        int prev = 0;
        for (int col : columns) {
            if (col != ++prev) 
                throw new FormatException("Wrong column number: "+col+" in the header",tokenizer.getLineNr());                
        }
        if (columns.isEmpty()) throw new FormatException("Empty header",tokenizer.getLineNr());                
        return columns.size();
    }

    ValueRow readValueRow(Tokenizer tokenizer) throws IOException, FormatException {
        
        Token token = tokenizer.getNext();
        if (!token.is(TEXT)) //throw new FormatException("Row id expected instead of "+token.getTextVal(),tokenizer.getLineNr());
        {
            tokenizer.pushBack(token);
            return null;
        }
        
        String rowId = token.getTextVal();
        int rowNr = convertRowIdToNumber(rowId,tokenizer.getLineNr());
        
        List<Token> columns = new ArrayList<>(12);
        for(;;) {
            token = tokenizer.getNext();
            if (token.is(EOLn)) break;
            if (token.is(EMPTY) || token.is(INTVAL) || token.is(DOUBLEVAL)) {
                columns.add(token);
            } else {
                throw new FormatException("Value expected instead of: "+token.getTextVal(),tokenizer.getLineNr());
            }
        }
        return new ValueRow(rowNr,rowId,columns);
    }

    void validateRows(List<? extends TopCountRow> rows, int cols) throws FormatException {
        if (rows.isEmpty())
            throw new FormatException("Row block cannot be empty");
        for (TopCountRow row : rows) {
            if (row.getColumns().size() != cols)
                throw new FormatException("Row: "+row.toString()+" has wrong length: "+row.getColumns().size()+"!="+cols);
        }
    }

    private int convertRowIdToNumber(String rowId,int lineNr) throws FormatException {
        int ix = ROWIDNUMBERS.indexOf(rowId);
        if (ix < 1) throw new FormatException("Cannot decode row id:"+rowId,lineNr);
        return ix;
    }

    TimeRow readTimeRow(Tokenizer tokenizer) throws FormatException, IOException {
        Token token = tokenizer.getNext();
        if (!token.is(TEXT)) //throw new FormatException("Row id expected instead of "+token.getTextVal(),tokenizer.getLineNr());
        {
            tokenizer.pushBack(token);
            return null;
        }
        
        String rowId = token.getTextVal();
        int rowNr = convertRowIdToNumber(rowId,tokenizer.getLineNr());
        
        List<Token> columns = new ArrayList<>(12);
        for(;;) {
            token = tokenizer.getNext();
            if (token.is(EOLn)) break;
            if (token.is(EMPTY) || token.is(TIME)) {
                columns.add(token);
            } else {
                throw new FormatException("Time expected instead of: "+token.getTextVal()+" "+token.getClass().getSimpleName(),tokenizer.getLineNr());
            }
        }
        return new TimeRow(rowNr,rowId,columns);
    }


}
