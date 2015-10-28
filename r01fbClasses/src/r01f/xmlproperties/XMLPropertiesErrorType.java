package r01f.xmlproperties;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.exceptions.EnrichedThrowableSubType;
import r01f.exceptions.EnrichedThrowableSubTypeWrapper;
import r01f.exceptions.ExceptionSeverity;
import r01f.exceptions.Throwables;
import r01f.internal.R01F;

/**
 * {@link XMLPropertiesException} types 
 */
@Accessors(prefix="_")
public enum XMLPropertiesErrorType 
 implements EnrichedThrowableSubType<XMLPropertiesErrorType> {
	COMPONENTDEF_NOT_FOUND(R01F.CORE_GROUP+1),
	COMPONENTDEF_XML_MALFORMED(R01F.CORE_GROUP+2),
	PROPERTIES_NOT_FOUND(R01F.CORE_GROUP+3),
	PROPERTIES_XML_MALFORMED(R01F.CORE_GROUP+4);
	
	@Getter private final int _group = R01F.CORE_GROUP;
	@Getter private final int _code;
	
	private XMLPropertiesErrorType(final int code) {
		_code = code;
	}
	
	private static EnrichedThrowableSubTypeWrapper<XMLPropertiesErrorType> WRAPPER = EnrichedThrowableSubTypeWrapper.create(XMLPropertiesErrorType.class); 
	
	public static XMLPropertiesErrorType from(final int errorCode) {
		return WRAPPER.from(R01F.CORE_GROUP,errorCode);
	}
	public static XMLPropertiesErrorType from(final int groupCode,final int errorCode) {
		if (groupCode != R01F.CORE_GROUP) throw new IllegalArgumentException(Throwables.message("The group code for a {} MUST be {}",
																								XMLPropertiesErrorType.class,R01F.CORE_GROUP));
		return WRAPPER.from(R01F.CORE_GROUP,errorCode);
	}
	@Override
	public ExceptionSeverity getSeverity() {
		return ExceptionSeverity.FATAL;		// All xml properties errors are fatal
	}
	@Override
	public boolean is(final int group,final int code) {
		return WRAPPER.is(this,
						  group,code);
	}
	public boolean is(final int code) {
		return this.is(R01F.CORE_GROUP,code);
	}
	@Override
	public boolean isIn(final XMLPropertiesErrorType... els) {
		return WRAPPER.isIn(this,els);
	}
	@Override
	public boolean is(final XMLPropertiesErrorType el) {
		return WRAPPER.is(this,el);
	}
}
