package r01f.enums;

import java.util.regex.Matcher;
import java.util.regex.Pattern;




/**
 * Encapsula las operaciones habituales en un Enum que implementa {@link CodeAndDescriptionEnum}
 * El uso habitual es el siguiente:
 * <pre class='brush:java'>
 * @Accessors(prefix="_")
 * @RequiredArgsConstructor
 * public enum MyEnum implements EnumWithCodeAndLabel<MyEnum> {
 *		MyEnumValue1("oid1","MyEnumValue1Description"),
 *		MyEnumValue2("oid2","MyEnumValue2Description"),
 *		MyEnumValue3("oid3","MyEnumValue3Description");
 *				
 *		@Getter private String _code;		// NOTA: NO tiene por qué ser un String... puede ser un Integer u otra cosa
 *		@Getter private String _label;
 *
 *		// Wrapper estático del enum que implementa toda la funcionalidad de CodeEnum
 *		private static EnumWithCodeAndLabelWrapper<String,MyEnum> _enums = new EnumWithCodeAndLabelWrapper<String,MyEnum>(MyEnum.values());
 *		
 *		@Override
 *		public boolean isIn(MyEnum... other) {
 *			return _enums.isIn(this,other);
 *		}
 *		@Override
 *		public boolean is(MyEnum other) {
 *			return _enums.is(this,other);
 *		}
 *		public static MyEnum fromCode(int code) {
 *			return _enums.fromCode(code);
 *		}
 *		public static MyEnum fromName(String name) {
 *			return _enums.fromName(name);
 *		}
 *		public static MyEnum fromLabel(String label) {
 *			return _enums.fromDescription(label);
 *		}
 * }
 * </pre> 
 * @param <T> el Enum concreto
 */
public class EnumWithCodeAndLabelWrapper<C,T extends EnumWithCodeAndLabel<C,T>>
     extends EnumWithCodeWrapper<C,T> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructor en base a los valores del enum
	 * @param values 
	 */
	public EnumWithCodeAndLabelWrapper(final T[] values) {
		super(values);
	}
	/**
	 * Factory
	 * @param enumType
	 * @return
	 */
	public static <C,T extends EnumWithCodeAndLabel<C,T>> EnumWithCodeAndLabelWrapper<C,T> create(final Class<T> enumType) {
		return new EnumWithCodeAndLabelWrapper<C,T>(enumType.getEnumConstants());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public EnumWithCodeAndLabelWrapper<C,T> strict() {		// Es necesario sobre escribir este método para adecuar el tipo devuelto
		super.strict();
		return this;
	}
	/**
	 * Comprueba si un elemento del enum puede ser asignado a partir de una descripción
	 * @param label descripción
	 * @return true si puede ser asignado
	 */
	public boolean canBeFrom(final String label) {
		T el = null;
		for (T ty : _values) {
			if (ty.getLabel().equals(label)) {
				el = ty;
				break;
			}
		}
		return el != null ? true : false;
	}
	/**
	 * Obtiene el elemento del enum a partir de la descripcion
	 * @param label descripcion del elemento
	 * @return el elemento del enum
	 */
	public T from(final String label) {
		T outT = null;
		for (T ty : _values) {
			if (ty.getLabel().equals(label)) {
				outT = ty;
				break;
			}
		}
		if (_strict && outT == null)  throw new IllegalArgumentException("NO existe un elemento del enum con descripcion = " + label);
		return outT;
	}
	/**
	 * Obtiene el elemento del enum a partir de aplicar una expresion regular a la descripcion
	 * si el elemento el enum verifica la expresión regular que se pasa, se asume que ese elemento es el buscado
	 * @param matchingRegEx expresion regular
	 * @return el elemento del enum
	 */	
	public T elementMatching(final Pattern matchingRegEx) {
		T outT = null;
		//Pattern p = Pattern.compile(matchingRegEx);
		Matcher m = null;
		for (T ty : _values) {
			m = matchingRegEx.matcher(ty.getLabel());
			if (m.matches()) {
				outT = ty;
				break;
			}
		}
		if (_strict && outT == null) throw new IllegalArgumentException("NO existe un elemento del enum que cumpla el patron = " + matchingRegEx.toString());
		return outT;
	}	
}
