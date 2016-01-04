package r01f.validation;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Model object validation result
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class ObjectValidationResults {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	static class ObjectValidationResultBase<M> 
	  implements ObjectValidationResult<M> {
		@Getter private final M _validatedObject;
		@Getter private final boolean _valid;
		
		@Override
		public boolean isNOTValid() {
			return !_valid;
		}
		@Override
		public ObjectValidationResultOK<M> asOKValidationResult() {
			return (ObjectValidationResultOK<M>)this;
		}
		@Override
		public ObjectValidationResultNOK<M> asNOKValidationResult() {
			return (ObjectValidationResultNOK<M>)this;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static class ObjectValidationResultOK<M> 
				extends ObjectValidationResultBase<M> {
		ObjectValidationResultOK(final M modelObject) {
			super(modelObject,
				  true);	// it's valid
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	@Accessors(prefix="_")
	public static class ObjectValidationResultNOK<M> 
				extends ObjectValidationResultBase<M> {
		@Getter private final String _reason;
		
		ObjectValidationResultNOK(final M modelObject,
								 final String reason) {
			super(modelObject,
				  false);	// it's NOT valid
			_reason = reason;
		}
	}

}
