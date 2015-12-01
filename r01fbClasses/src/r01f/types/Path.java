package r01f.types;

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
	 * Constructor from path components
	 * @param elements 
	 * @return the {@link Path} object
	 */
	public static Path of(final String... elements) {
		if (CollectionUtils.isNullOrEmpty(elements)) return null;
		return new Path(elements);
	}
	/**
	 * Constructor from other {@link Path} object
	 * @param other 
	 * @return the new {@link Path} object
	 */
	public static <P extends IsPath> Path of(final P other) {
		if (other == null) return null;
		Path outPath = new Path(other);
		outPath.replaceWith(other);
		return outPath;
	}
	/**
	 * Constructor from an {@link Object} (the path is composed translating the {@link Object} to {@link String})
	 * @param obj 
	 * @return the {@link Path} object
	 */
	public static Path of(final Object obj) {
		if (obj == null) return null;
		return new Path(obj);
	}
	/**
	 * Constructor from a {@link String} object
	 * @param thePath
	 * @return the new {@link Path}
	 */
	public static Path of(final String thePath) {
		if (thePath == null) return null;
		return new Path(thePath);
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
}
