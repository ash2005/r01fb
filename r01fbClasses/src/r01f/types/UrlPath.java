package r01f.types;

import java.io.File;
import java.net.URL;
import java.util.Collection;

import com.google.common.collect.Lists;

import r01f.types.annotations.Inmutable;
import r01f.types.url.UrlQueryString;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * Represents a {@link Path} in a {@link URL}
 * ie: http://site:port/urlPath
 */
@Inmutable
public class UrlPath
	 extends Path {
	private static final long serialVersionUID = -4132364966392988245L;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public UrlPath() {
		super(Lists.newArrayList());
	}
	public UrlPath(final Collection<String> pathEls) {
		super(pathEls);
	}
	public UrlPath(final Object... objs) {
		super(objs);
	}
	public UrlPath(final Object obj) {
		super(obj);
	}
	public UrlPath(final String newPath) {
		super(newPath);
	}
	public <P extends IsPath> UrlPath(final P otherPath) {
		super(otherPath);
	}
	public UrlPath(final String... elements) {
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
	public static UrlPath valueOf(final String path) {
		return UrlPath.of(path);
	}
	/**
	 * Factory from path components
	 * @param elements 
	 * @return the {@link Path} object
	 */
	public static UrlPath of(final String... elements) {
		if (CollectionUtils.isNullOrEmpty(elements)) return null;
		return new UrlPath(elements);
	}
	/**
	 * Factory from other {@link Path} object
	 * @param other 
	 * @return the new {@link Path} object
	 */
	public static <P extends IsPath> UrlPath of(final P other) {
		if (other == null) return null;
		UrlPath outPath = new UrlPath(other);
		return outPath;
	}
	/**
	 * Factory from an {@link Object} (the path is composed translating the {@link Object} to {@link String})
	 * @param obj 
	 * @return the {@link Path} object
	 */
	public static UrlPath of(final Object obj) {
		if (obj == null) return null;
		return new UrlPath(obj);
	}
	/**
	 * Factory from a {@link String} object
	 * @param thePath
	 * @return the new {@link Path}
	 */
	public static UrlPath of(final String thePath) {
		if (thePath == null) return null;
		return new UrlPath(thePath);
	}
	/**
	 * Factory from a {@link File} object 
	 * @param file
	 * @return
	 */
	public static UrlPath of(final File file) {
		if (file == null) return null;
		return new UrlPath(file.getAbsolutePath());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public String asAbsoluteStringIncludingQueryStringEncoded(final UrlQueryString queryString) {
		return this.asAbsoluteStringIncludingQueryString(queryString,
														 true);
	}
	public String asAbsoluteStringIncludingQueryString(final UrlQueryString queryString) {
		return this.asAbsoluteStringIncludingQueryString(queryString,
														 false);	}
	public String asAbsoluteStringIncludingQueryString(final UrlQueryString queryString,
													   final boolean encodeParamValues) {
		return queryString != null ? Strings.customized("{}?{}",
														this.asAbsoluteString(),queryString.asString(encodeParamValues))
								   : this.asAbsoluteString();
	}
	public String asRelativeStringIncludingQueryStringEncoded(final UrlQueryString queryString) {
		return this.asRelativeStringIncludingQueryString(queryString,
														 true);
	}
	public String asRelativeStringIncludingQueryString(final UrlQueryString queryString) {
		return this.asRelativeStringIncludingQueryString(queryString,
														 false);
	}
	public String asRelativeStringIncludingQueryString(final UrlQueryString queryString,
													   final boolean encodeParamValues) {
		return queryString != null ? Strings.customized("{}?{}",
														this.asRelativeString(),queryString.asString(encodeParamValues))
								   : this.asRelativeString();
	}
}
