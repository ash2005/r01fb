package r01f.test.types;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.junit.Assert;
import org.junit.Test;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.marshalling.Marshaller;
import r01f.marshalling.simple.SimpleMarshallerBuilder;
import r01f.types.Paths;
import r01f.types.url.Host;
import r01f.types.url.Url;
import r01f.types.url.Url.UrlComponents;
import r01f.types.url.UrlPath;
import r01f.types.url.UrlProtocol;
import r01f.types.url.UrlQueryString;
import r01f.types.url.Urls;

public class UrlTest {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
//	@Test
	public void testMarshalling() {
		Marshaller marshaller = SimpleMarshallerBuilder.createForPackages(this.getClass().getPackage().getName())
													   .getForSingleUse();
		MyModelObj obj1 = MyModelObj.createInstance();
		String xml1 = marshaller.xmlFromBean(obj1);
		System.out.println("xml1: " + xml1);
		Assert.assertTrue(xml1.contains("anUrl='http://www.euskadi.eus'"));
		Assert.assertTrue(xml1.contains("<url><![CDATA[site.com/foo/bar/baz.html?param1=param1Value&param2=param2Value#anchor]]></url>"));
		
		MyModelObj obj2 = marshaller.beanFromXml(xml1);
		String xml2 = marshaller.xmlFromBean(obj2);
		System.out.println("xml2: " + xml2);
		Assert.assertTrue(xml1.equals(xml2));
	}
	@XmlRootElement(name="myModelObj")
	@Accessors(prefix="_")
	@NoArgsConstructor @AllArgsConstructor
	private static class MyModelObj {
		@XmlAttribute(name="anUrl")
		@Getter @Setter private Url _anUrl;
		
		@XmlValue
		@Getter @Setter private Url _anotherUrl;
		
