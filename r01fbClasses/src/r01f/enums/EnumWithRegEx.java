package r01f.enums;

import java.util.regex.Pattern;

public interface EnumWithRegEx<T> 
         extends EnumExtended<T> {
	/**
	 * Devuelve la descripcion del elemento del enum
	 * @return
	 */
	public Pattern[] getPatterns(); 
	/**
	 * Combrueba si el elemento puede ser asignado a partir de un label 
	 * macheando el regEx
	 * @param desc la descripción
	 * @return true si se puede asignar, false en caso contrario
	 */
	public boolean canBeFrom(String label);
}
