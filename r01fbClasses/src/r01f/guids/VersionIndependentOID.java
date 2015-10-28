package r01f.guids;

import r01f.model.ModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;

/**
 * Version independent {@link ModelObject} {@link OID}
 * {@link HasVersionableFacet} {@link ModelObject}s have a {@link CompositeOID} composed by a {@link VersionIndependentOID} and a {@link VersionIndependentOID}
 * and a {@link VersionOID}
 */
public interface VersionIndependentOID
		 extends OID {
	// marker interface
}
