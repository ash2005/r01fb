package r01f.enums;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Encapsula las operaciones habituales en un Enum que implementa {@link CodeAndDescriptionsEnum}
 * El uso habitual es el siguiente:
 * <pre class='brush:java'>
 * @Accessors(prefix="_")
 * @RequiredArgsConstructor
 * public enum MyEnum implements EnumWithCodeAndMultipleLabelsEnum<MyEnum> {
 *		MyEnumValue1(0.5F,"MyEnumValue1Description_11","MyEnumValue1Description_12"),
 *		MyEnumValue2(0.7F,"MyEnumValue2Description_21","MyEnumValue2Description_22"),
 *		MyEnumValue3(0.9F,"MyEnumValue3Description_31","MyEnumValue3Description_32");
 *				
 *		@Getter private Float _code;		// NOTA: NO tiene por qué ser un Float... puede ser un String u otra cosa
 *		@Getter private String[] _labels;
 *
 *		// Wrapper estático del enum que implementa toda la funcionalidad de CodeEnum
 *		private static EnumWithCodeAndMultipleLabelsWrapper<Float,MyEnum> _enums = new EnumWithCodeAndMultipleLabelsWrapper<Float,MyEnum>(MyEnum.values());
 *		
 *		@Override
 *		public String getDescription() {
 *			// Devuelve la primera de las descripciones
 *			return _descriptions != null && _descriptions.length > 0 ? _descriptions[0] 
 *																	 : null;
 *		}
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
 *		public static MyEnum fromLabel(String desc) {
 *			return _enums.fromDescription(desc);
 *		}
 * }
 * </pre> 
 * @param <T> el Enum concreto
 */
public class EnumWithCodeAndMultipleLabelsWrapper<C,T extends EnumWithCodeAndMultipleLabels<C,T>> 
     extends EnumWithCodeAndLabelWrapper<C,T> {
	/**
	 * Constructor en base a los valores del enum
	 * @param values 
	 */
	public EnumWithCodeAndMultipleLabelsWrapper(T[] values) {
		super(values);
	}
	
	@Override
	public EnumWithCodeAndMultipleLabelsWrapper<C,T> strict() {		// Es necesario sobre escribir este método para adecuar el tipo devuelto
		super.strict();
		return this;
	}
	/**
	 * Comprueba si un elemento del enum puede ser asignado a partir de una descripción
	 * @param el el elemento
	 * @param desc descripción
	 * @return true si puede ser asignado
	 */
	public boolean canBeFrom(T el,String desc) {
		boolean outCan = false;
		for (String d : el.getLabels()) {
			if (d.equals(desc)) {
				outCan = true;
				break;
			}
		}
		return outCan;
	}
	@Override
	public T from(String desc) {
		T outT = null;
		for (T ty : _values) {
			for (String d : ty.getLabels()) {
				if (d.equals(desc)) {
					outT = ty;
					break;
				}
				if (outT != null) break;
			}
		}
		if (_strict && outT == null)  throw new IllegalArgumentException("NO existe un elemento del enum con descripcion = " + desc);
		return outT;
	}
	@Override	
	public T elementMatching(Pattern matchingRegEx) {
		T outT = null;
		//Pattern p = Pattern.compile(matchingRegEx);
		Matcher m = null;
		for (T ty : _values) {
			for (String d : ty.getLabels()) {
				m = matchingRegEx.matcher(d);
				if (m.matches()) {
					outT = ty;
					break;
				}
			}
			if (outT != null) break;
		}
		if (_strict && outT == null) throw new IllegalArgumentException("NO existe un elemento del enum que cumpla el patrón = " + matchingRegEx.toString());
		return outT;
	}	
}
