package r01f.types;

import java.io.File;
import java.util.Collection;

import com.google.common.collect.Lists;

import lombok.experimental.Accessors;
import r01f.types.annotations.Inmutable;
import r01f.util.types.collections.CollectionUtils;

/**
 * path abstraction, simply using a String encapsulation
 */
@Accessors(prefix="_")
@Inmutable
public class Path
     extends PathBase<Path> {
	
	private static final long serialVersionUID = -4132364966392988245L;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public Path() {
		super(Lists.newArrayList());
	}
	public Path(final Collection<String> pathEls) {
		super(pathEls);
	}
	public Path(final Object... obj) {
		super(obj);
	}
	public Path(final Object obj) {
		super(obj);
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
		return Path.from(path);
	}
	/**
	 * Factory from path components
	 * @param elements 
	 * @return the {@link Path} object
	 */
	public static Path from(final String... elements) {
		if (CollectionUtils.isNullOrEmpty(elements)) return null;
		return new Path(elements);
	}
	/**
	 * Factory from other {@link Path} object
	 * @param other 
	 * @return the new {@link Path} object
	 */
	public static <P extends IsPath> Path from(final P other) {
		if (other == null) return null;
		Path outPath = new Path(other);
		return outPath;
	}
	/**
	 * Factory from an {@link Object} (the path is composed translating the {@link Object} to {@link String})
	 * @param obj 
	 * @return the {@link Path} object
	 */
	public static Path from(final Object... obj) {
		if (obj == null) return null;
		return new Path(obj);
	}
	/**
	 * Factory from a {@link File} object 
	 * @param file
	 * @return
	 */
	public static Path from(final File file) {
		if (file == null) return null;
		return new Path(file.getAbsolutePath());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CLONE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new Path(_pathElements);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  JOIN & PREPEND
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Joins this path with another ones returning a new {@link Path}
	 * object
	 * @param other
	 * @return
	 */
	public Path joinWith(final Object... other) {
		return Paths.forPaths().join(this,
									 other);
	}
	/**
	 * @param other
	 * @return
	 */
	public Path prependWith(final Object... other) {
		return Paths.forPaths().prepend(this,
										other);
	}
}
