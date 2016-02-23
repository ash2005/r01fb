package r01f.types.url;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.html.HtmlLinkPresentationData;
import r01f.marshalling.annotations.XmlCDATA;
import r01f.model.facets.LangNamed;
import r01f.model.facets.LangNamed.HasLangNamedFacet;
import r01f.model.facets.TaggeableModelObject;
import r01f.model.facets.TaggeableModelObject.HasTaggeableFacet;
import r01f.model.facets.delegates.LangNamedDelegate;
import r01f.model.facets.delegates.TaggeableDelegate;
import r01f.types.TagList;
import r01f.util.types.collections.CollectionUtils;

@ConvertToDirtyStateTrackable
@XmlRootElement(name="urlCollectionItem")
@Accessors(prefix="_")
public class UrlCollectionItem 
  implements Serializable,
  			 HasLangNamedFacet,
  			 HasTaggeableFacet {
	
	private static final long serialVersionUID = 4310914046678104811L;
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The url title
	 */
	@XmlElement(name="name") @XmlCDATA
	@Getter @Setter private String _name;
	/**
	 * The url 
	 */
	@XmlElement
	@Getter @Setter private Url _url;
    /**
     * link presentation
     */
	@XmlElement(name="presentation")
    @Getter private HtmlLinkPresentationData _presentation;
	/**
	 * Tags
	 */
	@XmlElementWrapper(name="tags") @XmlElement(name="tag")
	@Getter @Setter private TagList _tags;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	public UrlCollectionItem() {
		// default no-args constructor
	}
	public UrlCollectionItem(final String name,final Url url,
							 final String... tags) {
		_name = name;
		_url = url;
		if (CollectionUtils.hasData(tags)) _tags = new TagList(tags);
	}
	public UrlCollectionItem(final String name,final Url url,
							 final TagList tags) {
		_name = name;
		_url = url;
		_tags = tags;
	}
	public UrlCollectionItem(final String name,final Url url,
							 final HtmlLinkPresentationData urlPresentation,
							 final String... tags) {
		this(name,
			 url,
			 tags);
		_presentation = urlPresentation;
	}
	public UrlCollectionItem(final String name,final Url url,
							 final HtmlLinkPresentationData urlPresentation,
							 final TagList tags) {
		this(name,
			 url,
			 tags);
		_presentation = urlPresentation;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Facets
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public LangNamed asLangInDependentNamed() {
		return new LangNamedDelegate<UrlCollectionItem>(this);
	}
	@Override
	public TaggeableModelObject asTaggeable() {
		return new TaggeableDelegate<UrlCollectionItem>(this);
	}
}
