package r01f.types.url;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.util.types.collections.CollectionUtils;

/**
 * A collection of urls
 */
@ConvertToDirtyStateTrackable
@XmlRootElement(name="urlCollection")
public class UrlCollection 
	 extends LinkedHashSet<UrlCollectionItem> {

	private static final long serialVersionUID = 8522332353669168499L;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public UrlCollection() {
		// default no-args constructor
	}
	// required by the marshaller
	public UrlCollection(final int length) {
		super(length);	
	}
	public UrlCollection(final UrlCollectionItem... items) {
		if (CollectionUtils.hasData(items)) this.addAll(Arrays.asList(items));
	}
	public UrlCollection(final Collection<UrlCollectionItem> items) {
		if (CollectionUtils.hasData(items)) this.addAll(items);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public UrlCollection addUrls(final UrlCollectionItem... urls) {
		if (CollectionUtils.hasData(urls)) return this.addUrls(Arrays.asList(urls));
		return this;
	}
	public UrlCollection addUrls(final Collection<UrlCollectionItem> urls) {
		if (CollectionUtils.hasData(urls)) this.addAll(urls);
		return this;
	}
	public boolean removeUrl(final Url url) {
		if (this.size() == 0) return false;
		
		Collection<UrlCollectionItem> itemsToBeRemoved = Lists.newArrayList();
		// find the items to be removed
		for (UrlCollectionItem item : this) {
			if (url.equals(item.getUrl())) itemsToBeRemoved.add(item);
		}
		// Effectively remove the items
		boolean removed = this.removeAll(itemsToBeRemoved);
		return removed;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  GET Urls by tag
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns only urls tagged by a certain tag
	 * @param tag
	 * @return
	 */
	public Collection<UrlCollectionItem> getUrlsTaggedBy(final String tag) {
		Collection<UrlCollectionItem> outItems = FluentIterable.from(this)
																   .filter(new Predicate<UrlCollectionItem>() {
																					@Override
																					public boolean apply(final UrlCollectionItem item) {
																						return item.asTaggeable()
																								    .containsTag(tag);
																					}
																   		   })
																   	.toList();
		return outItems;
	}
}
