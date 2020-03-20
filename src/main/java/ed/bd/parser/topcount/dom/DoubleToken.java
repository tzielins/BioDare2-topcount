/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.bd.parser.topcount.dom;

/**
 *
 * @author tzielins
 */
public class DoubleToken extends Token {
    
    double value;
    
    public DoubleToken(double val) {
        super(TokenType.DOUBLEVAL,""+val);
        this.value = val;
    }
    
    public DoubleToken(String val) {
        this(Double.parseDouble(val));
    }

    @Override
    public double getDoubleVal() {
        return value;
    }
    
    
}
