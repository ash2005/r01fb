package r01f.model.facets;

import r01f.model.ModelObject;
import r01f.persistence.db.ModelObjectValidationResult;

/**
 * Facet for model objects that contains self validation logic:
 * <pre class='brush:java'>
 * 		public class MySelfValidatedType 
 * 		  implements ModelObject,
 * 					 SelfValidates<MySelfValidatedType> {
 * 			...
 *			@Override
 *			public ModelObjectValidationResult<MySelfValidatedType> validate() {
 *				if (this.getOid() == null) {
 *					return ModelObjectValidationResultBuilder.on(this)
 *															 .isNotValidBecause("The oid MUST NOT be null");
 *				}
 *				return ModelObjectValidationResultBuilder.on(this)
 *														 .isValid();
 *			}
 * 		}
 * </pre>
 * @param <T>
 */
public interface SelfValidates<T extends ModelObject> {
	
	public ModelObjectValidationResult<T> validate();
}
