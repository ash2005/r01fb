package r01f.model.facets;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import r01f.reflection.ReflectionUtils;

/**
 * Utility methods for {@link Facetable} objects
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class Facetables {
	/**
	 * Casts a {@link Facetable} object to a facet
	 * (obviously the {@link Facetable} object MUST implements the facet
	 * @param f
	 * @param facetType
	 * @return
	 */
	public static <F extends ModelObjectFacet> F asFacet(final Facetable f,
												  		 final Class<F> facetType) {
		return ReflectionUtils.cast(facetType,f);
	}
	/**
	 * Checks if a {@link Facetable} object has a facet
	 * @param facetable
	 * @param facetType
	 * @return
	 */
	public static <F extends ModelObjectFacet> boolean hasFacet(final Facetable facetable,
														 		final Class<F> facetType) {
		return Facetables.hasFacet(facetable.getClass(),
								   facetType);
	}
	/**
	 * Checks if a {@link Facetable} object has a facet
	 * @param facetableType
	 * @param facetType
	 * @return
	 */
	public static <F extends ModelObjectFacet> boolean hasFacet(final Class<? extends Facetable> facetableType,
																final Class<F> facetType) {
		return ReflectionUtils.isImplementing(facetableType,
											  facetType);
	}
}
