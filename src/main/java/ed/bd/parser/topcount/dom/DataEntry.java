/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.bd.parser.topcount.dom;

/**
 *
 * @author tzielins
 */
public class DataEntry {

    double time;
    double value;
    
    public DataEntry() {        
    }
    
    public DataEntry(double time,double value) {
        this.time = time;
        this.value = value;
    }
    
    public void setTime(double time) {
        this.time = time;
    }
    
    public void setValue(int value) {
        this.value = value;
    }
    
    public void setValue(double value) {
        this.value = value;
    }

    public double getTime() {
        return time;
    }

    public double getValue() {
        return value;
    }
    
    
}
