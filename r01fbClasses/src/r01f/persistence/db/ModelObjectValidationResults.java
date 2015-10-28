package r01f.persistence.db;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.model.ModelObject;

/**
 * Model object validation result
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class ModelObjectValidationResults {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	static class ModelObjectValidationResultBase<M extends ModelObject> 
	  implements ModelObjectValidationResult<M> {
		@Getter private final M _modelObject;
		@Getter private final boolean _valid;
		
		@Override
		public boolean isNOTValid() {
			return !_valid;
		}
		@Override
		public ModelObjectValidationResultOK<M> asOKValidationResult() {
			return (ModelObjectValidationResultOK<M>)this;
		}
		@Override
		public ModelObjectValidationResultNOK<M> asNOKValidationResult() {
			return (ModelObjectValidationResultNOK<M>)this;
		}
	}
	public static class ModelObjectValidationResultOK<M extends ModelObject> 
				extends ModelObjectValidationResultBase<M> {
		ModelObjectValidationResultOK(final M modelObject) {
			super(modelObject,
				  true);	// it's valid
		}
	}
	@Accessors(prefix="_")
	public static class ModelObjectValidationResultNOK<M extends ModelObject> 
				extends ModelObjectValidationResultBase<M> {
		@Getter private final String _reason;
		
		ModelObjectValidationResultNOK(final M modelObject,
									   final String reason) {
			super(modelObject,
				  false);	// it's NOT valid
			_reason = reason;
		}
	}

}
