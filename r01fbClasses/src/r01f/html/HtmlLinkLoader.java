package r01f.html;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import r01f.exceptions.Throwables;
import r01f.html.HtmlLinkTargetResourceData.RelationBetweenTargetAndLinkContainerDocuments;
import r01f.locale.Languages;
import r01f.types.url.Url;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * Loads a link from it's html representation
 * <pre class='brush:html'>
 * 		<a href='http://www.euskadi.net/ayudas/migrupo/ikt2013/r33-2220/es' 
 * 		   id='myLink' 
 * 		   title='a link' 
 * 		   lang='en' 
 * 		   rel='search' 
 * 		   type='application/pdf' 
 * 	       style='myStyleClass1 myStyleClass2' 
 * 		   onClick='doSomething()'>
 * 				My linked text
 * 		</a>
 * </pre>
 */
public class HtmlLinkLoader {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	private static final transient String LINK_REGEX = "<a\\s+href='([^']+)'\\s*([^>]+)?>(.*)</a>";
	private static final transient Pattern LINK_PATTERN = Pattern.compile(LINK_REGEX);
	
	private static final transient String LINK_ATTRIBUTES_REGEX = "\\s*([^=]+)\\s*=\\s*'([^']+)'";
	private static final transient Pattern LINK_ATTRIBUTES_PATTERN = Pattern.compile(LINK_ATTRIBUTES_REGEX);
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Parses a link from it's html representation
	 * @param linkAsString
	 * @return
	 */
	@SuppressWarnings("null")
	public static HtmlLink parse(final String linkAsString) {
		Preconditions.checkArgument(Strings.isNOTNullOrEmpty(linkAsString),"The link cannot be null");

		HtmlLink outLinkData = null;
		Matcher m = LINK_PATTERN.matcher(linkAsString);
		if (m.find()) {
			String hrefStr = m.group(1);				// href is mandatory
			String linkAttributes = m.group(2);			// attributes
			String linkText = m.group(3);				// text is mandatory
			// other link properties
			Map<String,String> props = null;
			if (Strings.isNOTNullOrEmpty(linkAttributes)) {
				props = Maps.newHashMap();
				Matcher attrMatcher = LINK_ATTRIBUTES_PATTERN.matcher(linkAttributes);
				while(attrMatcher.find()) {
					props.put(attrMatcher.group(1),attrMatcher.group(2));
				}
			}
			// Create the link data
			HtmlLinkPresentationData presentation = null;
			if (CollectionUtils.hasData(props)) {
				presentation = new HtmlLinkPresentationData();
				if (props.containsKey("id")) 	presentation.setId(props.get("id"));
				if (props.containsKey("title"))	presentation.setTitle(props.get("title"));
				if (props.containsKey("lang"))	{
					if (presentation.getTargetResourceData() == null) presentation.setTargetResourceData(new HtmlLinkTargetResourceData());
					presentation.getTargetResourceData()
								.setLanguage(Languages.fromCountryCode(props.get("lang")));
				}
				if (props.containsKey("rel") && RelationBetweenTargetAndLinkContainerDocuments.canBe(props.get("rel"))) {
					if (presentation.getTargetResourceData() == null) presentation.setTargetResourceData(new HtmlLinkTargetResourceData());
					presentation.getTargetResourceData()
								.setRelationWithSource(RelationBetweenTargetAndLinkContainerDocuments.fromName(props.get("rel")));
				}
				if (props.containsKey("type")) {
					if (presentation.getTargetResourceData() == null) presentation.setTargetResourceData(new HtmlLinkTargetResourceData());
					presentation.getTargetResourceData()
								.setMimeType(props.get("type"));
				}
				if (props.containsKey("style")) presentation.setInlineStyle(props.get("style"));
				if (props.containsKey("class")) presentation.setStyleClasses(Strings.of(props.get("class"))
																					.splitter(" ")
																					.toCollection());
				if (props.containsKey("onClick")) {
					if (presentation.getJavaScriptEvents() == null) presentation.setJavaScriptEvents(new HashMap<String,String>());
					presentation.getJavaScriptEvents()
								.put("onClick",props.get("onClick"));
				}
			}
			outLinkData = new HtmlLink(linkText,
										   Url.from(hrefStr),
										   presentation);
		} else {
			throw new IllegalArgumentException(Throwables.message("The link {} does NOT match the pattern {}",linkAsString,LINK_REGEX));
		}
		return outLinkData;
	}
	public static void main(String[] args) {
		String testLink = "<a href='http://www.euskadi.net/ayudas/migrupo/ikt2013/r33-2220/es' " +
							 "id='myLink' " +
							 "title='a link' " +
							 "lang='en' " + 
							 "rel='search' " + 
							 "type='application/pdf' " +
							 "style='h' " + 
							 "class='myStyleClass1 myStyleClass2' " +
							 "onClick='doSomething()'>" +
							 	"My linked text" +
						   "</a>";
		System.out.println(testLink);
		HtmlLink linkData = HtmlLinkLoader.parse(testLink);
		System.out.println(HtmlLinkRenderer.render(linkData));
	}
}





