package r01f.types;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.file.Files;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * path abstraction, simply using a String encapsulation
 */
@Accessors(prefix="_")
abstract class PathBase<SELF_TYPE extends PathBase<SELF_TYPE>> 
    implements IsPath,
    		   Iterable<String>,
    		   Cloneable {

	private static final long serialVersionUID = -2932591433085305985L;
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlTransient
	@Getter protected final ImmutableList<String> _pathElements;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public PathBase(final Collection<String> pathEls) {
		Preconditions.checkArgument(pathEls != null,"The path elements collection cannot be null or empty");
		_pathElements = ImmutableList.copyOf(normalize(pathEls));
	}
	public PathBase(final String newPath) {
		this(Lists.newArrayList(newPath));
	}
	public PathBase(final Object obj) {
		this(Paths.pathElementsFrom(obj));
	}
	public <P extends IsPath> PathBase(final P otherPath) {
		this(otherPath.getPathElements());
	}
	public PathBase(final String... elements) {
		this(Lists.newArrayList(elements));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	OVERRIDEN METHODS	
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public String toString() {
		return this.asString();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	IsPath METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String asString() {
		return Paths.asString(this);
	}
	@Override
	public String asRelativeString() {
		return Paths.asRelativeString(this);
	}
	@Override
	public String asAbsoluteString() {
		return Paths.asAbsoluteString(this);
	}
	@Override
	public <P extends IsPath> String asAbsoluteStringFrom(final P parentPath) {
		return Paths.asAbsoluteStringFrom(parentPath,
					   	   		  		  this);
	}
	@Override
	public <P extends IsPath> String asRelativeStringFrom(final P parentPath) {
		return Paths.asRelativeStringFrom(parentPath,
					   	   		  		  this);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the last path element
	 */
	public String getLastPathElement() {
		String outPathElement = CollectionUtils.hasData(_pathElements) ? Iterables.getLast(_pathElements,null)
																	   : null;
		return outPathElement;
	}
	/**
	 * @return the first path element
	 */
	public String getFirstPathElement() {
		String outPathElement = CollectionUtils.hasData(_pathElements) ? Iterables.getFirst(_pathElements,null)
																	   : null;
		return outPathElement;
	}
	/**
	 * Return the path element at the provided position (zeroed-based)
	 * @param pos
	 * @return
	 */
	public String getPathElementAt(final int pos) {
		String outPathElement = CollectionUtils.hasData(_pathElements) ? Iterables.get(_pathElements,pos,null)
																	   : null;
		return outPathElement;
	}
	/**
	 * Returns the N first path elements
	 * @param num the number of elements to return
	 * @return
	 */
	public List<String> getFirstNPathElements(final int num) {
		List<String> outPathElements = CollectionUtils.hasData(_pathElements) ? _pathElements.subList(0,num)
																	   		  : null;
		return outPathElements;
	}
	/**
	 * Returns the elements starting at provided position (zeroed-based) to the end of the list
	 * @param pos the start position (zeroed-based)
	 * @return
	 */
	public List<String> getPathElementsFrom(final int pos) {
		List<String> outPathElements = CollectionUtils.hasData(_pathElements) ? _pathElements.subList(pos,_pathElements.size())
																	   		  : null;
		return outPathElements;
	}
	/**
	 * @return an {@link Iterator} over the path elements
	 */
	public Iterator<String> getPathElementsIterator() {
		return CollectionUtils.hasData(_pathElements) ? _pathElements.iterator()
													  : null;
	}
	/**
	 * The number of path items
	 * @return
	 */
	public int getPathElementCount() {
		return CollectionUtils.hasData(_pathElements) ? _pathElements.size()
													  : 0;
	}
	@Override
	public Iterator<String> iterator() {
		return this.getPathElementsIterator();
	}
	/**
	 * Checks if the path contains the provided element 
	 * @param pathEl
	 * @return
	 */
	public boolean containsPathElement(final String pathEl) {
		List<String> pathEls = this.getPathElements();
		return CollectionUtils.hasData(pathEls) ? pathEls.contains(pathEl) : false;
	}
	/**
	 * Checks if the path contains the provided elements in the provided order
	 * @param pathEl
	 * @return
	 */
	public boolean containsAllPathElements(final String... pathElsToCheck) {
		Path subPath = Path.of(pathElsToCheck);
		Path fullPath = Path.of(_pathElements.toArray(new String[_pathElements.size()]));
		return fullPath.asRelativeString().contains(subPath.asRelativeString());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns true if the path is pointing to a file
	 * (this is quite simple... it only checks if the last element of the path contains a dot)
	 * @return true if the last element of the path contains a dot (.)
	 */
	public boolean isFilePath() {
		String lastPathEl = this.getLastPathElement();
		return lastPathEl != null ? lastPathEl.contains(".") 
								  : false;
	}
	/**
	 * Returns true if this is a path to a folder
	 * (this is quite simple... it only checks if the last element of the path does not contains a dot)
	 * @return true if the last element of the path does not contains a dot (.)
	 */
	public boolean isFolderPath() {
		return !this.isFilePath();
	}
	/**
	 * Returns the folder path (all the path except the file)
	 * @return
	 */
	public Path getFolderPath() {
		if (_pathElements.size() == 1 && this.isFilePath()) return null;
		if (_pathElements.size() == 1) return Path.of(_pathElements.get(0));
		return Path.of(this.getFirstNPathElements(_pathElements.size()-1)
						   .toArray(new String[_pathElements.size()-1]));
	}
	/**
	 * The file name (if this is a path to a file)
	 * If this is NOT a path to a file this returns null
	 * @return the file name with extension
	 */
	public String getFileName() {
		String lastPathEl = this.getLastPathElement();
		return lastPathEl != null && lastPathEl.contains(".") ? lastPathEl
															  : null;
	}
	/**
	 * The file extension (if this is a path to a file)
	 * If this is NOT a path to a file this returns null
	 * @return the file extension
	 */
	public String getFileExtension() {
		String fileName = this.getFileName();
		return fileName != null ? Files.getExtension(fileName)
								: null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	PRIVATE STATIC METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns {@link String} with the path as an absolute path (not starting with /)
	 * @param pathElements
	 * @return
	 */
	protected static String asRelativeString(final LinkedList<String> pathElements) {
		String outStr = _joinPathElements(pathElements);
		if (outStr == null) outStr = "";
		return outStr;
	}
	/**
	 * Returns {@link String} with the path as an absolute path (starting with /)
	 * @param pathElements
	 * @return
	 */
	protected static String asAbsoluteString(final LinkedList<String> pathElements) {
		String outStr = _joinPathElements(pathElements);
		if (outStr == null) return "";
		if (outStr.matches("([a-zA-Z]:|http://|https://).*")) return outStr;	// d: or http://
		return "/" + outStr;
	}
	/**
	 * Composes a {@link String} with the path
	 * @param pathElements
	 * @return
	 */
	protected static String asString(final LinkedList<String> pathElements) {
		return PathBase.asRelativeString(pathElements);
	}
	private static String _joinPathElements(final LinkedList<String> pathElements) {
		String outStr = null;
		if (CollectionUtils.hasData(pathElements)) {
			outStr = Joiner.on('/')
						   .skipNulls()
						   .join(pathElements);
			outStr = outStr.replaceFirst("/\\?","?");									// fix query strings as foo/bar/?queryStr
			if (outStr.endsWith("?")) outStr = outStr.substring(0,outStr.length()-1);	// fix empty query strings
		}
		return outStr;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected static Collection<String> normalize(final Collection<String> els) {
		if (CollectionUtils.isNullOrEmpty(els)) return Lists.newArrayList();
		Collection<String> outNormalizedEls = Lists.newLinkedList();
		for (String el : els) {
			Collection<String> normalizedEls = _normalizePathElement(el);
			outNormalizedEls.addAll(normalizedEls);
		}
		return outNormalizedEls;
	}
	/**
	 * @param path
	 * @return
	 */
	private static Collection<String> _normalizePathElement(final String path) {
		Collection<String> normalizedPathEls = null;
		if (!path.startsWith("http://") && !path.startsWith("https://")) {
			Collection<String> notNormalizedPathEls = Strings.of(path)
															 .splitter("/")
															 .toCollection();		
			normalizedPathEls = _normalizePathElements(notNormalizedPathEls);
		} else if (path.startsWith("http://")) {
			Collection<String> notNormalizedPathEls = Strings.of(path.substring("http://".length()))
															 .splitter("/")
															 .toCollection();		
			normalizedPathEls = Lists.newArrayList();
			normalizedPathEls.add("http:/");
			normalizedPathEls.addAll(_normalizePathElements(notNormalizedPathEls));
		} else if (path.startsWith("https://")) {
			Collection<String> notNormalizedPathEls = Strings.of(path.substring("https://".length()))
															 .splitter("/")
															 .toCollection();		
			normalizedPathEls = Lists.newArrayList();
			normalizedPathEls.add("https:/");
			normalizedPathEls.addAll(_normalizePathElements(notNormalizedPathEls));
		}
		return normalizedPathEls;
	}
	/**
	 * Normalizes a collections of path elements
	 * @param notNormalizedPathElements
	 * @return
	 */
	private static Collection<String> _normalizePathElements(final Collection<String> notNormalizedPathElements) {
		Collection<String> normalizedPathEls = null;
		if (CollectionUtils.hasData(notNormalizedPathElements)) {
			normalizedPathEls = FluentIterable.from(notNormalizedPathElements)
											  .filter(new Predicate<String>() {
															@Override
															public boolean apply(final String notNormalized) {
																return Strings.isNOTNullOrEmpty(notNormalized);
															}
											  		  })
											  .transform(new Function<String,String>() {		// normalize
																@Override
																public String apply(final String notNormalized) {
																	return _normalizePathElementString(notNormalized);
																}
											 			 })
											 			 
											  .toList();
		}
		return normalizedPathEls;
	}
	/**
	 * Removes the leading or trailing / character from the element as string
	 * @param element
	 * @return
	 */
	private static String _normalizePathElementString(final String element) {
		if (element == null) return null;

		// trim spaces
		String outNormalizedElement = element.trim();
		// remove leading / 
		if (outNormalizedElement.startsWith("/")) {
			outNormalizedElement = _removeLeadingSlashes(outNormalizedElement);		// remove the leading /
		} 
		// remove trailing /
		if (outNormalizedElement.endsWith("/")) {
			outNormalizedElement = _removeTrailingSlashes(outNormalizedElement);	// remove the trailing / 
		} 	
		// remove duplicates /
		outNormalizedElement = _removeDuplicateSparators(outNormalizedElement); 
		
		return outNormalizedElement;
	}
	private static String _removeLeadingSlashes(final String path) {
		String outPath = path;
		while (outPath.startsWith("/")) outPath = outPath.substring(1);
		return outPath;
	}
	private static String _removeTrailingSlashes(final String path) {
		String outPath = path;
		while (outPath.endsWith("/")) outPath = outPath.substring(0,outPath.length()-1);
		return outPath;
	}
	private static String _removeDuplicateSparators(final String path) {
		return path.replaceAll("/{1,}","/");		// replaces multiple / with a single / 
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  UTIL METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public static <P extends IsPath> String asRelativeStringOrNull(final P path) {
		return path != null ? path.asRelativeString() 
							: null;
	}
	public static <P extends IsPath> String asAbsoluteStringOrNull(final P path) {
		return path != null ? path.asAbsoluteString()
							: null;
	}
	public static String asRelativeStringOrNull(final String path) {

		return PathBase.asRelativeStringOrNull(Path.of(path));
	}
	public static String asAbsoluteStringOrNull(final String path) {
		return PathBase.asAbsoluteStringOrNull(Path.of(path));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_pathElements == null) ? 0 : this.asAbsoluteString().hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		PathBase<?> other = (PathBase<?>)obj;
		if (this.getClass() != obj.getClass()) return false;
		String thisPath = this.asAbsoluteString();
		String otherPath = other.asAbsoluteString();
		return thisPath.equals(otherPath);
	}
}
