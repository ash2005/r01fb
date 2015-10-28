package r01f.util.types;

import java.util.Map;

import r01f.reflection.ReflectionException;
import r01f.reflection.ReflectionUtils;

public class StringCustomizeUtils {
///////////////////////////////////////////////////////////////////////////////////////////
//  SUSTITUCION DE VARIABLES
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Sustituye una cadena que contiene variables por sus valores.
     * Las variables estan delimitadas por un caracter marcador varDelim pej: $varName$
     * @param inStr La cadena en la que se hacen las sustituciones
     * @param varDelim La cadena que hace de delimitador de variables
     * @param varValues Un mapa variable-valor para realizar las sustituciones
     * @return La cadena original con las variables sustituidas
     */
    public static String replaceVariableValues(final String inStr,final String varDelim,final Map<String,String> varValues) {
        return StringCustomizeUtils.replaceVariableValues(inStr,varDelim.charAt(0),varValues,false);
    }
    /**
     * Sustituye una cadena que contiene variables por sus valores
     * Las variables estan delimitadas por caracter marcador varDelim, pej: $varName$
     * NOTA:
     *      Ojo!!   La expresion utiliza expresiones regulares así que hay que
     *              escapar la cadena a sustituir.
     * @param inStr La cadena en la que se hacen las sustituciones
     * @param varDelim El delimitador de variables
     * @param varValues El valor de las variables variable-valor
     * @return la cadena con las variables sustituidas
     */
    public static String replaceVariableValues(final String inStr,final char varDelim,final Map<String,String> varValues) {
        return StringCustomizeUtils.replaceVariableValues(inStr,varDelim,varValues,false);
    }
    /**
     * Sustituye una cadena que contiene variables por sus valores
     * Las variables estan delimitadas por caracter marcador varDelim, pej: $varName$
     * NOTA:
     *      Ojo!!   La expresion utiliza expresiones regulares así que hay que
     *              escapar la cadena a sustituir.
     * @param inStr La cadena en la que se hacen las sustituciones
     * @param varDelim El delimitador de variables
     * @param varValues El valor de las variables variable-valor
     * @param deep indica si hay que revisar si las variables a su vez contienen variables
     * @return la cadena con las variables sustituidas
     */
    public static String replaceVariableValues(final String inStr,final String varDelim,final Map<String,String> varValues,
    										   final boolean deep) {
        return StringCustomizeUtils.replaceVariableValues(inStr,varDelim.charAt(0),varValues,deep);
    }
    /**
     * Sustituye una cadena que contiene variables por sus valores
     * Las variables estan delimitadas por caracter marcador varDelim, pej: $varName$
     * NOTA:
     *      Ojo!!   La expresion utiliza expresiones regulares así que hay que
     *              escapar la cadena a sustituir.
     * @param inStr La cadena en la que se hacen las sustituciones
     * @param varDelim El delimitador de variables
     * @param varValues El valor de las variables variable-valor
     * @param deep indica si hay que revisar si las variables a su vez contienen variables
     * @return la cadena con las variables sustituidas
     */
    public static String replaceVariableValues(final String inStr,final char varDelim,final Map<String,String> varValues,
    										   final boolean deep) {
        // Primero escapar el delimitador en el caso que contenga un caracter
        // reservado de para las expresiones regulares
        String delim = "" + varDelim;
        String outString = inStr;
        if (varValues != null) {
          	for (Map.Entry<String,String> me : varValues.entrySet()) {          		
                String currStrToReplace = delim + me.getKey() + delim;
                String currVarValue = me.getValue();
                if (currVarValue == null)
                    continue;
                if (deep && currVarValue.indexOf(varDelim) >= 0) {
                    // La propia variable contiene otras variables
                    currVarValue = StringCustomizeUtils.replaceVariableValues(currVarValue, varDelim, varValues, false);
                }
                
                //ReplaceAll usa el primer parámtro string en forma de expresión regular, por lo que hay que filtrar caracteres especiales.
                outString = outString.replaceAll(_filterRegexChars(currStrToReplace), currVarValue);
            }
        }
        return outString;
    }
    
    
    /**
     * Sustituye una cadena que contiene variables por sus valores
     * Las variables estan delimitadas por una cadena que se pasa, pej: $varName$
     * NOTA: Para introducir el delimitador en el texto, doblarlo, pej: Esto son 10$$ dolares
     * En un mapa indexado por el nombre de las variables se pasan sus valores
     * @param inStr La cadena en la que se hacen las sustituciones
     * @param varDelim El delimitador de variables
     * @param obj El objeto que contiene el valor de las variables
     * @param varPaths Un mapa que machea las variables con su path en el objeto
     * @return la cadena con las variables sustituidas
     * @throws ReflectionException si no se puede obtener el valor de una variable utilizando reflection
     */
    public static String replaceVariableValuesUsingReflection(final String inStr,final String varDelim,
    														  final Object obj,final Map<String,String> varPaths) throws ReflectionException {
        StringBuilder outBuff = new StringBuilder(inStr.length());

        // Ir buscando las variables
        int p1 = 0;
        int v1 = -1;
        int v2 = -1;
        String varName = null;
        String varPath = null;
        String varValue = null;
        boolean substitution = false;

        do {
            substitution = false;
            v1 = inStr.indexOf(varDelim, p1);
            if (v1 >= 0)
                v2 = inStr.indexOf(varDelim, v1 + 1);
            if (v2 > v1) {
                outBuff.append(inStr.substring(p1, v1)); // Añadir el texto anterior a la variable
                varName = inStr.substring(v1 + 1, v2);
                if (varName.length() > 0) {
                    varPath = varPaths.get(varName);
                    if (varPath == null) {
                        outBuff.append("null");
                    } else {
                        varValue = (String)ReflectionUtils.fieldValueUsingPath(obj,varPath,false);
                        outBuff.append(varValue);
                    }
                } else if (varName.length() == 0) {
                    outBuff.append(varDelim);
                    outBuff.append(varDelim);
                }
                p1 = v2 + 1;
                v1 = -1;
                v2 = -1;
                substitution = true;
            }
        } while (substitution);
        // Añadir el resto de la cadena
        if (p1 < inStr.length())
            outBuff.append(inStr.substring(p1));
        return outBuff.toString();
    }  
    
    
    /**
	 * Parsea en busca de caracteres especiales de una Expresión Regular y los sutituye por \\char-exp
	 * @param strToBeFiltered
	 * @return
	 */
    private static String _filterRegexChars(String strToBeFiltered) {
       
    	//Caracteres a filtrar
    	char[] charsToFilter = { '.', '?', '*', '+', '^', '$','(',')' };
    	
        // Copiar la cadena de entrada en un char[]
        char content[] = new char[strToBeFiltered.length()];
        strToBeFiltered.getChars(0, strToBeFiltered.length(), content, 0);
        // Ir filtrando los caracteres y poniendo el resultado en un StringBuffer
        StringBuffer result = new StringBuffer(content.length + 50);
        for (int i = 0; i < content.length; i++) {
            boolean replaced = false;
         
			for (int j = 0; j < charsToFilter.length; j++) {
                if (content[i] == charsToFilter[j]) {
                    result.append("\\"+content[i]);
                    replaced = true;
                }
            }
            if (!replaced)
                result.append(content[i]);
        }
        return (result.toString());
    }
	
}
