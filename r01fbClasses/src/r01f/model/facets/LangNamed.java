package r01f.model.facets;




/**
 * Interface for objects with a name in a certain language
 * (it's the same as {@link LangInDependentNamed})
 */
public interface LangNamed 
		 extends LangInDependentNamed {
/////////////////////////////////////////////////////////////////////////////////////////
//  HaLangInDependentNamedFacet
/////////////////////////////////////////////////////////////////////////////////////////
	public static interface HasLangNamedFacet 
					extends HasLangInDependentNamedFacet {
		// just extend
	}
}
