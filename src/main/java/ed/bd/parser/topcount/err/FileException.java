/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.bd.parser.topcount.err;

/**
 *
 * @author tzielins
 */
public class FileException extends RuntimeException {
    private static final long serialVersionUID = 10L;

    public FileException(String msg, int line) {
        super(msg+" at line: "+line);
    }

    public FileException(String msg) {
        super(msg);
    }
    
    public FileException(String msg,Throwable e) {
        super(msg,e);
    }
}
