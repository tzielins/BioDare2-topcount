/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biodare.data.topcount;

import ed.biodare.data.topcount.dom.DataBlock;
import ed.biodare.data.topcount.err.FileException;
import ed.biodare.data.topcount.err.FormatException;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author tzielins
 */
public interface TopCountParser {
    
    public boolean isSuitableFormat(BufferedReader in) throws FileException;
    
    default public boolean isSuitableFormat(Path file) throws FileException {
        try (BufferedReader in = makeReader(file)) {
            return isSuitableFormat(in);
        } catch (IOException e) {
            return false; //throw new FileException(e.getMessage(),e);
        }
    }
    
    public List<DataBlock> readDataBlocks(BufferedReader reader) throws FileException, FormatException;

    default public List<DataBlock> readDataBlocks(Path file) throws FileException, FormatException {
        try (BufferedReader in = makeReader(file)) {
            return readDataBlocks(in);
        } catch (IOException e) {
            throw new FileException(e.getMessage(),e);
        }    
    }
    
    static BufferedReader makeReader(Path file) throws IOException {
        return Files.newBufferedReader(file, Charset.forName("US-ASCII"));
    }
    
}
