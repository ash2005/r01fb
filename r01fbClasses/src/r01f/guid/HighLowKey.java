package r01f.guid;

import java.io.Serializable;


/**
 * Modela una clave (HIGH o LOW)
 */
     class HighLowKey 
implements Serializable {    
    	 
    private static final long serialVersionUID = 2379521800350045150L;

///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTES
///////////////////////////////////////////////////////////////////////////////////////////
    // Rango de valores entre los que se aumenta un byte: -127....-1,0,1....128
    // podría ser desde -127 (Byte.MIN_VALUE) a 128 (Byte.MAX_VALUE), pero los 
    // primeros oids que salen son muy feos...
    private static byte MAX_VALUE = -1;
    private static byte MIN_VALUE = 0;
    
///////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
///////////////////////////////////////////////////////////////////////////////////////////
    private byte[] _value = null;       // El valor de la clave en forma array de bytes

///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORES
///////////////////////////////////////////////////////////////////////////////////////////        
    /**
     * ConStruye una clave con el tamaño que se pasa
     * (que equivala a dicho tamaño por 8 en bytes)
     */
    public HighLowKey(final int newLength) {
        _value = new byte[newLength];
        this.setToZero();
    }
    /**
     * Construye una clave a partir de su representacion en forma de String
     * @param inStr la representacion en cadena de la clave
     */
    public HighLowKey(final String inStr) {
        _value = _fromStringOfHexToByteArray(inStr);
    }

///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS
///////////////////////////////////////////////////////////////////////////////////////////    
    /** 
     * Reinicializa la clave al valor minimo
     */ 
    public void setToZero() {
        for (int i = 0; i < _value.length; i++){
            _value[i] = MIN_VALUE;
        }
    }    
    /** 
     * Incrementa el valor de la clave en una unidad.
     */ 
    public void increment() throws HighLowMaxForKeyReachedException {
        _value = _increment(_value);
    }
    @Override
    public String toString() { 
		// NOTA:
		// Cuano un array de bytes se pasa a cadena, cada byte ocupa 2 caracteres
		// de la cadena ya que se pasa a la cadena su representacion exadecimal
		// (0=00 .... 255=FF)
        StringBuffer sb = new StringBuffer();

        String hex; // Representacion hex del byte
        String end;
        for (int i=0; i < _value.length; i++) { 
            hex = "0" + Integer.toHexString(_value[i]);     // Añade un cero a la representacion hex del byte
            end = hex.substring(hex.length()-2);            // Los dos ultimos caracteres de la cadena hex  
            //System.out.print(_value[i] + ":" + Integer.toHexString(_value[i]) + ":" + hex + ":" + end);
            sb.append(end.toUpperCase());   // La representacion HEX del byte (2 caracteres)
        }
        return sb.toString();
    }
    
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS PRIVADOS
///////////////////////////////////////////////////////////////////////////////////////////
    /** 
     * Incrementa el valor del array de bytes en uno pero en orden inverso.
     * Esto se hace para evitar el efecto HotSpot en los indices debido a que
     * hay muchos valores del indice consecutivos con el mismo principio
     * Lo que se hace es:
     *     Si el array de bytes en formato binario tiene la forma:
     *            00000000|00000000.....00000000|00000000
     *     Lo normal a la hora de aumentar en una unidad seria:
     *            00000000|00000000.....00000000|00000001
     *     Sin embargo, lo que se hace es aumentar al reves (primero el
     *     entero que esta en primer lugar)
     *            00000000|00000001.....00000000|00000000
     *     De esta forma dos llamadas consecutivas a aumentar la clave, darán
     *     como resultado series de numeros muy diferentes.
     */ 
    private byte[] _increment(byte[] array) throws HighLowMaxForKeyReachedException {
        return _incrementElement(array,0);
    }
    /** 
     * Método recursivo para aumentar un array de bytes en uno pero en orden inverso
     */ 
    private byte[] _incrementElement(byte[] array,int index) throws HighLowMaxForKeyReachedException {
        if (array[index] == MAX_VALUE) { 
            if (index == (array.length-1)) throw new HighLowMaxForKeyReachedException();
            _incrementElement(array,index + 1);
            array[index] = MIN_VALUE;  // Se pone el byte al valor minimo
        } else {
            array[index]++; // Se aumenta en uno el valor del byte
        }
        return array;
    } 
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS ESTATICOS
///////////////////////////////////////////////////////////////////////////////////////////
    /** 
     * Devuelve una representacion en formato array de bytes de una cadena
     * Cada byte se representa en la cadena como dos caracteres debido a la 
     * representacion Hexadecimal del byte 0=00 .... 255=FF
     */
    private static byte[] _fromStringOfHexToByteArray(String str) { 
        int size = str.length()/2;      // La longitud en bytes es la mitad
        byte[] b = new byte[size];

        for (int i = 0; i < size; i++) { 
            String chunk = str.substring(i*2,i*2+2);    // Coje dos caracteres (representacion hex de un byte)
            b[i] = (byte)Integer.parseInt(chunk, 16);   // Los mete al array de bytes como un byte
        }
        return b; 
    }    
}