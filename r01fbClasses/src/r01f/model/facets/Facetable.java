package r01f.model.facets;


/**
 * Interface for facetable objects
 * @see Facetables
 */
public interface Facetable {
	/**
	 * Casts a {@link Facetable} object to a facet
	 * (obviously the {@link Facetable} object MUST implements the facet
	 * @param facetType
	 * @return
	 */
	public <F extends ModelObjectFacet> F asFacet(final Class<F> facetType);
	/**
	 * Checks if a {@link Facetable} object has a facet
	 * @param facetType
	 * @return
	 */
	public <F extends ModelObjectFacet> boolean hasFacet(final Class<F> facetType);
}
