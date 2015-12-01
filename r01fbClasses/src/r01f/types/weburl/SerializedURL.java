package r01f.types.weburl;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.collect.Sets;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.exceptions.Throwables;
import r01f.marshalling.annotations.XmlCDATA;
import r01f.types.CanBeRepresentedAsString;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;


/**
 * Encapsulates an url as a {@link String} 
 * It's used to store the URLs as a {@link String} at an XML
 */
@GwtIncompatible("SerializedURL NOT usable in GWT")
@XmlRootElement(name="urlAsString")
@Accessors(prefix="_")
@NoArgsConstructor @AllArgsConstructor
public class SerializedURL 
  implements CanBeRepresentedAsString,
  			 Serializable {

	private static final long serialVersionUID = 5383405611707444269L;
/////////////////////////////////////////////////////////////////////////////////////////
// 	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	@XmlValue @XmlCDATA
	@Getter @Setter private StringBuilder _url;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates an empty {@link SerializedURL}
	 * @return
	 */
	public static SerializedURL create() {
		return new SerializedURL();
	}
	/**
	 * Creates a {@link SerializedURL}
	 * @param webUrlAsString
	 * @return
	 */
	public static SerializedURL create(final String webUrlAsString) {
		return SerializedURL.of(webUrlAsString);
	}
	/**
	 * Creates a {@link SerializedURL} from a url in it's {@link String} serialized format
	 * @param url
	 * @return
	 */
	public static SerializedURL of(final String url) {
		if (url == null) return null;
		SerializedURL outUrl = new SerializedURL();
		outUrl.setUrl(new StringBuilder(url));
		return outUrl;
	}
	/**
	 * Creates a {@link SerializedURL} from a url in it's {@link String} serialized format
	 * replacing var values present at the url
	 * @param url
	 * @param vars
	 * @return
	 */
	public static SerializedURL of(final String url,final Object... vars) {
		return SerializedURL.of(Strings.customized(url,vars));
	}
	/**
	 * Creates a {@link SerializedURL} from a url in it's {@link String} serialized format
	 * @param url
	 * @return
	 */
	public static SerializedURL valueOf(final String url) {
		return SerializedURL.of(url);
	}
	/**
	 * Creates a {@link SerializedURL} from an absolute / relative url path
	 * @param path
	 * @return
	 */
	public static SerializedURL of(final Path path) {		
		return SerializedURL.of(path.asAbsoluteString());
	}
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return SerializedURL.of(_url.toString());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds a path to the url
	 * @param path
	 */
	public void addUrlPath(final Path path) {
		if (path == null) return;
		if (_url == null) {
			_url = new StringBuilder(path.asAbsoluteString());
		} else {
			_url = _url.append(path.asAbsoluteString());
		}
	}
	/**
	 * Adds a param to the url
	 * @param paramName
	 * @param paramValue
	 * @return
	 */
	public SerializedURL addQueryStringParam(final String paramName,
											  final Serializable paramValue) {
		int qIndex = _url.indexOf("?");
		if (qIndex >= 0) {
			_url.append("&");
		} else if (qIndex < 0) {
			_url.append("?");
		}
	    _url.append(paramName)
	    	.append("=")
	    	.append(paramValue);
		return this;
	}
	/**
	 * Sets the url's query string
	 * @param queryString
	 */
	public void setUrlQueryString(final Set<WebUrlQueryStringParam> queryString) {
		if (CollectionUtils.isNullOrEmpty(queryString)) return; 
		
		StringBuilder sb = new StringBuilder();
		sb.append("?");
		for (Iterator<WebUrlQueryStringParam> paramIt = queryString.iterator(); paramIt.hasNext(); ) {
			WebUrlQueryStringParam param = paramIt.next();
			sb.append(param.asString());
			if (paramIt.hasNext()) sb.append(";");
		}
		if (_url == null) {
			_url = sb;
		} else {
			_url = _url.append(sb);
		}
	}
	/**
	 * Returns all query string params
	 * @return
	 */
	public Set<WebUrlQueryStringParam> getQueryStringParams() {
		if (_url == null) throw new IllegalStateException("The url is null!!");
		
		Set<WebUrlQueryStringParam> outParams = null;
		
		// get the url's query string part
		String[] urlSplitted = _url.toString().split("\\?");
		if (urlSplitted.length == 1) {
			// no query string
		} else if (urlSplitted.length == 2) {
			String queryString = urlSplitted[1];
			if (Strings.isNOTNullOrEmpty(queryString)) {
				String[] queryStringParams = queryString.split("&");
				outParams = Sets.newLinkedHashSetWithExpectedSize(queryStringParams.length);
				for (String queryStringParam : queryStringParams) {
					outParams.add(WebUrlQueryStringParamImpl.from(queryStringParam));					
				}
			} else {
				outParams = Sets.newLinkedHashSet();
			}
		} else {
			throw new IllegalStateException(Throwables.message("The url={} is NOT valid: it has TWO ?",_url));
		}
		return outParams;
	}
	/**
	 * Checks if the url's query string contains a param with a provided name
	 * @param name
	 * @return
	 */
	public boolean containsQueryStringParam(final String name) {
		return this.getQueryStringParamValue(name) != null;
	}
	/**
	 * Checks if the url's query string contains a param with a provided name
	 * @param name
	 * @return
	 */
	public String getQueryStringParamValue(final String name) {
		String outValue = null;
		Set<WebUrlQueryStringParam> queryStringParams = this.getQueryStringParams();
		if (CollectionUtils.hasData(queryStringParams)) {
			for (WebUrlQueryStringParam param : queryStringParams) {
				if (param.getName().equals(name)) {
					outValue = param.getValue();
					break;
				}
			}
		}
		return outValue;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return an object of type {@link WebUrl} from the string representing the url
	 */
	public WebUrl asWebUrl() {
		return WebUrl.from(_url.toString());
	}
	/**
	 * Return an object of type {@link URL} from the string representing the url
	 * @return
	 * @throws MalformedURLException
	 */
	public URL asUrl() throws MalformedURLException {
		return new URL(_url.toString());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  WebUrlAsString
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String asString() {
		return this.asStringNotUrlEncodingQueryStringParamsValues();
	}
	/**
	 * Returns the url as {@link String} url-encoding the query string param values as 
	 * specified by the param
	 * @param encodeQueryStringParams
	 * @return
	 */
	public String asString(boolean encodeQueryStringParams) {
		return encodeQueryStringParams ? this.asStringUrlEncodingQueryStringParamsValues()
									   : this.asStringNotUrlEncodingQueryStringParamsValues();
	}
	/**
	 * Returns the url as a {@link String} url-encoding the query string param values 
	 * @return
	 */
	public String asStringUrlEncodingQueryStringParamsValues() {
		String outUrl = null;
		// Split the query string to encode it
		String[] urlSplitted = _url.toString().split("\\?");
		if (urlSplitted.length == 2) {
			String queryStringEncoded = WebUrlQueryStringWrapper.fromParamsString(urlSplitted[1])
																.asStringEncodingParamValues();
			outUrl = Strings.of("{}?{}")
							.customizeWith(urlSplitted[0],queryStringEncoded)
							.asString();
		} else if (urlSplitted.length == 1) {
			outUrl = urlSplitted[0];
		} else {
			throw new IllegalArgumentException(Throwables.message("The url {} is NOT valid!",_url));
		}
		return outUrl;
	}
	/**
	 * Returns the url as a {@link String} NOT encoding the query string param values 
	 * @return
	 */
	public String asStringNotUrlEncodingQueryStringParamsValues() {
		return _url.toString();
	}
	@Override
	public String toString() {
		return this.asString();
	}
}
