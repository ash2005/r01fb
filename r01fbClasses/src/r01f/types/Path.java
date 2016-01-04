package r01f.types;

import java.io.File;

import lombok.experimental.Accessors;
import r01f.util.types.collections.CollectionUtils;

/**
 * path abstraction, simply using a String encapsulation
 */
@Accessors(prefix="_")
public class Path
     extends PathBase<Path> {
	
	private static final long serialVersionUID = -4132364966392988245L;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public Path() {
		// no args constructor
	}
	public Path(final Object obj) {
		super(obj);
	}
	public Path(final String newPath) {
		super(newPath);
	}
	public <P extends IsPath> Path(final P otherPath) {
		super(otherPath);
	}
	public Path(final String... elements) {
		super(elements);
	}
	public Path(final boolean readOnly,final Object obj) {
		super(readOnly,obj);
	}
	public Path(final boolean readOnly,final String newPath) {
		super(readOnly,newPath);
	}
	public <P extends IsPath> Path(final boolean readOnly,final P otherPath) {
		super(readOnly,otherPath);
	}
	public Path(final boolean readOnly,final String... elements) {
		super(readOnly,elements);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	FACTORIES
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Factory from {@link String}
	 * @param path
	 * @return
	 */
	public static Path valueOf(final String path) {
		return Path.of(path);
	}
	/**
	 * @return an empty path
	 */
	public static Path create() {
		return new Path();
	}
	/**
	 * Factory from path components
	 * @param elements 
	 * @return the {@link Path} object
	 */
	public static Path of(final String... elements) {
		if (CollectionUtils.isNullOrEmpty(elements)) return null;
		return new Path(elements);
	}
	/**
	 * Factory from other {@link Path} object
	 * @param other 
	 * @return the new {@link Path} object
	 */
	public static <P extends IsPath> Path of(final P other) {
		if (other == null) return null;
		Path outPath = new Path(other);
		return outPath;
	}
	/**
	 * Factory from an {@link Object} (the path is composed translating the {@link Object} to {@link String})
	 * @param obj 
	 * @return the {@link Path} object
	 */
	public static Path of(final Object obj) {
		if (obj == null) return null;
		return new Path(obj);
	}
	/**
	 * Factory from a {@link String} object
	 * @param thePath
	 * @return the new {@link Path}
	 */
	public static Path of(final String thePath) {
		if (thePath == null) return null;
		return new Path(thePath);
	}
	/**
	 * Factory from a {@link File} object 
	 * @param file
	 * @return
	 */
	public static Path of(final File file) {
		if (file == null) return null;
		return new Path(file.getAbsolutePath());
	}
	/**
	 * Factory from path components
	 * @param elements 
	 * @return the {@link Path} object
	 */
	public static Path readOnlyOf(final String... elements) {
		if (CollectionUtils.isNullOrEmpty(elements)) return null;
		return new Path(true,elements);
	}
	/**
	 * Factory from other {@link Path} object
	 * @param other 
	 * @return the new {@link Path} object
	 */
	public static <P extends IsPath> Path readOnlyOf(final P other) {
		if (other == null) return null;
		Path outPath = new Path(true,other);
		return outPath;
	}
	/**
	 * Factory from an {@link Object} (the path is composed translating the {@link Object} to {@link String})
	 * @param obj 
	 * @return the {@link Path} object
	 */
	public static Path readOnlyOf(final Object obj) {
		if (obj == null) return null;
		return new Path(true,obj);
	}
	/**
	 * Factory from a {@link String} object
	 * @param thePath
	 * @return the new {@link Path}
	 */
	public static Path readOnlyOf(final String thePath) {
		if (thePath == null) return null;
		return new Path(true,thePath);
	}
	
/////////////////////////////////////////////////////////////////////////////////////////
//  UTIL METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public static Path join(final Path... paths) {
		Path outPath = null;
		if (CollectionUtils.hasData(paths)) {
			outPath = Path.create();
			for (Path path : paths) outPath.add(path);
		}
		return outPath;
	}
	public static Path join(final String... paths) {
		return Path.of(paths);
	}
	public static Path join(final Object... paths) {
		Path outPath = null;
		if (CollectionUtils.hasData(paths)) {
			outPath = Path.create();
			for (Object path : paths) outPath.add(path);
		}
		return outPath;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CLONE
/////////////////////////////////////////////////////////////////////////////////////////
	public Path copy() {
		return Path.of(this);
	}
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return this.copy();
	}
	
}
