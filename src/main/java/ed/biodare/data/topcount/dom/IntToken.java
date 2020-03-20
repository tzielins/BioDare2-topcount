/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biodare.data.topcount.dom;

/**
 *
 * @author tzielins
 */
public class IntToken extends Token {
    
    int value;
    
    public IntToken(int val) {
        super(TokenType.INTVAL,""+val);
        this.value = val;
    }
    
    public IntToken(String val) {
        this(Integer.parseInt(val));
    }

    @Override
    public int getIntVal() {
        return value;
    }
    
    
}
