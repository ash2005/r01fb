package r01f.internal;

import java.nio.charset.Charset;

import com.google.common.base.Charsets;

import lombok.extern.slf4j.Slf4j;


/**
 * Environment properties
 */
@Slf4j
public class R01F {
///////////////////////////////////////////////////////////////////////////////
// CONSTANTES
///////////////////////////////////////////////////////////////////////////////
	public static String ENCODING_UTF_8 = "UTF-8";
	public static String ENCODING_ISO_8859_1 = "ISO-8859-1";
	
	public static Charset DEFAULT_CHARSET = Charsets.UTF_8; 
	
	public static int CORE_GROUP = 0;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static void initSystemEnv() {
		log.warn("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		log.warn("Setting base system properties:");
		log.warn("networkaddress.cache.ttl=10");
		log.warn("file.encoding={}",DEFAULT_CHARSET.name());
		log.warn("mail.mime.charset={}",DEFAULT_CHARSET.name());
		log.warn("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
		
		// set all encodings to UTF-8 (independent of the underlying OS) to provide a stable environment
		System.setProperty("file.encoding",DEFAULT_CHARSET.name());
		System.setProperty("mail.mime.charset",DEFAULT_CHARSET.name());
		
		// if DNS round robin based services are going to be used (like amazon S3 or a mail cluster),
		// don�t forget to configure the DNS cache tiemout of Java (which is also infinite by default)
		java.security.Security.setProperty("networkaddress.cache.ttl","10"); 	// Only cache DNS lookups for 10 seconds
	}
}
