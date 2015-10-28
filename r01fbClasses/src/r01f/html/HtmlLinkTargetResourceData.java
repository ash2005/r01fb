package r01f.html;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.enums.EnumExtended;
import r01f.enums.EnumExtendedWrapper;
import r01f.locale.Language;
import r01f.mime.MimeType;

/**
 * Descriptor for the target resource (destination) of a link
 * Usage:
 * <pre class='brush:java'>
 * 		HtmlLinkTargetResourceData targetResourceData = HtmlLinkTargetResourceData.create()
 * 																.relatedAas(LICENSE)
 * 																.withLang(Language.ENGLISH)
 * 																.withMimeType(MimeTypeForDocument.PDF);
 * </pre>
 */
@ConvertToDirtyStateTrackable
@XmlRootElement(name="linkTargetResourceData")
@Accessors(prefix="_")
@NoArgsConstructor
public class HtmlLinkTargetResourceData {
	/**
	 * Language of the target document
	 */
	@XmlAttribute(name="lang")
	@Getter @Setter private Language _language;
	/**
	 * Relation between the document containing the link and the document target of the link
	 */
	@XmlAttribute(name="relationWithSource")
	@Getter @Setter private RelationBetweenTargetAndLinkContainerDocuments _relationWithSource;
	/**
	 * Mime-Type of the destination document
	 */
	@XmlAttribute(name="mimeType")
	@Getter @Setter private String _mimeType;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static HtmlLinkTargetResourceData create() {
		HtmlLinkTargetResourceData outData = new HtmlLinkTargetResourceData();
		return outData;
	}
	public HtmlLinkTargetResourceData withLang(final Language lang) {
		_language = lang;
		return this;
	}
	public HtmlLinkTargetResourceData relatedAs(final RelationBetweenTargetAndLinkContainerDocuments rel) {
		_relationWithSource = rel;
		return this;
	}
	public HtmlLinkTargetResourceData withMimeType(final MimeType mime) {
		_mimeType = mime.getTypeName();
		return this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Relation between the document containing the link and the document target of the link
	 */
	public static enum RelationBetweenTargetAndLinkContainerDocuments 
		    implements EnumExtended<RelationBetweenTargetAndLinkContainerDocuments> {
		ALTERNATE,
		AUTHOR,
		BOOKMARK,
		HELP,
		LICENSE,
		NEXT,
		PREV,
		SEARCH,
		TAG,
		NOFOLLOW,
		NOREFERRER,
		PREFETCH;
		
		private static final EnumExtendedWrapper<RelationBetweenTargetAndLinkContainerDocuments> WRAPPER = EnumExtendedWrapper.create(RelationBetweenTargetAndLinkContainerDocuments.class);
		
		public static RelationBetweenTargetAndLinkContainerDocuments fromName(final String name) {
			return WRAPPER.fromName(name.toUpperCase());
		}
		public static boolean canBe(final String name) {
			return WRAPPER.canBe(name.toUpperCase());
		}
		@Override
		public boolean isIn(final RelationBetweenTargetAndLinkContainerDocuments... els) {
			return WRAPPER.isIn(this,els);
		}
		@Override
		public boolean is(final RelationBetweenTargetAndLinkContainerDocuments el) {
			return WRAPPER.is(this,el);
		}
	}
}
