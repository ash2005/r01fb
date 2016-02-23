package r01f.model.facets.delegates;

import java.util.Arrays;
import java.util.Collection;

import r01f.model.facets.TaggeableModelObject;
import r01f.model.facets.TaggeableModelObject.HasTaggeableFacet;
import r01f.types.TagList;
import r01f.util.types.collections.CollectionUtils;

/**
 * Delegate for {@link TaggeableModelObject} behavior
 * @param <M>
 */
public class TaggeableDelegate<M extends HasTaggeableFacet> 
	 extends FacetDelegateBase<M> 
  implements TaggeableModelObject {

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public TaggeableDelegate(final M hasTaggeableFacet) {
		super(hasTaggeableFacet);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  R01MTaggeableModelObject interface
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public TagList getTags() {
		return _modelObject.getTags();
	}
	@Override
	public boolean containsTag(final String tag) {
		return this.getTags() != null ? this.getTags().contains(tag)
											  : false;
	}
	@Override
	public boolean containsAllTags(final String... tags) {
		return this.containsAllTags(Arrays.asList(tags));
	}
	@Override
	public boolean containsAllTags(final Collection<String> tags) {
		return this.getTags() != null ? this.getTags().containsAll(tags)
									  : false;
	}
	@Override
	public boolean addTag(final String tag) {
		_ensureTagList(_modelObject);
		return this.getTags().add(tag);
	}
	@Override
	public boolean addTags(final Collection<String> tags) {
		if (CollectionUtils.isNullOrEmpty(tags)) return false;
		_ensureTagList(_modelObject);
		return this.getTags().addAll(tags);
	}
	@Override
	public boolean addTags(final String... tags) {
		if (CollectionUtils.isNullOrEmpty(tags)) return false;
		return this.addTags(CollectionUtils.of(tags).asSet());
	}
	@Override
	public boolean removeTag(final String tag) {		
		return this.getTags() != null ? this.getTags().remove(tag)
									  : false;
	}
	@Override
	public void clearTags() {
		if (this.getTags() != null) this.getTags().clear(); 
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static void _ensureTagList(final HasTaggeableFacet modelObject) {
		if (modelObject.getTags() == null) modelObject.setTags(new TagList());
	}

}
