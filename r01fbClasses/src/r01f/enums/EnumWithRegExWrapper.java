package r01f.enums;

import java.util.regex.Pattern;





/**
 * Encapsula las operaciones habituales en un Enum que implementa {@link CodeAndDescriptionEnum}
 * El uso habitual es el siguiente:
 * <pre class='brush:java'>
 * @Accessors(prefix="_")
 * public enum MyEnum implements EnumWithRexEx<MyEnum> {
 *		IMAGE(".+\\.jpg"),
 *		DOC(".+\\.doc"),
 *				
 *		@Getter private Pattern[] _patterns;
 *
 *		public MyEnum(Pattern... patterns) {
 *			_patterns = patterns;
 *		}
 *
 *		// Wrapper estático del enum que implementa toda la funcionalidad de CodeEnum
 *		private static EnumWithRegExLabelWrapper<MyEnum> _enums = new EnumWithRegExLabelWrapper<MyEnum>(MyEnum.values());
 *		
 *		@Override
 *		public boolean isIn(MyEnum... other) {
 *			return _enums.isIn(this,other);
 *		}
  *		@Override
 *		public boolean is(MyEnum other) {
 *			return _enums.is(this,other);
 *		}
 *		@Override
 *		public boolean canBeFrom(String label) {
 *			return _enums.canBeFrom(label);
 *		}
 *		public static MyEnum fromName(String name) {
 *			return _enums.fromName(name);
 *		}
 *		public static MyEnum fromCode(int code) {
 *			return _enums.fromCode(code);
 *		}
 *		public static MyEnum fromLabel(String desc) {
 *			return _enums.fromDescription(desc);
 *		}
 * }
 * </pre> 
 * @param <T> el Enum concreto
 */
public class EnumWithRegExWrapper<T extends EnumWithRegEx<T>> 
     extends EnumExtendedWrapper<T> {
	/**
	 * Constructor en base a los valores del enum
	 * @param values 
	 */
	public EnumWithRegExWrapper(final T[] values) {
		super(values);
	}
	
	@Override
	public EnumWithRegExWrapper<T> strict() {		// Es necesario sobre escribir este método para adecuar el tipo devuelto
		super.strict();
		return this;
	}
	/**
	 * Comprueba si un elemento del enum puede ser asignado a partir de un texto 
	 * macheando con la expresión regular
	 * @param label el texto 
	 * @return true si puede ser asignado
	 */
	public boolean canBeFrom(final String label) {
		T outT = _fromLabel(label);
		return outT != null ? true : false;
	}
	/**
	 * Obtiene el elemento del enum a partir de la descripcion
	 * @param desc descripcion del elemento
	 * @return el elemento del enum
	 */
	public T fromLabel(final String label) {
		T outT = _fromLabel(label);
		if (_strict && outT == null)  throw new IllegalArgumentException("NO existe un elemento del enum con descripcion = " + label);
		return outT;
	}
	
	private T _fromLabel(final String label) {
		T outT = null;
		for (T ty : _values) {
			if (ty.getPatterns() != null) {
				for (Pattern p : ty.getPatterns()) {
					if (p.matcher(label).matches()) {
						outT = ty;
						break;
					}
				}
				if (outT != null) break;
			}
		}
		return outT;
	}
}