		public static MyModelObj createInstance() {
			return new MyModelObj(Url.from("www.euskadi.eus"),
								  Url.from("site.com/foo/bar/baz.html?param1=param1Value&param2=param2Value#anchor"));
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Test
	public void testBuilderMethods() {
		Url branchesUrl = Url.from(Host.of("myHost/someUrlPath?a=1"),80,
			   				       Paths.forUrlPaths().join("otherUrlPath"),
			   				       UrlQueryString.fromParamsString("b=2&c=3"));
		System.out.println("====>" + branchesUrl);
		Assert.assertEquals("http://myHost/someUrlPath/otherUrlPath?a=1&b=2&c=3",branchesUrl.asString());		
		
		
		Url url0 = Url.from("http://www.euskadi.eus");
		_checkComponents(url0.getComponents(), 
						 UrlProtocol.HTTP,Host.of("www.euskadi.eus"),80);
		System.out.println("-->Url OK: " + url0.asString());
		
		Url url01 = Url.from("www.euskadi.eus");
		_checkComponents(url01.getComponents(), 
						 UrlProtocol.HTTP,Host.of("www.euskadi.eus"),80);
		System.out.println("-->Url OK: " + url01.asString());
		
		Url url02 = Url.from("localhost");
		_checkComponents(url02.getComponents(), 
						 UrlProtocol.HTTP,Host.localhost(),80);
		System.out.println("-->Url OK: " + url02.asString());
		
		
		Url url1 = Url.from("http://www.euskadi.eus/foo/bar/baz.html");
		_checkComponents(url1.getComponents(), 
						 UrlProtocol.HTTP,Host.of("www.euskadi.eus"),80,
						 UrlPath.from("/foo/bar/baz.html"));
		System.out.println("-->Url OK: " + url1.asString());
		
		Url url2 = Url.from("www.euskadi.eus/foo/bar/baz.html?param1=param1Value&param2=param2Value#anchor");
		_checkComponents(url2.getComponents(), 
						 UrlProtocol.HTTP,Host.of("www.euskadi.eus"),80,
						 UrlPath.from("/foo/bar/baz.html"),UrlQueryString.fromParamsString("param1=param1Value&param2=param2Value"),"anchor");
		System.out.println("-->Url OK: " + url2.asString());
		
		Url url3 = Url.from("localhost:8080/foo/bar/baz.html?param1=param1Value&param2=param2Value#anchor");	
		_checkComponents(url3.getComponents(), 
						 null,Host.of("localhost"),8080,
						 UrlPath.from("/foo/bar/baz.html"),UrlQueryString.fromParamsString("param1=param1Value&param2=param2Value"),"anchor");
		System.out.println("-->Url OK: " + url3.asString());
		
		Url url4 = Url.from("localhost/foo/bar/baz.html?param1=param1Value&param2=param2Value#anchor");
		_checkComponents(url4.getComponents(), 
						 UrlProtocol.HTTP,Host.localhost(),80,
						 UrlPath.from("/foo/bar/baz.html"),UrlQueryString.fromParamsString("param1=param1Value&param2=param2Value"),"anchor");
		System.out.println("-->Url OK: " + url4.asString());
		
		Url url5 = Url.from("localhost#anchor");
		_checkComponents(url5.getComponents(), 
						 UrlProtocol.HTTP,Host.localhost(),80,
						 "anchor");
		System.out.println("-->Url OK: " + url5.asString());
		
		Url url6 = Url.from("anyhost/foo/bar/baz.html?param1=param1Value&param2=param2Value#anchor");
		_checkComponents(url6.getComponents(), 
						 null,null,0,
						 UrlPath.from("anyhost/foo/bar/baz.html"),UrlQueryString.fromParamsString("param1=param1Value&param2=param2Value"),"anchor");
		System.out.println("-->Url OK: " + url6.asString());
		
		Url url7 = Url.from("file://d/:/eclipse/projects_default/aa88/aa88fDocs/test/test2.txt");
		_checkComponents(url7.getComponents(),
						 UrlProtocol.FILE,null,0,
						 UrlPath.from("d/:/eclipse/projects_default/aa88/aa88fDocs/test/test2.txt"));
		System.out.println("-->Url OK: " + url7.asString());
	}
//	@Test
	public void testJoin() {
		Url url1 = Urls.join(Url.from("http://localhost:8080"),
							 UrlPath.from("/foo/bar"));
		_checkComponents(url1.getComponents(),
						 UrlProtocol.HTTP,Host.localhost(),8080,
						 UrlPath.from("foo/bar"),null,null);
	}
//	@Test 
	public void testAsString() {
		Url url1 = Url.from("http://localhost:8080/");
		Assert.assertEquals(url1.asString(),"http://localhost:8080");
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static void _checkComponents(final UrlComponents comps,
										 final UrlProtocol protocol,final Host host,final int port,
										 final UrlPath urlPath,final UrlQueryString qryString) {
		_checkComponents(comps,protocol,host,port,
						 urlPath,qryString,null);
	}
	private static void _checkComponents(final UrlComponents comps,
										 final UrlProtocol protocol,final Host host,final int port,
										 final UrlPath urlPath) {
		_checkComponents(comps,protocol,host,port,
						 urlPath,null,null);
	}
	private static void _checkComponents(final UrlComponents comps,
										 final UrlProtocol protocol,final Host host,final int port) {
		_checkComponents(comps,protocol,host,port,
						 null,null,null);
	}
	private static void _checkComponents(final UrlComponents comps,
										 final UrlProtocol protocol,final Host host,final int port,
										 String anchor) {
		_checkComponents(comps,protocol,host,port,
						 null,null,anchor);
	}
	private static void _checkComponents(final UrlComponents comps,
										 final UrlProtocol protocol,final Host host,final int port,
										 final UrlPath urlPath,final UrlQueryString qryString,final String anchor) {
		if (comps.getProtocol() != null) 		Assert.assertTrue(comps.getProtocol() == protocol);
		if (comps.getPort() > 0)				Assert.assertTrue(comps.getPort() == port);
		if (comps.getHost() != null)			Assert.assertTrue(comps.getHost().equals(host));
		if (comps.getUrlPath() != null) 		Assert.assertTrue(comps.getUrlPath().equals(urlPath));
		if (comps.getQueryString() != null) 	Assert.assertTrue(comps.getQueryString().equals(qryString));
		if (comps.getAnchor() != null) 			Assert.assertTrue(comps.getAnchor().equals(anchor));
	}
}
