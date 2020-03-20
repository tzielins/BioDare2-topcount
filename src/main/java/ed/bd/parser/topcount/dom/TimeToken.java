/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.bd.parser.topcount.dom;

/**
 *
 * @author tzielins
 */
public class TimeToken extends Token {
    
    double time;
    public TimeToken(String val,double time) {
        super(TokenType.TIME,val);
        this.time = time;
    }

    @Override
    public double getTimeVal() {
        return time;
    }
    
    
}
