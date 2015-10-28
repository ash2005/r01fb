package r01f.types;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.util.types.Numbers;

/**
 * Encapsula un mapa de bits en un int (en Java los int son 4 bytes). 
 * Por ejemplo codificando en el último byte información de seguridad:
 * <pre>
 *    ControlTotal ----------------------------------------------
 *    Administrar Seguridad -----------------------------------  |
 *    Borrado -----------------------------------------------  | |
 *    Lectura ---------------------------------------------  | | | 
 *    Escritura -----------------------------------------  | | | |
 *    Crear -------------------------------------------  | | | | |
 *                                                     | | | | | |
 *                               0 0 0 0 0 0 0 0 | 0 0 0 0 0 0 0 0 
 * Por ejemplo:
 *   X X X 00011010: Puede administrar seguridad, leer y escribir pero no puede crear
 *                   ni tiene control total
 * </pre>
 * The normal use of this base class is:
 * <pre class='brush:java'>
 * 		public class MyMask extends BitMap {
 * 			private static transient int ELEMENT_A = 0;
 * 			private static transient int ELEMENT_B = 1;
 * 
 * 			public MyMask setElementA() {
 * 				this.setBit(ELEMENT_A);
 * 				return this;
 * 			}
 * 			public MyMask unsetElementA() {
 * 				this.clearBit(ELEMENT_A);
 * 				return this;
 * 			}
 * 			public boolean isElementASetted() {
 * 				return this.
 * 			}
 * 		}
 * </pre>
 */
@Accessors(prefix="_")
@AllArgsConstructor
public abstract class BitMap 
           implements Debuggable,
           			  Serializable {
	
	private static final long serialVersionUID = -7795853546674157713L;
///////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
///////////////////////////////////////////////////////////////////////////////////////////        
    @Getter @Setter private int _bitMap;      // Variable int que contiene la información       
    
///////////////////////////////////////////////////////////////////////////////////////////
//  MODIFICACION DEL ESTADO
///////////////////////////////////////////////////////////////////////////////////////////    
     /**
     * Incorpora la información de bitMap de otro objeto que se pasa como 
     * parámetro.
     * Lo que se hace realmente es un OR lógico con los flags de información
     * @param (BasePermissionBitMap)otherFlags: La información a incorporar
     */
    public void incorporateBitMapInfo(final BitMap otherFlags) {
        _bitMap |= otherFlags.getBitMap();   // Un OR lógico..
    }    
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS SET/CLEAR DE BITS INDIVIDUALES 
///////////////////////////////////////////////////////////////////////////////////////////    
    /**
     * Obtiene el estado de un bit del bitmap
     * @return El valor del bit
     */
    protected boolean bitAt(final int bitIndex) {
        return _getBit(_bitMap,
        			   (Numbers.INTEGER_WIDTH*8-1)-bitIndex-1);
    }
    /**
     * Establece un bit del bitmap (lo pone a uno)
     * @param bitIndex el indice del bit a establecer a uno
     */
    protected void setBitAt(final int bitIndex) {
        _bitMap = _setBit(_bitMap,
        				  (Numbers.INTEGER_WIDTH*8-1)-bitIndex-1);
    }
    /**
     * Pone un bit a cero
     * @param bitIndex El indicel del bit a poner a cero
     */
    protected void clearBitAt(final int bitIndex) {
        _bitMap = _clearBit(_bitMap,
        					(Numbers.INTEGER_WIDTH*8-1)-bitIndex-1);
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  METODOS SET/CLEAR GLOBALES
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Establece todos los bits
     */
    protected void setAll() {
        for (int i=0; i <= (Numbers.INTEGER_WIDTH*8-2); i++) {
        	this.setBitAt(i);
        }    	
    }
    /**
     * Limpia todos los bits
     */
    protected void clearAll() {
        for (int i=0; i <= (Numbers.INTEGER_WIDTH*8-2); i++) {
        	this.clearBitAt(i);
        }    	
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG-INFO
/////////////////////////////////////////////////////////////////////////////////////////    
    @Override
    public String debugInfo() {
        StringBuffer sb = new StringBuffer();
        sb.append("Mapa de bits: " + _bitMap + " : " + Integer.toBinaryString(this._bitMap) + "\r\n");
        String currBitStr = "";
        for (int i=0; i <= (Numbers.INTEGER_WIDTH*8-2); i++) {
            currBitStr = "  " + i;
            sb.append(currBitStr.substring(currBitStr.length()-2) + "|");
        }
        sb.append("\r\n");
        for (int i=0; i <= (Numbers.INTEGER_WIDTH*8-2); i++) {
            currBitStr = (this.bitAt(i) ? " 1":" 0");
            sb.append(currBitStr.substring(currBitStr.length()-2) + "|");
        }
        sb.append("\r\n");
        return sb.toString();
    }
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i=0; i <= (Numbers.INTEGER_WIDTH*8-2); i++) {
            sb.append( (this.bitAt(i)?"1":"0") );
        }
        return sb.toString();        
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS DE ENMASCARADO DE BITS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Obtiene una máscara para ver el valor de un bit de un entero
     * @param bitIndex El indice del bit cuya máscara se quiere obtener
     * @return la mascara en forma de entero
     */
    private static int _getMask(final int bitIndex) {
        return (1 << bitIndex);
    }
    /**
     * Establece un bit de un entero y devuelve este nuevo entero
     * @param originalInt El entero en el que hay que establecer el bit
     * @param bitIndex El indice del bit a establecer
     * @return el entero con el bit establecido
     */
    public static int _setBit(final int originalInt,final int bitIndex) {
        return originalInt | _getMask(bitIndex);
    }
    /**
     * Borra un bit de un entero y devuelve este nuevo entero
     * @param originalInt el entero en el que hay que establecer el bit
     * @param bitIndex El indice del bit a establecer
     * @return el entero con el bit establecido
     */
    public static int _clearBit(final int originalInt,final int bitIndex) {
        return originalInt & ~_getMask(bitIndex);
    }
    /**
     * Devuelve el valor de un bit de un entero
     * @param integer el entero del cual hay que extraer un bit
     * @param bitIndex el indice del bit a extraer
     * @return el valor del bit
     */
    public static boolean _getBit(final int integer,final int bitIndex) {
        return ((integer & _getMask(bitIndex)) != 0);
    }
}
