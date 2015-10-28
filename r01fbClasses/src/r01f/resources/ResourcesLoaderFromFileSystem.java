package r01f.resources;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

/**
 * Loads a file from the file system
 */
public class ResourcesLoaderFromFileSystem 
     extends ResourcesLoaderBase {
///////////////////////////////////////////////////////////////////////////////
// 	CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////
	ResourcesLoaderFromFileSystem(ResourcesLoaderDef def) {
		super(def);
	}
	@Override
	boolean _checkProperties(final Map<String,String> props) {
		return true;	// no properties are needed
	}
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS
///////////////////////////////////////////////////////////////////////////////////////////
	@Override
    public InputStream getInputStream(final String resourceName,
    								  final boolean reload) throws IOException {
        InputStream fileIS = new FileInputStream(resourceName);
        return fileIS;
    }
    @Override
    public Reader getReader(final String resourceName,
    						final boolean reload) throws IOException {
    	return new InputStreamReader(this.getInputStream(resourceName,reload));
    }
}
