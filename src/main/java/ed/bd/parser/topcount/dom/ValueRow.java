/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.bd.parser.topcount.dom;

import java.util.List;

/**
 *
 * @author tzielins
 */
public class ValueRow extends TopCountRow{

    
    public ValueRow(int rowNr, String rowId, List<Token> columns) {
        super(rowNr, rowId, columns);
    }
    
}
