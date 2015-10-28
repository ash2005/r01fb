package r01f.types.weburl;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.NoArgsConstructor;
import r01f.types.weburl.WebUrl.WebUrlSiteVoid;

/**
 * Default implementation for a web url where security zone nor environment ( {@link WebUrlSecurityZone}  / {@link WebUrlEnvironment} ) are taken into account
 */
@XmlRootElement(name="webUrl")
@NoArgsConstructor
public class WebUrl 
     extends WebUrlBase<WebUrlSiteVoid> {
	
	private static final long serialVersionUID = -7395280914594355116L;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public WebUrl(final WebUrl other) {
		super(other);
	}
	public static WebUrl from(final SerializedURL url) {
		return WebUrl.from(url.asString());
	}
	public static WebUrl from(final Host host,final int port) {
		WebUrl outUrl = new WebUrl();
		outUrl.setSite(outUrl._siteFrom(host,port));
		outUrl.setPort(port);
		return outUrl;
	}
	public static WebUrl from(final String url) {
		WebUrl outUrl = new WebUrl();
		outUrl._from(url);
		return outUrl;
	}
	public static WebUrl from(final WebUrl other) {
		WebUrl outUrl = new WebUrl();
		outUrl._from(other.asSerializedUrl()
						  .asStringUrlEncodingQueryStringParamsValues());
		return outUrl;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	protected WebUrlSiteVoid _siteFrom(final Host site,final int port) {		
		WebUrlSiteVoid outSite = new WebUrlSiteVoid(null,null,
													site);
		return outSite;
	}	
	@Override
	protected WebUrlSiteVoid _siteFrom(final WebUrlSiteVoid other) {
		return new WebUrlSiteVoid(other.getSecurityZone(),other.getEnvironment(),
								  other.getHost());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static class WebUrlSiteVoid 
			    extends WebUrlSite<WebUrlSecurityZoneVoid,WebUrlEnvironmentVoid> {
		private static final long serialVersionUID = 3359706043988923647L;
		
		public WebUrlSiteVoid(final WebUrlSecurityZoneVoid securityZone,final WebUrlEnvironmentVoid environment, 
							  final Host host) {
			super(securityZone,environment,
				  host);
		}
	}
	public static class WebUrlSecurityZoneVoid implements WebUrlSecurityZone<WebUrlSecurityZoneVoid> {
		@Override
		public boolean isIn(WebUrlSecurityZoneVoid... zones) {
			return true;
		}
		@Override
		public boolean is(WebUrlSecurityZoneVoid zone) {
			return true;
		}
		@Override
		public boolean isExternal() {
			return true;
		}
	}
	public static class WebUrlEnvironmentVoid 
	         implements WebUrlEnvironment<WebUrlEnvironmentVoid>{
		@Override
		public boolean isIn(WebUrlEnvironmentVoid... envs) {
			return true;
		}
		@Override
		public boolean is(WebUrlEnvironmentVoid env) {
			return true;
		}	
	}
}
