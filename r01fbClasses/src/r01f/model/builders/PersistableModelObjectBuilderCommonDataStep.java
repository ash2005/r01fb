package r01f.model.builders;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.model.builders.facets.TrackableModelObjectBuilder;

@Accessors(prefix="_")
@RequiredArgsConstructor
public class PersistableModelObjectBuilderCommonDataStep<O extends OID,M extends PersistableModelObject<O>,
														 BUILDER_NEXT_STEP_TYPE> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The {@link PersistableModelObject} instance that's being built
	 */
	private final M _modelObject;
	private final BUILDER_NEXT_STEP_TYPE _builderNextStep;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  ENTRY POINT
/////////////////////////////////////////////////////////////////////////////////////////
	public R01MContentModelObjectBuilderNumericIdStep withOid(final O oid) {
    	_modelObject.setOid(oid);
    	return new R01MContentModelObjectBuilderNumericIdStep();
    }
/////////////////////////////////////////////////////////////////////////////////////////
//	STEPS  
/////////////////////////////////////////////////////////////////////////////////////////
	public class R01MContentModelObjectBuilderNumericIdStep {
		public R01MContentModelObjectBuilderTrackingInfoStep withNoNumericId() {
			return new R01MContentModelObjectBuilderTrackingInfoStep();
		}
		public R01MContentModelObjectBuilderTrackingInfoStep withNumericId(final long numericId) {
			_modelObject.setNumericId(numericId);
			return new R01MContentModelObjectBuilderTrackingInfoStep();
		}
	}
	public class R01MContentModelObjectBuilderTrackingInfoStep {
		public BUILDER_NEXT_STEP_TYPE noTrackingInfo() {
			return _builderNextStep;	// return builder next step
		}
		public TrackableModelObjectBuilder<BUILDER_NEXT_STEP_TYPE,M> trackingInfo() {
			return new TrackableModelObjectBuilder<BUILDER_NEXT_STEP_TYPE,M>(_builderNextStep,	// after the tracking building return the builder next step
																	  		 _modelObject);
		}
	}
}
