/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.bd.parser.topcount;

import ed.bd.parser.topcount.dom.Token;
import ed.bd.parser.topcount.err.FormatException;
import java.io.Closeable;
import java.io.IOException;

/**
 *
 * @author tzielins
 */
public interface Tokenizer extends Closeable {

    public Token getNext() throws IOException, FormatException;
    
    public int getLineNr();

    public void pushBack(Token token);
}
