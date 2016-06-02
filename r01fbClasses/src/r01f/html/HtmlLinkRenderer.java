package r01f.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import r01f.guid.GUIDDispenser;
import r01f.guid.GUIDDispenserDef;
import r01f.guid.GUIDDispenserDefBuilder;
import r01f.guid.SimpleGUIDDispenser;
import r01f.guids.CommonOIDs.AppCode;
import r01f.locale.Languages;
import r01f.types.url.Url;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.collect.Sets;


/**
 * Renders html link presentation data
 * <pre class='brush:java'>
 * 		String html = HtmlLinkRenderer.render("This is my linked text",
 * 											  url,
 * 											  presentation);
 * </pre>
 * If you want to include the link XML as an XML data island use:
 * <pre class='brush:java'>
 * 		String html = HtmlLinkRenderer.render("This is my linked text",
 * 											  url,
 * 											  presentation,
 * 											  xml);
 * </pre>
 */
public class HtmlLinkRenderer {
	/**
	 * Renders a link
	 * @param data
	 * @return
	 */
	public static String render(final HtmlLink data) {
		return HtmlLinkRenderer.render(data.getLinkText(),
									   data.getUrl(),
									   data.getPresentation());
	}
	/**
	 * Renders a link
	 * @param linkText
	 * @param url
	 * @param presentation
	 * @return
	 */
	public static String render(final String linkText,
								final Url url,
								final HtmlLinkPresentationData presentation) {
		String outLink = Strings.of("<a href='{}' {}>{}</a>")
								.customizeWith(url.asStringNotUrlEncodingQueryStringParamsValues(),
											   _renderPresentationData(presentation),
											   linkText)
								.asString();
		return outLink;				
	}
	/**
	 * Renders a link alongside with an XML representation of the link in a data island 
	 * (see https://developer.mozilla.org/en/docs/Using_XML_Data_Islands_in_Mozilla)
	 * @param linkText
	 * @param url
	 * @param presentation
	 * @param xmlData
	 * @return
	 */
	public static String renderWithData(final String linkText,
									    final Url url,
										final HtmlLinkPresentationData presentation,
										final String xmlData) {
		// An id for the link is mandatory so if none is provided one is generated
		String id = presentation != null ? presentation.getId() : null;
		if (Strings.isNullOrEmpty(id)) id = _generateGuid();

		String outLink = Strings.of("<a href='{}' {}>{}</a>\n" + 
									"<script id='{}_data' type='application/xml'>\n{}\n</script>\n")
								.customizeWith(url.asStringNotUrlEncodingQueryStringParamsValues(),
											   _renderPresentationData(presentation),
											   linkText,
											   id,xmlData)		// data island
								.asString();
		return outLink;				
	}
	private static GUIDDispenserDef DISPENSER_DEF = GUIDDispenserDefBuilder.builderFor(AppCode.forId("r01f"),"html")
																			.withUniqueID("rf")
																			.withLength(36)
																			.usingDefaultGUIDDispenser()
																			.build();
	public static String _generateGuid() {
		GUIDDispenser dispenser = SimpleGUIDDispenser.create(DISPENSER_DEF);
		return dispenser.generateGUID();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static String _renderPresentationData(final HtmlLinkPresentationData data) {
		if (data == null) return "";
		
		// >>> [1] - Presentation data
		Collection<String> presentationDataParams = _presentationDataParams(data);
		
		// >>> [2] - Target resource data
		Collection<String> targetDataParams = _targetResourceDataParams(data.getTargetResourceData());
		
		// >>> [3] - window opening features
		Collection<String> windowOpeningParam = _windowOpeningModeParams(data.getNewWindowOpeningMode());

		Collection<String> allParams = Sets.newHashSet();
		if (CollectionUtils.hasData(presentationDataParams)) allParams.addAll(presentationDataParams);
		if (CollectionUtils.hasData(targetDataParams)) allParams.addAll(targetDataParams);
		if (CollectionUtils.hasData(windowOpeningParam)) allParams.addAll(windowOpeningParam);
		
		// ---- Return 
		return _partsToString(allParams);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static Collection<String> _presentationDataParams(final HtmlLinkPresentationData data) {
		if (data == null) return null;
		
		Collection<String> params = new ArrayList<String>();

		// Id
		if (Strings.isNOTNullOrEmpty(data.getId())) {
			params.add(Strings.of("id='{}'")
							 .customizeWith(data.getId().trim())
							 .asString());
		}
		// Title
		if (Strings.isNOTNullOrEmpty(data.getTitle())) {
			params.add(Strings.of("title='{}'")
							  .customizeWith(data.getTitle().trim())
							  .asString());
		}
		// style classes
		if (CollectionUtils.hasData(data.getStyleClasses())) {
			StringBuilder sb = new StringBuilder(data.getStyleClasses().size() * 10);
			for (Iterator<String> styleIt = data.getStyleClasses().iterator(); styleIt.hasNext(); ) {
				String style = styleIt.next();
				sb.append(style);
				if (styleIt.hasNext()) sb.append(" ");
			}
			params.add(Strings.of("class='{}'")
							  .customizeWith(sb)
							  .asString());
		}
		// inline style
		if (Strings.isNOTNullOrEmpty(data.getInlineStyle())) {
			params.add(Strings.of("style='{}'")
							  .customizeWith(data.getInlineStyle().trim())
							  .asString());
		}
		// JavaScript events
		if (CollectionUtils.hasData(data.getJavaScriptEvents())) {
			StringBuilder sb = new StringBuilder(data.getJavaScriptEvents().size() * 30);
			for (Iterator<Map.Entry<String,String>> eventIt = data.getJavaScriptEvents().entrySet().iterator(); eventIt.hasNext(); ) {
				Map.Entry<String,String> event = eventIt.next();
				sb.append(Strings.of("{}='{}'")
								 .customizeWith(event.getKey(),event.getValue())
								 .asString());
				if (eventIt.hasNext()) sb.append(" ");
			}
			params.add(sb.toString());
		}		
		return params;
	}
	private static Collection<String> _targetResourceDataParams(final HtmlLinkTargetResourceData resData) {
		if (resData == null) return null;
		
		Collection<String> parts = new ArrayList<String>();
		
		if (resData.getLanguage() != null) {
			parts.add(Strings.of("lang='{}'")
							 .customizeWith(Languages.language(resData.getLanguage()))	
							 .asString());
		}
		if (resData.getRelationWithSource() != null) {
			parts.add(Strings.of("rel='{}'")
							 .customizeWith(resData.getRelationWithSource().name().toLowerCase())
							 .asString());
		}
		if (resData.getMimeType() != null) {
			parts.add(Strings.of("type='{}'")
							 .customizeWith(resData.getMimeType())
							 .asString());
		}
		
		// ---- Return 
		return parts;
	}
	private static Collection<String> _windowOpeningModeParams(final HtmlLinkWindowOpeningMode openMode) {
		Collection<String> parts = new ArrayList<String>();
		
		// TODO terminar el renderizado del modo de apertura del enlace
		if (openMode != null) {
			/* todo terminar */
		}
		
		// ---- Return 
		return parts;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static String _partsToString(final Collection<String> parts) {
		System.out.println(">>>" + parts);
		// ---- Return 
		StringBuilder outSb = null;
		if (CollectionUtils.hasData(parts)) {
			outSb = new StringBuilder(parts.size()*100);
			for (Iterator<String> partIt = parts.iterator(); partIt.hasNext(); ) {
				outSb.append(partIt.next());
				if (partIt.hasNext()) outSb.append(" ");
			}
		} 
		return outSb != null ? outSb.toString() : null;
	}
}
