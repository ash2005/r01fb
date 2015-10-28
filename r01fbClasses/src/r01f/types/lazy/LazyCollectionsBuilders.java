package r01f.types.lazy;

import java.util.Collection;

import r01f.patterns.IsBuilder;
import r01f.types.lazy.LazyCachedMap.LazyCachedMapFluentBuilder;
import r01f.types.lazy.LazyNotCachedMap.LazyNotCachedMapFluentBuilder;

public class LazyCollectionsBuilders
  implements IsBuilder {
	/**
	 * @return a not cached {@link LazyMap} builder
	 */
	public static <K1,V1,C1> LazyNotCachedMapFluentBuilder<K1,V1,C1> forNOTCachedMap() {
		return new LazyNotCachedMapFluentBuilder<K1,V1,C1>();
	}
	/**
	 * @return a cached {@link LazyMap} builder
	 */
	public static <K1,V1,C1> LazyCachedMapFluentBuilder<K1,V1,C1> forCachedMap() {
		return new LazyCachedMapFluentBuilder<K1,V1,C1>();
	}
	/**
	 * Creates a lazy collection builder backed at the provided {@link Collection}
	 * @param backEndCol 
	 * @return
	 */
	public static <V> LazyCollectioFluentBuilder<V> forCollection(final Collection<V> backEndCol) {
		return new LazyCollectioFluentBuilder<V>(backEndCol);
	}
}
