package r01f.persistence.db;

import lombok.RequiredArgsConstructor;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.patterns.IsBuilder;
import r01f.persistence.db.ModelObjectValidationResults.ModelObjectValidationResultNOK;
import r01f.persistence.db.ModelObjectValidationResults.ModelObjectValidationResultOK;
import r01f.util.types.Strings;

/**
 * Builder for {@link ModelObjectValidationResult} implementing types: {@link ModelObjectValidationResultNOK} and {@link ModelObjectValidationResultOK}
 * <pre class='brush:java'>
 * 		ModelObjectValidationResultNOK validNOK = ModelObjectValidationResultBuilder.on(modelObj)
 * 																					.isNotValidBecause("blah blah");
 * 		ModelObjectValidationResultOK validOK = ModelObjectValidationResultBuilder.on(modelObj)
 * 																				  .isValid();
 * </pre>
 */
public class ModelObjectValidationResultBuilder 
  implements IsBuilder {
	
	public static <M extends PersistableModelObject<? extends OID>> ModelObjectValidationResultBuilderStep<M> on(final M modelObject) {
		return new ModelObjectValidationResultBuilderStep<M>(modelObject);
	}
	@RequiredArgsConstructor
	public static class ModelObjectValidationResultBuilderStep<M extends PersistableModelObject<? extends OID>> {
		private final M _modelObj;
		
		public ModelObjectValidationResultOK<M> isValid() {
			return new ModelObjectValidationResultOK<M>(_modelObj);
		}
		public ModelObjectValidationResultNOK<M> isNotValidBecause(final String reason,final Object... args) {
			return new ModelObjectValidationResultNOK<M>(_modelObj,
													  	 Strings.customized(reason,args));
		}
	}
}
