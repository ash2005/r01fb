package r01f.bundles;

import java.util.Locale;

import r01f.util.types.Strings;
import r01f.util.types.Strings.StringExtended;
import r01f.util.types.collections.CollectionUtils;

/**
 * Exception thrown in a ResourceBundle ( {@link r01f.locale.I18NService} or {@link r01f.configproperties.ConfigProperties} ) 
 * when a searched key is not found and the {@link ResourceBundleMissingKeyBehaviour} is setted to THROWEXCEPTION
 */
public class ResourceBundleMissingKeyException 
     extends RuntimeException {
	
	private static final long serialVersionUID = 8569200760575353323L;
/////////////////////////////////////////////////////////////////////////////////////////
//		
/////////////////////////////////////////////////////////////////////////////////////////
	public ResourceBundleMissingKeyException(final String key,final Locale locale,final String... bundleChain) {
        super( Strings.of("Missing key '{}' for locale '{}' in bundle '{}'")
        			  .customizeWith(key,locale.toString(),_arrayAsString(bundleChain))
        			  .asString() );
    }
	public ResourceBundleMissingKeyException(final String key,final String... bundleChain) {
        super( Strings.of("Missing key '{}' in bundle '{}'")
        			  .customizeWith(key,_arrayAsString(bundleChain))
        			  .asString() );
    }
	private static String _arrayAsString(final String... bundleChain) {
		StringExtended sw = Strings.create(100);
		sw.add("[");
		if (CollectionUtils.hasData(bundleChain)) {
			for (int i=0; i < bundleChain.length; i++) {
				sw.add(bundleChain[i]);
				if (i < bundleChain.length-1) sw.add(", ");
			}
		}
		sw.add("]");
		return sw.asString();
	}

}
