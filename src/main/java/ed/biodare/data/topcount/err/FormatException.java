/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.biodare.data.topcount.err;

/**
 *
 * @author tzielins
 */
public class FormatException extends RuntimeException {
    private static final long serialVersionUID = 10L;

    public FormatException(String msg, int line) {
        super(msg+" at line: "+line);
    }

    public FormatException(String msg) {
        super(msg);
    }
    
    public FormatException(String msg,Throwable e) {
        super(msg,e);
    }
}
