package r01f.types;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.reflection.ReflectionUtils;
import r01f.types.url.UrlPath;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * path abstraction, simply using a String encapsulation
 */
@Accessors(prefix="_")
@Slf4j
public abstract class Paths { 
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	// a cache of the path types constructor
	private static Map<Class<? extends IsPath>,Constructor<? extends IsPath>> CONSTRUCTOR_REF_CACHE = Maps.newHashMap();
	
	@SuppressWarnings("unchecked")
	private static <P extends IsPath> P _createPathInstanceFromElements(final Class<P> isPathType,
																		final Collection<String> pathEls) {
		P outPathInstance = null;
		try {
			Constructor<? extends IsPath> constructor = CONSTRUCTOR_REF_CACHE.get(isPathType);
			if (constructor == null) {
				// load the constructor
				constructor = (Constructor<? extends IsPath>)ReflectionUtils.getConstructor(isPathType,
	        												 								new Class<?>[] {Collection.class},
	        												 								true);
				if (constructor == null) throw new IllegalStateException(Throwables.message("Path type {} does NOT have a Collection<String> based constructor! It's mandatory!",
																							isPathType));
				CONSTRUCTOR_REF_CACHE.put(isPathType,constructor);
			}
			outPathInstance = (P)constructor.newInstance(new Object[] {pathEls});		// BEWARE!! the path elements are encapsulated in a ImmutableList
		} catch(Throwable th) {
			log.error("Could NOT create a {} instance: {}",isPathType.getName(),th.getMessage(),th);
			// should never happen
		}
		return outPathInstance;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////	
	public static String toString(final IsPath path) {
		return Paths.asString(path);
	}
	public static String asString(final IsPath path) {
		return _asString(path.getPathElements());
	}
	public static String asRelativeString(final IsPath path) {
		return _asRelativeString(path.getPathElements());
	}
	public static String asAbsoluteString(final IsPath path) {
		return _asAbsoluteString(path.getPathElements());
	}
	public static String asAbsoluteStringFrom(final IsPath parentPath,
									  		  final IsPath path) {
		if (parentPath == null || CollectionUtils.isNullOrEmpty(parentPath.getPathElements())) return path.asAbsoluteString();
		
		Collection<String> pathElements = Lists.newArrayListWithExpectedSize(parentPath.getPathElements().size() + path.getPathElements().size());
		pathElements.addAll(parentPath.getPathElements());
		pathElements.addAll(path.getPathElements());
		return _asAbsoluteString(pathElements);
	}
	public static String asRelativeStringFrom(final IsPath parentPath,
									  		  final IsPath path) {
		if (parentPath == null || CollectionUtils.isNullOrEmpty(parentPath.getPathElements())) return path.asRelativeString();
		
		Collection<String> pathElements = Lists.newArrayListWithExpectedSize(parentPath.getPathElements().size() + path.getPathElements().size());
		pathElements.addAll(parentPath.getPathElements());
		pathElements.addAll(path.getPathElements());
		return _asRelativeString(pathElements);
	}
	public static String asRelativeStringOrNull(final IsPath path) {
		return path != null ? path.asRelativeString() 
							: null;
	}
	public static String asAbsoluteStringOrNull(final IsPath path) {
		return path != null ? path.asAbsoluteString()
							: null;
	}
	public static String asRelativeStringOrNull(final String path) {

		return Paths.asRelativeStringOrNull(Path.from(path));
	}
	public static String asAbsoluteStringOrNull(final String path) {
		return Paths.asAbsoluteStringOrNull(Path.from(path));
	}
	/**
	 * Returns {@link String} with the path as an absolute path (not starting with /)
	 * @param pathElements
	 * @return
	 */
	private static String _asRelativeString(final Collection<String> pathElements) {
		String outStr = _joinPathElements(pathElements);
		if (outStr == null) outStr = "";
		return outStr;
	}
	/**
	 * Returns {@link String} with the path as an absolute path (starting with /)
	 * @param pathElements
	 * @return
	 */
	private static String _asAbsoluteString(final Collection<String> pathElements) {
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
	private static String _asString(final Collection<String> pathElements) {
		return _asRelativeString(pathElements);
	}
	private static String _joinPathElements(final Collection<String> pathElements) {
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
	public static Collection<String> pathElementsFrom(final Object obj) {
		Preconditions.checkArgument(obj != null,"Cannot build a path from null");
		Collection<String> outCol = null;		
		if (obj.getClass().isArray()) {
			Object[] array = (Object[])obj;
			LinkedList<String> els = Lists.newLinkedList();
			for (Object objEl : array) {
				if (objEl == null) continue;
				if (objEl instanceof IsPath) {
					els.addAll(((IsPath)objEl).getPathElements());
				} else if (objEl instanceof Collection) {
					els.addAll(Paths.pathElementsFrom(objEl));	// recurse					
				} else {
					els.addAll(_normalizePathElement(objEl.toString()));	
				} 
			}
			outCol = Lists.newArrayList(els);			
		} 
		else if (obj instanceof Collection) {			
			Collection<?> col = (Collection<?>)obj;
			Object[] array = col.toArray(new Object[col.size()]);
			outCol = Paths.pathElementsFrom(array);				// recurse
		}
		else {
			outCol = _normalizePathElement(obj.toString());
		}
		return outCol;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Note that join(pathType,Object...) is strictly the only method needed 
// 	BUT many join methods are provided to AVOID excesive normalization
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Joins a variable length path elements to a given path
	 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
	 * @param elements the path elements to be joined with path
	 * @return a new {@link IsPath} object
	 */
	public static <P extends IsPath> P join(final Class<P> pathType,
											final Object... elements) {
		if (CollectionUtils.isNullOrEmpty(elements)) return null;
		
		Collection<String> pathElsToAdd = Paths.pathElementsFrom(elements);		// elements will be normalized at Paths.pathElementsFrom
		return _createPathInstanceFromElements(pathType,
											   pathElsToAdd);
	}
	/**
	 * Joins a variable length path elements to a given path
	 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
	 * @param path the parent path
	 * @param elements the path elements to be joined with path
	 * @return a new {@link IsPath} object
	 */
	public static <P extends IsPath> P join(final Class<P> pathType,
											final P path,
											final Object... elements) {
		if (CollectionUtils.isNullOrEmpty(elements)) return path;
		if (path == null) return Paths.join(pathType,
										    elements);		// elements will be normalized
		
		Collection<String> pathElsToAdd = Paths.pathElementsFrom(elements);		// elements will be normalized at Paths.pathElementsFrom
		return Paths.join(pathType,
						  path,
						  pathElsToAdd);
	}
	/**
	 * Joins a path element to a given path
	 * The path element can be anything, form another Path to an array of path element Strings or a Collection
	 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
	 * @param path the parent path
	 * @param element the path elements to be joined with path
	 * @return a new {@link IsPath} object
	 */
	public static <P extends IsPath> P join(final Class<P> pathType,
									 		final P path,
									 		final Object element) {
		if (element == null) return path;		// no changes
		if (path == null) return Paths.join(pathType,
											element);		// elements will be normalized
		
		Collection<String> pathElsToAdd = Paths.pathElementsFrom(element);		// elements will be normalized at Paths.pathElementsFrom
		return Paths.join(pathType,
						  path,
						  pathElsToAdd);
	}
	/**
	 * Joins Collection of path elements to a given path
	 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
	 * @param path the parent path
	 * @param elements the path elements to be joined with path
	 * @return a new {@link IsPath} object
	 */
	public static <P extends IsPath> P join(final Class<P> pathType,
									 		final P path,
									 		final Collection<String> elements) {
		if (CollectionUtils.isNullOrEmpty(elements)) return path;	// no changes
		if (path == null) return Paths.join(pathType,
											elements);		// elements will be normalized
		
		Collection<String> newPathEls = _joinCols12(path.getPathElements(),
											        _normalizePathElements(elements));	// elements NEEDS normalization	
		return _createPathInstanceFromElements(pathType,
											   newPathEls);
	}
	/**
	 * Joins a string that can be a path element or a full path with the given path
	 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
	 * @param path the parent path
	 * @param element the path elements to be joined with path
	 * @return a new {@link IsPath} object
	 */
	public static <P extends IsPath> P join(final Class<P> pathType,
									 		final P path,
									 		final String element) {
		if (Strings.isNullOrEmpty(element)) return path;	// no changes
		if (path == null) return Paths.join(pathType,
											element);		// element will be normalized
		
		Collection<String> newPathEls = _joinCols12(path.getPathElements(),
											        _normalizePathElement(element));		// elements NEEDS normalization
		return _createPathInstanceFromElements(pathType,
											   newPathEls);
	}
	/**
	 * Joins a path to another path
	 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
	 * @param path the parent path
	 * @param otherPath the path elements to be joined with path
	 * @return a new {@link IsPath} object
	 */
	public static <P extends IsPath> P join(final Class<P> pathType,
									 		final P path,
									 		final IsPath otherPath) {
		if (otherPath == null) return path;		// no changes
		if (path == null) return Paths.join(pathType,
											otherPath);
		
		Collection<String> newPathEls = _joinCols12(path.getPathElements(),
											        otherPath.getPathElements());			// elements DOES NOT NEED normalizations since they come from another (already normalized) path
		return _createPathInstanceFromElements(pathType,
											   newPathEls);
	}
	/**
	 * Joins a string that can be a path element or a full path with the given path
	 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
	 * @param path the parent path
	 * @param element the path elements to be joined with path
	 * @param vars the vars to be substituted at the path element
	 * @return a new {@link IsPath} object
	 */
	public static <P extends IsPath> P joinCustomized(final Class<P> pathType,
									 		   		  final P path,
									 		   		  final String element,final String... vars) {
		if (Strings.isNullOrEmpty(element)) return path;	// no changes
		if (path == null) return Paths.join(pathType,
											Strings.customized(element,(Object[])vars));	// will be normalized at Paths.join
		
		return Paths.join(pathType,
						  path,
						  Strings.customized(element,(Object[])vars));		// will be normalized at Paths.join
	}
	private static Collection<String> _joinCols12(final Collection<String> pathElements,
									 			  final Collection<String> pathElsToAdd) {
		if (CollectionUtils.isNullOrEmpty(pathElsToAdd)) return pathElements;
		if (CollectionUtils.isNullOrEmpty(pathElements)) return pathElsToAdd;
		
		return Lists.newArrayList(Iterables.concat(pathElements,
							    				   pathElsToAdd));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PREPEND
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Prepends a variable length path elements to a given path
	 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
	 * @param path the parent path
	 * @param elements the path elements to be joined with path
	 * @return a new {@link IsPath} object
	 */
	public static <P extends IsPath> P prepend(final Class<P> pathType,
											   final P path,
											   final Object... elements) {
		if (CollectionUtils.isNullOrEmpty(elements)) return path;
		if (path == null) return Paths.join(pathType,
										    elements);		// elements will be normalized
		
		Collection<String> pathElsToPrepend = Paths.pathElementsFrom(elements);		// elements will be normalized at Paths.pathElementsFrom
		return Paths.prepend(pathType,
						     path,
						     pathElsToPrepend);
	}
	/**
	 * Prepends a path element to a given path
	 * The path element can be anything, form another Path to an array of path element Strings or a Collection
	 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
	 * @param path the parent path
	 * @param element the path elements to be joined with path
	 * @return a new {@link IsPath} object
	 */
	public static <P extends IsPath> P prepend(final Class<P> pathType,
									 		   final P path,
									 		   final Object element) {
		if (element == null) return path;		// no changes
		if (path == null) return Paths.join(pathType,
											element);		// elements will be normalized
		
		Collection<String> pathElsToPrepend = Paths.pathElementsFrom(element);		// elements will be normalized at Paths.pathElementsFrom
		return Paths.prepend(pathType,
						  	 path,
						  	 pathElsToPrepend);
	}
	/**
	 * Joins Collection of path elements to a given path
	 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
	 * @param path the parent path
	 * @param elements the path elements to be joined with path
	 * @return a new {@link IsPath} object
	 */
	public static <P extends IsPath> P prepend(final Class<P> pathType,
									 		   final P path,
									 		   final Collection<String> elements) {
		if (CollectionUtils.isNullOrEmpty(elements)) return path;	// no changes
		if (path == null) return Paths.join(pathType,
											elements);		// elements will be normalized
		
		Collection<String> newPathEls = _joinCols21(path.getPathElements(),
											        _normalizePathElements(elements));	// elements NEEDS normalization	
		return _createPathInstanceFromElements(pathType,
											   newPathEls);
	}
	/**
	 * Prepends a path to another path
	 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
	 * @param path the parent path
	 * @param otherPath the path elements to be joined with path
	 * @return a new {@link IsPath} object
	 */
	public static <P extends IsPath> P prepend(final Class<P> pathType,
									 		   final P path,
									 		   final IsPath otherPath) {
		if (otherPath == null) return path;		// no changes
		if (path == null) return  _createPathInstanceFromElements(pathType,
											   					  otherPath.getPathElements());		// elements DOES NOT NEED normalizations since they come from another (already normalized) path
		
		Collection<String> newPathEls = _joinCols21(path.getPathElements(),
												    otherPath.getPathElements());		// already normalized
		return _createPathInstanceFromElements(pathType,
											   newPathEls);
	}
	/**
	 * Prepends an element path (that can be a single element or a full path as a string) to another path
	 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
	 * @param path the parent path
	 * @param element the path elements to be joined with path
	 * @return a new {@link IsPath} object
	 */
	public static <P extends IsPath> P prepend(final Class<P> pathType,
									 		   final P path,
									 		   final String element) {
		if (Strings.isNullOrEmpty(element)) return path;		// no changes
		if (path == null) return _createPathInstanceFromElements(pathType,
																 _normalizePathElement(element));	// element needs normalization	
		
		Collection<String> newPathEls = _joinCols21(path.getPathElements(),
												    _normalizePathElement(element));	// element needs normalization
		return _createPathInstanceFromElements(pathType,
											   newPathEls);
	}
	/**
	 * Prepends an element path (that can be a single element or a full path as a string) to another path
	 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
	 * @param path the parent path
	 * @param element the path elements to be joined with path
	 * @param vars the variables to be replaced a the element 
	 * @return a new {@link IsPath} object
	 */
	public static <P extends IsPath> P prependCustomized(final Class<P> pathType,
									 					 final P path,
									 					 final String element,final String... vars) {
		if (Strings.isNOTNullOrEmpty(element)) return path;		// no changes
		if (path == null) return _createPathInstanceFromElements(pathType,
																 _normalizePathElement(Strings.customized(element,(Object[])vars)));	// element NEEDS normalization
		
		return Paths.prepend(pathType,
							 path,
							 Strings.customized(element,(Object[])vars));	// will be normalized at Paths.prepend
	}
	private static Collection<String> _joinCols21(final Collection<String> pathElements,
											      final Collection<String> pathElsToPrepend) {
		Preconditions.checkArgument(pathElements != null);
		
		if (CollectionUtils.isNullOrEmpty(pathElsToPrepend)) return pathElements;
		
		return Lists.newArrayList(Iterables.concat(pathElsToPrepend,
												   pathElements));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Removes an element from the tail
	 */
	public static <P extends IsPath> P removeLastPathElement(final Class<P> pathType,
									 						 final P path) {
		if (path.getPathElements().size() == 0) return path;
		
		Collection<String> newPathEls = Lists.newArrayList(Iterables.limit(path.getPathElements(),
																		   path.getPathElements().size()-1));
	    return _createPathInstanceFromElements(pathType,
											   newPathEls);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  MORE CONVENIENT JOIN & PREPEND ACCESS
/////////////////////////////////////////////////////////////////////////////////////////
	public static Paths2<Path> forPaths() {
		return new Paths2<Path>(Path.class);
	}
	public static Paths2<UrlPath> forUrlPaths() {
		return new Paths2<UrlPath>(UrlPath.class);
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class Paths2<P extends IsPath> {
		private final Class<P> _pathType;
		
		/**
		 * Joins a variable length path elements to a given path
		 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
		 * @param elements the path elements to be joined with path
		 * @return a new {@link IsPath} object
		 */
		public P join(final Object... elements) {
			return Paths.join(_pathType,
							  elements);
		}
		/**
		 * Joins a variable length path elements to a given path
		 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
		 * @param path the parent path
		 * @param elements the path elements to be joined with path
		 * @return a new {@link IsPath} object
		 */
		public P join(final P path,
					  final Object... elements) {
			return Paths.join(_pathType,
							  path,
							  elements);
		}
		/**
		 * Joins a path element to a given path
		 * The path element can be anything, form another Path to an array of path element Strings or a Collection
		 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
		 * @param path the parent path
		 * @param element the path elements to be joined with path
		 * @return a new {@link IsPath} object
		 */
		public P join(final P path,
					  final Object element) {
			return Paths.join(_pathType,
							  path,
							  element);
		}
		/**
		 * Joins Collection of path elements to a given path
		 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
		 * @param path the parent path
		 * @param elements the path elements to be joined with path
		 * @return a new {@link IsPath} object
		 */
		public P join(final P path,
					  final Collection<String> elements) {
			return Paths.join(_pathType,
							  path,
							  elements);
		}
		/**
		 * Joins a path to another path
		 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
		 * @param path the parent path
		 * @param otherPath the path elements to be joined with path
		 * @return a new {@link IsPath} object
		 */
		public P join(final P path,
					  final IsPath otherPath) {
			return Paths.join(_pathType,
							  path,
							  otherPath);
		}
		/**
		 * Joins a string that can be a path element or a full path with the given path
		 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
		 * @param path the parent path
		 * @param element the path elements to be joined with path
		 * @param vars the vars to be substituted at the path element
		 * @return a new {@link IsPath} object
		 */
		public P joinCustomized(final P path,
						 	    final String element,final String... vars) {
			return Paths.joinCustomized(_pathType,
										path, 
										element,vars);
		}
		/**
		 * Joins a string that can be a path element or a full path with the given path
		 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
		 * @param path the parent path
		 * @param element the path elements to be joined with path
		 * @return a new {@link IsPath} object
		 */
		public P join(final P path,
					  final String element) {
			return Paths.join(_pathType,
							  path,
							  element);
		}
		/**
		 * Prepends a variable length path elements to a given path
		 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
		 * @param path the parent path
		 * @param elements the path elements to be joined with path
		 * @return a new {@link IsPath} object
		 */
		public P prepend(final P path,
					  	 final Object... elements) {
			return Paths.prepend(_pathType,
							  	 path,
							  	 elements);
		}
		/**
		 * Prepends a path element to a given path
		 * The path element can be anything, form another Path to an array of path element Strings or a Collection
		 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
		 * @param path the parent path
		 * @param element the path elements to be joined with path
		 * @return a new {@link IsPath} object
		 */
		public P prepend(final P path,
					     final Object element) {
			return Paths.prepend(_pathType,
							  	 path,
							  	 element);
		}
		/**
		 * Prepends Collection of path elements to a given path
		 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
		 * @param path the parent path
		 * @param elements the path elements to be joined with path
		 * @return a new {@link IsPath} object
		 */
		public P prepend(final P path,
					     final Collection<String> elements) {
			return Paths.prepend(_pathType,
							     path,
							     elements);
		}
		/**
		 * Prepends a path to another path
		 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
		 * @param path the parent path
		 * @param otherPath the path elements to be joined with path
		 * @return a new {@link IsPath} object
		 */
		public P prepend(final P path,
						 final IsPath otherPath) {
			return Paths.prepend(_pathType,
								 path,
								 otherPath);
		}
		/**
		 * Prepends an element path (that can be a single element or a full path as a string) to another path
		 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
		 * @param path the parent path
		 * @param element the path elements to be joined with path
		 * @return a new {@link IsPath} object
		 */
		public P prepend(final P path,
						 final String element) {
			return Paths.prepend(_pathType,
								 path,
								 element);
		}
		/**
		 * Prepends an element path (that can be a single element or a full path as a string) to another path
		 * @param pathType the path type (ie {@link UrlPath} or {@link Path})
		 * @param path the parent path
		 * @param element the path elements to be joined with path
		 * @param vars the variables to be replaced a the element 
		 * @return a new {@link IsPath} object
		 */
		public P prependCustomized(final P path,
								   final String element,final String... vars) {
			return Paths.prependCustomized(_pathType,
										   path,
										   element,vars);
		}
		/**
		 * Removes an element from the tail
		 */
		public P removeLastPathElement(final P path) {
			return Paths.removeLastPathElement(_pathType,
											   path);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	static Collection<String> normalize(final Collection<String> els) {
		if (CollectionUtils.isNullOrEmpty(els)) return null;
		Collection<String> outNormalizedEls = Lists.newLinkedList();
		for (String el : els) {
			Collection<String> normalizedEls = _normalizePathElement(el);
			outNormalizedEls.addAll(normalizedEls);
		}
		return Lists.newArrayList(outNormalizedEls);
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
			Collection<String> normalizedPathElsWithoutProtocol = _normalizePathElements(notNormalizedPathEls);
			normalizedPathEls = Lists.newArrayListWithExpectedSize(normalizedPathElsWithoutProtocol.size() + 1);
			normalizedPathEls.add("http:/");
			normalizedPathEls.addAll(normalizedPathElsWithoutProtocol);
		} else if (path.startsWith("https://")) {
			Collection<String> notNormalizedPathEls = Strings.of(path.substring("https://".length()))
															 .splitter("/")
															 .toCollection();	
			Collection<String> normalizedPathElsWithoutProtocol = _normalizePathElements(notNormalizedPathEls);
			normalizedPathEls = Lists.newArrayListWithExpectedSize(normalizedPathElsWithoutProtocol.size() + 1);
			normalizedPathEls.add("https:/");
			normalizedPathEls.addAll(normalizedPathElsWithoutProtocol);
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
}
