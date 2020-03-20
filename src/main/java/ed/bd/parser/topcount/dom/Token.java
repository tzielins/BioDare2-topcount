/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ed.bd.parser.topcount.dom;

import java.util.Objects;

/**
 *
 * @author tzielins
 */
public class Token {
    
    public final TokenType type;
    String textVal;
    
    Token(TokenType type,String textVal) {
        if (type == null) throw new IllegalArgumentException("TokenType cannot be null");
        this.type=type;
        this.textVal=textVal;
    }

    public boolean is(TokenType tokenType) {
        return type.equals(tokenType);
    }

    public int getIntVal() {
        throw new UnsupportedOperationException("Int val is not supported for this token: "+type);
    }
    
    public double getDoubleVal() {
        throw new UnsupportedOperationException("Double val is not supported for this token: "+type);        
    }
    
    public double getTimeVal() {
        throw new UnsupportedOperationException("Time val is not supported for this token: "+type);        
    }
    

    public String getTextVal() {
        return textVal;
    }
    
    
    public static Token newEOF() {
       return new Token(TokenType.EOF,"EOF") ;
    }
    
    public static Token newEOLN() {
       return new Token(TokenType.EOLn,"\\n") ;
    }
    
    public static Token newEmpty() {
       return new Token(TokenType.EMPTY,"") ;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.type);
        hash = 71 * hash + Objects.hashCode(this.textVal);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Token other = (Token) obj;
        if (this.type != other.type) {
            return false;
        }
        if (!Objects.equals(this.textVal, other.textVal)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return type+":"+textVal;
    }
    
    
    
}
