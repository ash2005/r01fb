package r01f.util.types;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class StringXMLEncodeUtils {
///////////////////////////////////////////////////////////////////////////////
//  CODE / DECODE
///////////////////////////////////////////////////////////////////////////////
    /**
     * Comprueba si un caracter de una cadena XML es un caracter válido según la especificacion XML
     * @see http://seattlesoftware.wordpress.com/2008/09/11/hexadecimal-value-0-is-an-invalid-character/
     * Si se intenta parsear un XML con caracteres ilegales se lanza el error:
     * 		"Hexadecimal value 0x[---] is an invalid character"
     * El problema es que el XML contiene caracteres ilegales segun la especificacion XML; casi siempre estos
     * caracteres son caracteres ASCII de control (ej: like null, bell, backspace, etc)
     * 
     * @param theChar el caracter
     * @return true si el caracter es legal
     */
    public static boolean isLegalChar(final int theChar) {
    	return  (theChar == 0x9) ||/* == '\t' == 9   */
                (theChar == 0xA) ||/* == '\n' == 10  */
                (theChar == 0xD) ||/* == '\r' == 13  */
                ((theChar >= 0x20) && (theChar <= 0xD7FF)) ||
                ((theChar >= 0xE000) && (theChar <= 0xFFFD)) ||
                ((theChar >= 0x10000) && (theChar <= 0x10FFFF));
    }
    /**
     * Quita los caracteres invalidos de un xml
     * @param str la cadena a filtrar
     * @return la cadena filtrada
     */
    public static CharSequence filterInvalidChars(final CharSequence str) {
        StringBuffer outResp = new StringBuffer(str.length());

        for (int i = 0; i < str.length(); i++) {
            int code = str.charAt(i);
            if (StringXMLEncodeUtils.isLegalChar(code)) {  
                    outResp.append(str.charAt(i));
            }
        }
        return outResp.toString();
    }   
	/**
     * Codifica una cadena en UTF poniendo los caracteres de doble byte (>127) 
     * en formato escapado (&#CODE;)
     * @see http://seattlesoftware.wordpress.com/2008/09/11/hexadecimal-value-0-is-an-invalid-character/
     * @param str la cadena a codificar
     * @return la cadena codificada
     */
    public static CharSequence encodeUTFDoubleByteCharsAsEntities(final CharSequence str) {
    	if (str == null) return null;
        StringBuilder outResp = new StringBuilder(str.length());

        for (int i = 0; i < str.length(); i++) {
            int code = str.charAt(i);
            if (StringXMLEncodeUtils.isLegalChar(code)) {
                if (code >= 127) {
                    outResp.append("&#");
                    outResp.append(code);
                    outResp.append(";");
                } else {
                    outResp.append(str.charAt(i));
                }
            }
        }
        return outResp.toString();
    }
    /**
     * Codifica lo mismo que el método encodeUTF más las comillas simples y dobles
     * @see http://seattlesoftware.wordpress.com/2008/09/11/hexadecimal-value-0-is-an-invalid-character/
     * @param str la cadena a codificar
     * @return la cadena codificada
     */
    public static CharSequence encodeUTFDoubleByteCharsAndQuoutesAsEntities(final CharSequence str) {
    	if (str == null) return null;
        StringBuilder outResp = new StringBuilder(str.length());

        for (int i = 0; i < str.length(); i++) {
            int code = str.charAt(i);
            if (StringXMLEncodeUtils.isLegalChar(code)) {
                if (code > 127 || code == 34 || code == 39) {
                    outResp.append("&#");
                    outResp.append(code);
                    outResp.append(";");
                } else {
                    outResp.append(str.charAt(i));
                }
            }
        }
        return outResp.toString();
    }
    /**
     * Decodifica una cadena en UTF con caracteres expresados en forma de entity, 
     * es decir, cambia las entidades por su valor real
     * Por ejemplo, los caracteres chinos se codifican como &#valorNumerico;
     * Esta funcion pasa el valor numerico a su valor real como caracter
     * @param str cadena con caracteres expresados en forma de entidad
     * @return cadena decodificada
     */
    public static CharSequence decodeUTFDoubleByteCharsFromEntities(final CharSequence str) {
        if (str == null) return null;
        StringBuffer result = new StringBuffer(str.length());

        String regExp = "&#(([0-9]{1,7})|(x[0-9a-f]{1,6}));?";
        Pattern p = Pattern.compile(regExp, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        boolean found = m.find();
        if (found) {
            // sustituir
            do {
                //String replaceStr = m.group();
                String numericValue = m.group(1);
                char c = Character.MIN_VALUE;
                if (numericValue.startsWith("x") || numericValue.startsWith("X")) {
                    // hexadecimal
                    c = (char)Integer.parseInt(numericValue, 16);
                } else {
                    // decimal
                    c = (char)Integer.parseInt(numericValue);
                }
                m.appendReplacement(result, Character.toString(c));
                found = m.find();
            } while (found);
            m.appendTail(result);
            return result.toString();
        }
        // Devolver lo que llega sin modificar
        return str;
    }  
}
