package r01f.model.metadata;

import r01f.guids.CommonOIDs.AppCode;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

public class MetaDataConfigUtil {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Composes a metaData id
	 * @param appCode the app code
	 * @param id the metadata id
	 * @return
	 */
	public static FieldMetaDataID idFor(final AppCode appCode,final String... ids) {
		// [1] join all the ids
		String idsJoined = CollectionUtils.of(ids)
										  .toStringSeparatedWith('.');
		// [2] add the appCode
		return FieldMetaDataID.forId(Strings.of("{}.{}")
									   .customizeWith(appCode,idsJoined)
									   .asString());
	}
}
