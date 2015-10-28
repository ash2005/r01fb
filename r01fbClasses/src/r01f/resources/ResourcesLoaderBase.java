package r01f.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;

import lombok.experimental.Accessors;
import r01f.types.Path;

import com.google.common.base.Preconditions;

@Accessors(prefix="_")
abstract class ResourcesLoaderBase 
    implements ResourcesLoader {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	private ResourcesLoaderDef _def;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ResourcesLoaderBase() {
		// nothing
	}
	public ResourcesLoaderBase(final ResourcesLoaderDef def) {
		Preconditions.checkArgument(def != null,"The definition MUST NOT be null");
		if (!_checkProperties(def.getLoaderProps())) throw new IllegalArgumentException("The loader definition has invalid properties!");
		_def = def;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ABSTRACT METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	abstract boolean _checkProperties(final Map<String,String> props);
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public ResourcesLoaderDef getConfig() {
		return _def;
	}
	@Override
	public InputStream getInputStream(final String resourcePath) throws IOException {
		return this.getInputStream(resourcePath,false);
	}
	@Override
	public Reader getReader(final String resourcePath) throws IOException {
		return this.getReader(resourcePath,false);
	}
	@Override
	public InputStream getInputStream(final Path resourcePath) throws IOException {
		return this.getInputStream(resourcePath.asString());
	}
	@Override
	public InputStream getInputStream(final Path resourcePath, boolean reload) throws IOException {
		return this.getInputStream(resourcePath.asString(),reload);
	}
	@Override
	public Reader getReader(final Path resourcePath) throws IOException {
		return this.getReader(resourcePath.asString());
	}
	@Override
	public Reader getReader(final Path resourcePath, boolean reload) throws IOException {
		return this.getReader(resourcePath.asString(),reload);
	}
}
 