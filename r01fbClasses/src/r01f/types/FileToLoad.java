package r01f.types;

import java.util.LinkedList;

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
public class FileToLoad 
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
	public static FileToLoad classPathLoaded(final String filePath) {
		return new FileToLoad(ResourcesLoaderType.CLASSPATH,
							  Path.of(filePath));
	}
	public static FileToLoad classPathLoaded(final Path filePath) {
		return new FileToLoad(ResourcesLoaderType.CLASSPATH,
							  Path.of(filePath));
	}
	public static FileToLoad fileSystemLoaded(final String filePath) {
		return new FileToLoad(ResourcesLoaderType.FILESYSTEM,
							  Path.of(filePath));
	}
	public static FileToLoad fileSystemLoaded(final Path filePath) {
		return new FileToLoad(ResourcesLoaderType.FILESYSTEM,
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
	public LinkedList<String> getPathElements() {
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
	public <P extends IsPath> String asStringFrom(final P parentPath) {
		return _filePath != null ? _filePath.asStringFrom(parentPath) : parentPath.asString();
	}
}
