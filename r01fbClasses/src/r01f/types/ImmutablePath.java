package r01f.types;

import java.util.Collection;

import lombok.experimental.Accessors;
import r01f.util.types.collections.CollectionUtils;

/**
 * Immutable path abstraction, simply using a String encapsulation
 */
@Accessors(prefix="_")
public class ImmutablePath
     extends Path {
	
	private static final long serialVersionUID = -4132364966392988245L;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ImmutablePath() {
		// no args constructor
	}
	public ImmutablePath(final Object obj) {
		super(obj);
	}
	public ImmutablePath(final String newPath) {
		super(newPath);
	}
	public <P extends IsPath> ImmutablePath(final P otherPath) {
		super(otherPath);
	}
	public ImmutablePath(final String... elements) {
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
	public static ImmutablePath valueOf(final String path) {
		return ImmutablePath.of(path);
	}
	/**
	 * @return an empty path
	 */
	public static ImmutablePath create() {
		return new ImmutablePath();
	}
	/**
	 * Constructor from path components
	 * @param elements 
	 * @return the {@link ImmutablePath} object
	 */
	public static ImmutablePath of(final String... elements) {
		return new ImmutablePath(elements);
	}
	/**
	 * Constructor from other {@link ImmutablePath} object
	 * @param other 
	 * @return the new {@link ImmutablePath} object
	 */
	public static <P extends IsPath> ImmutablePath of(final P other) {
		ImmutablePath outPath = new ImmutablePath(other);
		outPath.replaceWith(other);
		return outPath;
	}
	/**
	 * Constructor from an {@link Object} (the path is composed translating the {@link Object} to {@link String})
	 * @param obj 
	 * @return the {@link ImmutablePath} object
	 */
	public static ImmutablePath of(final Object obj) {
		return new ImmutablePath(obj);
	}
	/**
	 * Constructor from a {@link String} object
	 * @param thePath
	 * @return the new {@link ImmutablePath}
	 */
	public static ImmutablePath of(final String thePath) {
		return new ImmutablePath(thePath);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  UTIL METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public static ImmutablePath join(final ImmutablePath... paths) {
		ImmutablePath outPath = null;
		if (CollectionUtils.hasData(paths)) {
			outPath = ImmutablePath.create();
			for (ImmutablePath path : paths) outPath.add(path);
		}
		return outPath;
	}
	public static ImmutablePath join(final String... paths) {
		return ImmutablePath.of(paths);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  OVERRIDE MUTATOR METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public <P extends IsPath> void replaceWith(final P otherPath) {
		throw new IllegalStateException("Inmutable Path");
	}
	@Override
	public ImmutablePath add(final Object element) {
		throw new IllegalStateException("Inmutable Path");
	}
	@Override
	public ImmutablePath add(final Collection<String> elements) {
		throw new IllegalStateException("Inmutable Path");
	}
	@Override
	public ImmutablePath add(final String element) {
		throw new IllegalStateException("Inmutable Path");
	}
	@Override
	public <P extends IsPath> ImmutablePath add(final P otherPath) {
		throw new IllegalStateException("Inmutable Path");
	}
	@Override
	public ImmutablePath addCustomized(final String element,String... vars) {
		throw new IllegalStateException("Inmutable Path");
	}
	@Override
	public ImmutablePath prepend(final String element) {
		throw new IllegalStateException("Inmutable Path");
	}
	@Override
	public ImmutablePath prependCustomized(final String element,String... vars) {
		throw new IllegalStateException("Inmutable Path");
	}
	@Override
	public ImmutablePath removeLastPathElement() {
		throw new IllegalStateException("Inmutable Path");
	}

}
