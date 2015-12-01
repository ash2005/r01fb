package r01f.resources;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import r01f.types.Path;

/**
 * Loads a file from the ClassPath
 * <h2>IMPORTANT!</h2>
 * ClassLoader and Class apply resource names differently
 * see: http://www.thinkplexx.com/learn/howto/java/system/java-resource-loading-explained-absolute-and-relative-names-difference-between-classloader-and-class-resource-loading
 * the ClassLoader methods needs RELATIVE paths, but the Class methods needs ABSOLUTE paths
 * <ul>
 * 		<li>
 * 		ClassPathResourcesLoader.class.getResource(name) and ClassPathResourcesLoader.class.getResourceAsStream(name)
 * 		needs absolute paths
 * 		</li>
 * 		<li>
 * 		ClassPathResourcesLoader.class.getClassLoader().getResource(name) and ClassPathResourcesLoader.class.getClassLoader().getResourceAsStream(name)
 * 		needs relativePaths.
 * 		</li>
 * </ul>
 * This class uses ClassPathResourcesLoader.class.getClassLoader() so it needs RELATIVE paths. Normally this class ensures that the path
 * is RELATIVE and converts an absolute path into a relative one
 */
public class ResourcesLoaderFromClassPath 
     extends ResourcesLoaderBase {
///////////////////////////////////////////////////////////////////////////////
// 	CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////
	ResourcesLoaderFromClassPath(final ResourcesLoaderDef def) {
		super(def);
	}
	@Override
	boolean _checkProperties(final Map<String,String> props) {
		return true;	// no properties are needed
	}
///////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
///////////////////////////////////////////////////////////////////////////////////////////
	@Override
    public InputStream getInputStream(final Path resourcePath,
    								  final boolean reload) throws IOException {
		// IMPORTANT: ClassLoader and Class apply resource names differently
		// see: http://www.thinkplexx.com/learn/howto/java/system/java-resource-loading-explained-absolute-and-relative-names-difference-between-classloader-and-class-resource-loading
		// The ClassLoader methods, NEEDS absolute paths while Class methods NEEDS relative paths
		//		- ClassPathResourcesLoader.class.getResource(name) y ClassPathResourcesLoader.class.getResourceAsStream(name)
		//	  	  use ABSOLUTE paths
		//	  	- BUT ClassPathResourcesLoader.class.getClassLoader().getResource(name) y ClassPathResourcesLoader.class.getClassLoader().getResourceAsStream(name)
		//		  use relative paths

		String theResourcePath = resourcePath.asRelativeString();
        InputStream outResourceIS = null;
        ClassLoader loader = ResourcesLoaderFromClassPath.class.getClassLoader();

        if (reload) {
        	// ... ensures NOT to use the loader's cache
        	URL url = loader.getResource(theResourcePath);
            if (url != null) {
            	URLConnection connection = url.openConnection();
                if (connection != null) {
                	// Disable caches to get fresh data for reloading.
                    connection.setUseCaches(false);
                    outResourceIS = connection.getInputStream();
                }
            }
        } else {
        	// ... can use the loader's cache
        	outResourceIS = loader.getResourceAsStream(theResourcePath);
        }
        return outResourceIS;
    }
}
