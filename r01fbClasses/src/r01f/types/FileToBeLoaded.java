package r01f.types;

import java.util.Collection;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.resources.ResourcesLoaderDef.ResourcesLoaderType;

/**
 * A file {@link Path} alongside with the resources loader to use
 */
@Accessors(prefix="_")
@RequiredArgsConstructor(access=AccessLevel.PROTECTED)
public class FileToBeLoaded 
  implements IsPath {

	private static final long serialVersionUID = 1199580273135196069L;
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final ResourcesLoaderType _resourcesLoaderType;
	@Getter private final Path _filePath;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static FileToBeLoaded classPathLoaded(final String filePath) {
		return new FileToBeLoaded(ResourcesLoaderType.CLASSPATH,
							  Path.of(filePath));
	}
	public static FileToBeLoaded classPathLoaded(final Path filePath) {
		return new FileToBeLoaded(ResourcesLoaderType.CLASSPATH,
							  Path.of(filePath));
	}
	public static FileToBeLoaded fileSystemLoaded(final String filePath) {
		return new FileToBeLoaded(ResourcesLoaderType.FILESYSTEM,
							  Path.of(filePath));
	}
	public static FileToBeLoaded fileSystemLoaded(final Path filePath) {
		return new FileToBeLoaded(ResourcesLoaderType.FILESYSTEM,
							  Path.of(filePath));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public boolean isLoadedFromClassPath() {
		return _resourcesLoaderType == ResourcesLoaderType.CLASSPATH;
	}
	public boolean isLoadedFromFileSystem() {
		return _resourcesLoaderType == ResourcesLoaderType.FILESYSTEM;
	}
	public String getFilePathAsString() {
		if (_filePath == null) return null;
		String outPathAsString = null;
		if (_resourcesLoaderType == ResourcesLoaderType.CLASSPATH) {
			outPathAsString = _filePath.asRelativeString();
		} else {
			outPathAsString = _filePath.asAbsoluteString();
		}
		return outPathAsString;
	}
	@Override
	public Collection<String> getPathElements() {
		return _filePath != null ? _filePath.getPathElements() : null;
	}
	@Override
	public String asString() {
		return _filePath != null ? _filePath.asString() : null;
	}
	@Override
	public String asRelativeString() {
		return _filePath != null ? _filePath.asRelativeString() : null;
	}
	@Override
	public String asAbsoluteString() {
		return _filePath != null ? _filePath.asAbsoluteString() : null;
	}
	@Override
	public <P extends IsPath> String asAbsoluteStringFrom(final P parentPath) {
		return _filePath != null ? _filePath.asAbsoluteStringFrom(parentPath) 
								 : parentPath.asAbsoluteString();
	}
	@Override
	public <P extends IsPath> String asRelativeStringFrom(final P parentPath) {
		return _filePath != null ? _filePath.asRelativeStringFrom(parentPath) 
								 : parentPath.asRelativeString();
	}
}
