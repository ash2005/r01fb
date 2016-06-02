package r01f.guid;

import java.io.Serializable;


/**
 * A guid key (HIGH or LOW)
 */
     class HighLowKey 
implements Serializable {    
    	 
    private static final long serialVersionUID = 2379521800350045150L;

///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
///////////////////////////////////////////////////////////////////////////////////////////
    // Value range within a byte is increased: -127....-1,0,1....128
    // ... it could have been from -127 (Byte.MIN_VALUE) to 128 (Byte.MAX_VALUE), 
    //	   BUT the first oid are ugly...
    private static byte MAX_VALUE = -1;
    private static byte MIN_VALUE = 0;
    
///////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
///////////////////////////////////////////////////////////////////////////////////////////
    private byte[] _value = null;       // the key value as byte array

///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////////////////        
    /**
     * Builds a key with the provided lengthConStruye una clave con el tamaño que se pasa
     * (the real length in bytes is the given length multiplied by 8)
     */
    public HighLowKey(final int newLength) {
        _value = new byte[newLength];
        this.setToZero();
    }
    /**
     * Builds a key from its String representation
     * @param inStr 
     */
    public HighLowKey(final String inStr) {
        _value = _fromStringOfHexToByteArray(inStr);
    }

///////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
///////////////////////////////////////////////////////////////////////////////////////////    
    /** 
     * Sets the key to zero
     */ 
    public void setToZero() {
        for (int i = 0; i < _value.length; i++){
            _value[i] = MIN_VALUE;
        }
    }    
    /** 
     * Increments the key in a unit
     */ 
    public void increment() throws HighLowMaxForKeyReachedException {
        _value = _increment(_value);
    }
    @Override
    public String toString() { 
		// BEWARE:
		// When a byte array is converted to a String, every byte uses 2 chars of the String
		// since the byte is converted to it's hex representation
		// (0=00 .... 255=FF)
        StringBuffer sb = new StringBuffer();

        String hex; // hex byte representation
        String end;
        for (int i=0; i < _value.length; i++) { 
            hex = "0" + Integer.toHexString(_value[i]);     // Pad with zeros
            end = hex.substring(hex.length()-2);            // Last two chars  
            //System.out.print(_value[i] + ":" + Integer.toHexString(_value[i]) + ":" + hex + ":" + end);
            sb.append(end.toUpperCase());   // HEX byte repr (2 chars)
        }
        return sb.toString();
    }
    
///////////////////////////////////////////////////////////////////////////////////////////
//  PRIVATE METHODS
///////////////////////////////////////////////////////////////////////////////////////////
    /** 
     * Increments the byte array value in a unit but using an inverse order
     * This is done to avoid the HotSpot effect at DB indexes avoiding a lot of
     * consecutive values with the same beginning  
     * What it's done:
     *     If the byte array in it's binary format is like
     *            00000000|00000000.....00000000|00000000
     *     When increasing a unit, the usual thing to do will be:
     *            00000000|00000000.....00000000|00000001
     *     But in order to avoid the hot-sport effect at DB indexes, the first integer is increased
     *            00000000|00000001.....00000000|00000000
     *     this way two consecutive calls to increase the key will result in very different numbers
     */ 
    private byte[] _increment(byte[] array) throws HighLowMaxForKeyReachedException {
        return _incrementElement(array,0);
    }
    /** 
     * Recursive method to increase a byte array in a unit
     */ 
    private byte[] _incrementElement(byte[] array,int index) throws HighLowMaxForKeyReachedException {
        if (array[index] == MAX_VALUE) { 
            if (index == (array.length-1)) throw new HighLowMaxForKeyReachedException();
            _incrementElement(array,index + 1);
            array[index] = MIN_VALUE;  // min value
        } else {
            array[index]++; // increment
        }
        return array;
    } 
///////////////////////////////////////////////////////////////////////////////////////////
//  STATIC METHODS
///////////////////////////////////////////////////////////////////////////////////////////
    /** 
     * Return a byte array representation of a String
     * Every byte at the String is two chars length due to the hex byte representation 
     * (ie: 0=00 .... 255=FF)
     */
    private static byte[] _fromStringOfHexToByteArray(String str) { 
        int size = str.length()/2;      // The length in bytes is half the String length
        byte[] b = new byte[size];

        for (int i = 0; i < size; i++) { 
            String chunk = str.substring(i*2,i*2+2);    // Pick two chars (a byte hex representation is two chars length)
            b[i] = (byte)Integer.parseInt(chunk,16);   // put the two chars at the array as an int
        }
        return b; 
    }    
}