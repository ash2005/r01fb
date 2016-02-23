package r01f.model.facets;

import r01f.types.TagList;


/**
 * Interface for model objects that can be tagged 
 */
public interface TaggeableModelObject 
		 extends Taggeable<String> {
/////////////////////////////////////////////////////////////////////////////////////////
//  R01MHasAssetContainCapableFacet
/////////////////////////////////////////////////////////////////////////////////////////
	public static interface HasTaggeableFacet 
					extends ModelObjectFacet {
		public TaggeableModelObject asTaggeable();
		
		public TagList getTags();
		public void setTags(TagList tags);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the list of tags
	 */
	public TagList getTags();
}