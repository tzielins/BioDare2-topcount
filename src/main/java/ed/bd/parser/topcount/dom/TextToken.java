/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.bd.parser.topcount.dom;

/**
 *
 * @author tzielins
 */
public class TextToken extends Token {
    
    public TextToken(String val) {
        super(TokenType.TEXT,val);
    }
}
