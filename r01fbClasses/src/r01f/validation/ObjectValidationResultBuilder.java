package r01f.validation;

import lombok.RequiredArgsConstructor;
import r01f.patterns.IsBuilder;
import r01f.util.types.Strings;
import r01f.validation.ObjectValidationResults.ObjectValidationResultNOK;
import r01f.validation.ObjectValidationResults.ObjectValidationResultOK;

/**
 * Builder for {@link ObjectValidationResult} implementing types: {@link ObjectValidationResultNOK} and {@link ObjectValidationResultOK}
 * <pre class='brush:java'>
 * 		ObjectValidationResultNOK validNOK = ObjectValidationResultBuilder.on(modelObj)
 * 																					.isNotValidBecause("blah blah");
 * 		ObjectValidationResultOK validOK = ObjectValidationResultBuilder.on(modelObj)
 * 																				  .isValid();
 * </pre>
 */
public class ObjectValidationResultBuilder 
  implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static <M> ObjectValidationResultNOK<M> isNotValidBecauseNull(final String reason,final Object... args) {
		return new ObjectValidationResultNOK<M>(null,
												Strings.customized(reason,args));
	}
	public static <M> ObjectValidationResultNOK<M> isNotValidBecauseNull() {
		return ObjectValidationResultBuilder.isNotValidBecauseNull("Not valid because the object is null");
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static <M> ObjectValidationResultBuilderStep<M> on(final M Object) {
		return new ObjectValidationResultBuilder() {/* ignore */}
						.new ObjectValidationResultBuilderStep<M>(Object);
	}
	@RequiredArgsConstructor
	public class ObjectValidationResultBuilderStep<M> {
		private final M _modelObj;
		
		public ObjectValidationResultOK<M> isValid() {
			return new ObjectValidationResultOK<M>(_modelObj);
		}
		public ObjectValidationResultNOK<M> isNotValidBecause(final String reason,final Object... args) {
			return new ObjectValidationResultNOK<M>(_modelObj,
													  	 Strings.customized(reason,args));
		}
	}
}
